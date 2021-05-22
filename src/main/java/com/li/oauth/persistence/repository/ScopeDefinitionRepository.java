package com.li.oauth.persistence.repository;

import com.li.oauth.persistence.entity.ScopeDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScopeDefinitionRepository extends JpaRepository<ScopeDefinitionEntity, Long> {
    ScopeDefinitionEntity findByScope(String scope);
}
