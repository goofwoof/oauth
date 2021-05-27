package com.li.oauth.persistence.repository;

import com.li.oauth.persistence.entity.OauthClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthClientRepository extends JpaRepository<OauthClientEntity, Long> {
    OauthClientEntity findByClientId(String clientId);

    Page<OauthClientEntity> findAllByUserId(Long id, Pageable pageable);
}
