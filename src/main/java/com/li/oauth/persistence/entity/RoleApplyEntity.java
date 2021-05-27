/*
 * Copyright (c) 2021-2021. com.li,Inc. All rights reserved.
 *     项目名称: oauth
 *     文件名称: RoleApplyEntity.java
 */

package com.li.oauth.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @ClassName: RoleApplyEntity
 * @Description:
 * @author: puthlive
 * @date: 2021/5/26
 */
@Entity
@Getter
@Setter
public class RoleApplyEntity extends BaseEntity {
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private RoleEntity role;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private UserAccountEntity user;

    @Column(nullable = false, columnDefinition = "VARCHAR(15)")
    private String status;
}
