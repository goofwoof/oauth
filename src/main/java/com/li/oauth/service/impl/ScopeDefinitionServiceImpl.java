package com.li.oauth.service.impl;

import com.github.dozermapper.core.Mapper;
import com.li.oauth.domain.Exception.NotImplementException;
import com.li.oauth.domain.ScopeDefinition;
import com.li.oauth.persistence.entity.ScopeDefinitionEntity;
import com.li.oauth.persistence.repository.ScopeDefinitionRepository;
import com.li.oauth.service.ScopeDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<ScopeDefinition> findAll() {
        List<ScopeDefinitionEntity> all = scopeDefinitionRepository.findAll();
        return all.stream()
                .map(scopeDefinitionEntity -> dozerMapper.map(scopeDefinitionEntity, ScopeDefinition.class))
                .collect(Collectors.toList());
    }
}
