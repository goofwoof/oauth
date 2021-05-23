package com.li.oauth.token;

import com.li.oauth.ErrorCodeConstant;
import com.li.oauth.config.CachesEnum;
import com.li.oauth.domain.Exception.OAuth2Exception;
import com.li.oauth.domain.OauthClient;
import com.li.oauth.domain.UserInfo;
import com.li.oauth.utils.UuidCreateUtils;
import io.jsonwebtoken.Jwts;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuthorizationCodeTokenGranter implements TokenGranter {
    private static final String GRANT_TYPE = "authorization_code";
    private final AuthenticationManager authenticationManager;
    KeyPair keyPair;
    String issuer;
    CacheManager cacheManager;

    public AuthorizationCodeTokenGranter(AuthenticationManager authenticationManager, CacheManager cacheManager, KeyPair keyPair, String issuer) {
        this.authenticationManager = authenticationManager;
        this.cacheManager = cacheManager;
        this.keyPair = keyPair;
        this.issuer = issuer;
    }

    @Override
    public Map<String, Object> grant(OauthClient client, String grantType, Map<String, String> parameters) {

        Map<String, Object> result = new HashMap<>();
        result.put("errorCode", ErrorCodeConstant.PARAM_INVALID);

        String authorizationCode = parameters.get("code");
        String redirectUri = parameters.get("redirect_uri");
        String clientId = parameters.get("client_id");
        String scope = parameters.get("scope");
        if (authorizationCode == null) {
            throw new OAuth2Exception("An authorization code must be supplied.", HttpStatus.BAD_REQUEST, ErrorCodeConstant.TOKEN_CODE_EXPIRED);
        }
        Cache.ValueWrapper storedCode = Objects.requireNonNull(cacheManager.getCache(CachesEnum.Oauth2AuthorizationCodeCache.name())).get(authorizationCode);
        if (storedCode != null) {

            Authentication userAuth = (Authentication) (storedCode.get());
            assert userAuth != null;
            UserInfo userInfo = (UserInfo) userAuth.getPrincipal();

            Date now = new Date();
            Date tokenExpiration = Date.from(LocalDateTime.now().plusSeconds(client.getAccessTokenValidity()).atZone(ZoneId.systemDefault()).toInstant());
            Date refreshTokenExpiration = Date.from(LocalDateTime.now().plusSeconds(client.getRefreshTokenValidity()).atZone(ZoneId.systemDefault()).toInstant());


            String tokenId = UuidCreateUtils.createTokenCode();
            String accessToken = Jwts.builder()
                    .setHeaderParam("alg", "HS256")
                    .setHeaderParam("typ", "JWT")
                    .claim("accountOpenCode", userInfo.getAccountOpenCode())
                    .setIssuer(issuer)
                    .setSubject(userInfo.getUsername())
                    .setAudience(clientId)
                    .claim("roles", userInfo.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .setExpiration(tokenExpiration)
                    .setNotBefore(now)
                    .setIssuedAt(now)
                    .setId(tokenId)
                    .signWith(keyPair.getPrivate())
                    .compact();

            String refreshToken = Jwts.builder()
                    .setHeaderParam("alg", "HS256")
                    .setHeaderParam("typ", "JWT")
                    .claim("accountOpenCode", userInfo.getAccountOpenCode())
                    .claim("jti", tokenId)
                    .setIssuer(issuer)
                    .setSubject(userInfo.getUsername())
                    .setAudience(clientId)
                    .claim("roles", userInfo.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .setExpiration(refreshTokenExpiration)
                    .setNotBefore(now)
                    .setIssuedAt(now)
                    .setId(UuidCreateUtils.createTokenCode())
                    .signWith(keyPair.getPrivate())
                    .compact();

            Objects.requireNonNull(cacheManager.getCache(CachesEnum.Oauth2AuthorizationCodeCache.name())).evictIfPresent(authorizationCode);

            result.put("access_token", accessToken);
            result.put("token_type", "bearer");
            result.put("refresh_token", refreshToken);
            result.put("expires_in", client.getAccessTokenValidity() - 1);
            result.put("accountOpenCode", userInfo.getAccountOpenCode());
            result.put("scope", scope);
            result.put("jti", tokenId);
            result.put("errorCode", ErrorCodeConstant.DEFAULT_SUCCESS);
            return result;
        } else {
            throw new OAuth2Exception("Authorization code was expired.", HttpStatus.BAD_REQUEST, ErrorCodeConstant.TOKEN_EXPIRED);
        }
    }
}
