/*
 * Copyright (c) 2021-2021. com.li,Inc. All rights reserved.
 *     项目名称: oauth
 *     文件名称: RoleApplyRepository.java
 */

package com.li.oauth.persistence.repository;

import com.li.oauth.persistence.entity.RoleApplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @InterfaceName: RoleApplyRepository
 * @Description:
 * @author: puthlive
 * @date: 2021/5/26
 */
public interface RoleApplyRepository extends JpaRepository<RoleApplyEntity, Long> {

    @Query(
        value = "SELECT * FROM role_apply_entity u WHERE u.user_id = ?1 and u.role_id = ?2",
        nativeQuery = true)
    List<RoleApplyEntity> findApply(Long userId, Long RoleId);
}
