package com.li.oauth.service.impl;

import com.github.dozermapper.core.Mapper;
import com.li.oauth.domain.NotImplementException;
import com.li.oauth.domain.ScopeDefinition;
import com.li.oauth.persistence.entity.ScopeDefinitionEntity;
import com.li.oauth.persistence.repository.ScopeDefinitionRepository;
import com.li.oauth.service.ScopeDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScopeDefinitionServiceImpl implements ScopeDefinitionService {

    @Autowired
    ScopeDefinitionRepository scopeDefinitionRepository;

    @Autowired
    Mapper dozerMapper;

    @Override
    public ScopeDefinition findByScope(String scope) throws NotImplementException {
        ScopeDefinitionEntity scopeDefinitionEntity = scopeDefinitionRepository.findByScope(scope);
        if (scopeDefinitionEntity != null) {
            return dozerMapper.map(scopeDefinitionEntity, ScopeDefinition.class);
        } else {
            return null;
        }
    }

}
