package com.li.oauth.persistence.repository;

import com.li.oauth.persistence.entity.RoleEntity;
import com.li.oauth.persistence.entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {
    UserAccountEntity findByUsername(String username);

    Page<UserAccountEntity> findByUsernameLike(String username, Pageable page);

    Page<UserAccountEntity> findByRolesIn(List<RoleEntity> roles, Pageable page);

    boolean existsByUsername(String username);
}
