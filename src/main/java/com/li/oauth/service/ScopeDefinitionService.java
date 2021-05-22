package com.li.oauth.service;

import com.li.oauth.domain.NotImplementException;
import com.li.oauth.domain.ScopeDefinition;

public interface ScopeDefinitionService extends com.li.oauth.service.CommonServiceInterface<ScopeDefinition> {
    default ScopeDefinition findByScope(String scope) throws NotImplementException {
        throw new NotImplementException();
    }
}
