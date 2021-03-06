package com.li.oauth.token;

import com.li.oauth.ErrorCodeConstant;
import com.li.oauth.domain.Exception.OAuth2Exception;
import com.li.oauth.domain.OauthClient;
import com.li.oauth.domain.UserInfo;
import com.li.oauth.utils.UuidCreateUtils;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PasswordTokenGranter implements TokenGranter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String GRANT_TYPE = "password";
    private final AuthenticationManager authenticationManager;
    KeyPair keyPair;
    String issuer;

    public PasswordTokenGranter(AuthenticationManager authenticationManager, KeyPair keyPair, String issuer) {
        this.authenticationManager = authenticationManager;
        this.keyPair = keyPair;
        this.issuer = issuer;
    }

    @Override
    public Map<String, Object> grant(OauthClient client, String grantType, Map<String, String> parameters) {

        Map<String, Object> result = new HashMap<>();
        result.put("errorCode", ErrorCodeConstant.TOKEN_GRANT_ERROR);

        String username = parameters.get("username");
        String password = parameters.get("password");
        String clientId = parameters.get("client_id");
        String scope = parameters.get("scope");

        if (!GRANT_TYPE.equals(grantType)) {
            return result;
        }

        Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        try {
            userAuth = authenticationManager.authenticate(userAuth);
        } catch (AccountStatusException | BadCredentialsException ase) {
            //covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
            throw new OAuth2Exception(ase.getMessage(), HttpStatus.UNAUTHORIZED, ErrorCodeConstant.TOKEN_GRANT_ERROR);
        } // If the username/password are wrong the spec says we should send 400/invalid grant

        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new OAuth2Exception("Could not authenticate user: " + username, HttpStatus.UNAUTHORIZED, ErrorCodeConstant.TOKEN_GRANT_ERROR);
        }
        Date now = new Date();
        Date tokenExpiration = Date.from(LocalDateTime.now().plusSeconds(client.getAccessTokenValidity()).atZone(ZoneId.systemDefault()).toInstant());
        Date refreshTokenExpiration = Date.from(LocalDateTime.now().plusSeconds(client.getAccessTokenValidity()).atZone(ZoneId.systemDefault()).toInstant());

        UserInfo userInfo = (UserInfo) userAuth.getPrincipal();
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

        result.put("access_token", accessToken);
        result.put("token_type", "bearer");
        result.put("refresh_token", refreshToken);
        result.put("expires_in", client.getAccessTokenValidity() - 1);
        result.put("accountOpenCode", userInfo.getAccountOpenCode());
        result.put("scope", scope);
        result.put("jti", tokenId);
        result.put("errorCode", ErrorCodeConstant.DEFAULT_SUCCESS);
        return result;
    }
}
