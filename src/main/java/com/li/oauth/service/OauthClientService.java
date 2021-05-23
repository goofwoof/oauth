package com.li.oauth.service;

import com.li.oauth.domain.Exception.NotImplementException;
import com.li.oauth.domain.OauthClient;

public interface OauthClientService extends com.li.oauth.service.CommonServiceInterface<OauthClient> {
    default OauthClient findByClientId(String clientId){
        throw new NotImplementException();
    }
}
