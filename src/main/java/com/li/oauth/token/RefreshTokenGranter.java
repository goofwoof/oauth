package com.li.oauth.token;

import com.li.oauth.ErrorCodeConstant;
import com.li.oauth.domain.OauthClient;
import com.li.oauth.utils.UuidCreateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RefreshTokenGranter implements TokenGranter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String GRANT_TYPE = "refresh_token";
    private final AuthenticationManager authenticationManager;
    KeyPair keyPair;
    String issuer;

    public RefreshTokenGranter(AuthenticationManager authenticationManager, KeyPair keyPair, String issuer) {
        this.authenticationManager = authenticationManager;
        this.keyPair = keyPair;
        this.issuer = issuer;
    }

    @Override
    public Map<String, Object> grant(OauthClient client, String grantType, Map<String, String> parameters) {

        Map<String, Object> result = new HashMap<>();
        result.put("errorCode", ErrorCodeConstant.TOKEN_GRANT_ERROR);

        String refreshToken = parameters.get("refresh_token");

        if (!GRANT_TYPE.equals(grantType)) {
            return result;
        }

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(refreshToken).getBody();
            Date now = new Date();
            Date tokenExpiration = Date.from(LocalDateTime.now().plusSeconds(client.getAccessTokenValidity()).atZone(ZoneId.systemDefault()).toInstant());
            Date refreshTokenExpiration = Date.from(LocalDateTime.now().plusSeconds(client.getRefreshTokenValidity()).atZone(ZoneId.systemDefault()).toInstant());
            String tokenId = UuidCreateUtils.createTokenCode();
            claims.setId(tokenId);
            claims.setIssuedAt(now);
            claims.setExpiration(tokenExpiration);
            claims.setNotBefore(now);

            claims.put("jti", tokenId);
            String newRefreshToken = Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .signWith(keyPair.getPrivate())
                .compact();

            claims.setId(tokenId);
            claims.setIssuedAt(now);
            claims.setExpiration(tokenExpiration);
            claims.setNotBefore(now);
            claims.remove("jti");
            String accessToken = Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .signWith(keyPair.getPrivate())
                .compact();

            result.put("access_token", accessToken);
            result.put("token_type", "bearer");
            result.put("refresh_token", newRefreshToken);
            result.put("expires_in", client.getAccessTokenValidity() - 1);
            result.put("accountOpenCode", claims.get("accountOpenCode"));
            result.put("scope", "user_info");
            result.put("jti", tokenId);
            result.put("errorCode", ErrorCodeConstant.DEFAULT_SUCCESS);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("exception", e);
            }
            throw e;
        }


        return result;
    }
}
