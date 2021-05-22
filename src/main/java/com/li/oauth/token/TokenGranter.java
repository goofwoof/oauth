package com.li.oauth.token;

import com.li.oauth.domain.OauthClient;

import java.util.Map;

public interface TokenGranter {

    Map<String, Object> grant(OauthClient client, String grantType, Map<String, String> parameters);

    default void validateGrantType(OauthClient client, String grantType) {

    }
}
