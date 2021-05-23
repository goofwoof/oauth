package com.li.oauth.controller;

import com.li.oauth.config.CachesEnum;
import com.li.oauth.domain.OauthClient;
import com.li.oauth.domain.ScopeDefinition;
import com.li.oauth.service.OauthClientService;
import com.li.oauth.service.ScopeDefinitionService;
import com.li.oauth.token.AuthorizationCodeTokenGranter;
import com.li.oauth.token.PasswordTokenGranter;
import com.li.oauth.token.RefreshTokenGranter;
import com.li.oauth.utils.UuidCreateUtils;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/oauth")
public class OAuth2Controller {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OauthClientService oauthClientService;

    @Autowired
    ScopeDefinitionService scopeDefinitionService;

    CacheManager cacheManager;

    private final AuthenticationManager authenticationManager;
    private final KeyPair keyPair;
    private final String issuerUri;

    PasswordTokenGranter passwordTokenGranter;
    RefreshTokenGranter refreshTokenGranter;
    AuthorizationCodeTokenGranter authorizationCodeTokenGranter;

    public OAuth2Controller(AuthenticationManager authenticationManager, KeyPair keyPair, CacheManager cacheManager, @Value("${oauth2.issuer-uri:http://localhost:10380}") String issuerUri) {
        this.authenticationManager = authenticationManager;
        this.keyPair = keyPair;
        this.cacheManager = cacheManager;
        this.issuerUri = issuerUri;
        passwordTokenGranter = new PasswordTokenGranter(authenticationManager, keyPair, issuerUri);
        refreshTokenGranter = new RefreshTokenGranter(authenticationManager, keyPair, issuerUri);
        authorizationCodeTokenGranter = new AuthorizationCodeTokenGranter(authenticationManager, cacheManager, keyPair, issuerUri);
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> getAccessToken(@RequestParam(value = "client_id", required = false) String client_id,
                                                              @RequestParam(value = "client_secret", required = false) String client_secret,
                                                              @RequestParam(value = "grant_type") String grant_type,
                                                              @RequestParam(value = "redirect_uri", required = false) String redirect_uri,
                                                              @RequestParam(value = "refresh_token", required = false) String refresh_token,
                                                              @RequestParam(value = "code", required = false) String code,
                                                              @RequestParam(value = "username", required = false) String username,
                                                              @RequestParam(value = "password", required = false) String password) {
        Map<String, Object> result = new HashMap<>(16);
        OauthClient client = oauthClientService.findByClientId(client_id);
        HttpHeaders headers = new HttpHeaders();

        if (client == null) {
            result.put("status", 0);
            result.put("code", "invalid_client");
            result.put("message", "invalid_client");
            return new ResponseEntity<>(
                result, headers, HttpStatus.OK);
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", client_id);
        parameters.put("client_secret", client_secret);
        parameters.put("grant_type", grant_type);
        parameters.put("scope", client.getScope());
        parameters.put("redirect_uri", redirect_uri);
        parameters.put("refresh_token", refresh_token);
        parameters.put("code", code);
        parameters.put("username", username);
        parameters.put("password", password);
        if (StringUtils.equalsIgnoreCase(grant_type, "password")) {
            result = passwordTokenGranter.grant(client, "password", parameters);
        } else if (StringUtils.equalsIgnoreCase(grant_type, "authorization_code")) {
            if (StringUtils.isEmpty(redirect_uri) || !StringUtils.equalsIgnoreCase(client.getWebServerRedirectUri(), redirect_uri)) {
                result.put("status", 0);
                result.put("code", "invalid_redirect_uri");
                result.put("message", "invalid_redirect_uri");
                return new ResponseEntity<>(
                    result, headers, HttpStatus.OK);
            }
            result = authorizationCodeTokenGranter.grant(client, "authorization_code", parameters);
        } else if (StringUtils.equalsIgnoreCase(grant_type, "refresh_token")) {
            result = refreshTokenGranter.grant(client, grant_type, parameters);
        } else {
            result.put("status", 0);
            result.put("message", "不支持的grant类型");
        }
        return new ResponseEntity<>(
            result, headers, HttpStatus.OK);
    }

    @GetMapping("/authorize")
    public String getAccessToken(ModelMap model,
                                 Authentication authentication,
                                 @RequestHeader(name = "referer", required = false) String referer,
                                 @RequestParam(value = "client_id") String client_id,
                                 @RequestParam(value = "redirect_uri") String redirect_uri) {
        OauthClient client = oauthClientService.findByClientId(client_id);

        if (client == null || !StringUtils.equalsIgnoreCase(client.getWebServerRedirectUri(), redirect_uri)) {
            if (redirect_uri.indexOf("?") > 0) {
                return "redirect:" + redirect_uri + "&error=invalid_client";
            } else {
                return "redirect:" + redirect_uri + "?error=invalid_client";
            }
        }

        if ("1".equals(client.getAutoApprove())) {
            String uuid = UuidCreateUtils.createUniqueCode();
            Objects.requireNonNull(cacheManager.getCache(CachesEnum.Oauth2AuthorizationCodeCache.name())).put(uuid, authentication);
            if (client.getWebServerRedirectUri().indexOf("?") > 0) {
                return "redirect:" + client.getWebServerRedirectUri() + "&code=" + uuid;
            } else {
                return "redirect:" + client.getWebServerRedirectUri() + "?code=" + uuid;
            }
        } else {

            model.put("client_id", client_id);
            model.put("applicationName", client.getApplicationName());
            model.put("from", referer);
            model.put("redirect_uri", redirect_uri);
            Map<String, String> scopeMap = new LinkedHashMap<>();
            for (String scope : client.getScope().split(",")) {
                ScopeDefinition scopeDefinition = scopeDefinitionService.findByScope(scope);
                if (scopeDefinition != null) {
                    scopeMap.put("scope." + scope, scopeDefinition.getDefinition());
                } else {
                    scopeMap.put("scope." + scope, scope);
                }
            }
            model.put("scopeMap", scopeMap);

            return "accessConfirmation";
        }
    }

    @PostMapping("/authorize")
    public String postAccessToken(ModelMap model,
                                  Authentication authentication,
                                  @RequestParam(name = "referer", required = false) String referer,
                                  @RequestParam(value = "client_id") String client_id,
                                  @RequestParam(value = "user_oauth_approval", required = false, defaultValue = "false") boolean userOauthApproval,
                                  @RequestParam(value = "redirect_uri") String redirect_uri) {
        OauthClient client = oauthClientService.findByClientId(client_id);
        model.put("client_id", client_id);
        model.put("applicationName", client.getApplicationName());
        model.put("from", referer);

        if (userOauthApproval) {
            String uuid = UuidCreateUtils.createUniqueCode();
            Objects.requireNonNull(cacheManager.getCache(CachesEnum.Oauth2AuthorizationCodeCache.name())).put(uuid, authentication);
            if (client.getWebServerRedirectUri().indexOf("?") > 0) {
                return "redirect:" + client.getWebServerRedirectUri() + "&code=" + uuid;
            } else {
                return "redirect:" + client.getWebServerRedirectUri() + "?code=" + uuid;
            }
        } else {
            if (redirect_uri.indexOf("?") > 0) {
                return "redirect:" + redirect_uri + "&error=not_approval";
            } else {
                return "redirect:" + redirect_uri + "?error=not_approval";
            }
        }
    }

    @ResponseBody
    @PostMapping("/check_token")
    public Map<String, Object> checkToken(@RequestParam(value = "access_token") String access_token) {
        Map<String, Object> result = new HashMap<>(16);
        try {
            Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(access_token).getBody();
            result.put("status", 1);
        } catch (Exception e) {
            result.put("status", 0);
            result.put("message", "access_token 无效");
            if (log.isErrorEnabled()) {
                log.error("验证token异常", e);
            }
        }
        return result;
    }

}
