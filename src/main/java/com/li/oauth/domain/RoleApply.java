/*
 * Copyright (c) 2021-2021. com.li,Inc. All rights reserved.
 *     项目名称: oauth
 *     文件名称: RoleApplyInfl.java
 */

package com.li.oauth.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: RoleApply
 * @Description:
 * @author: puthlive
 * @date: 2021/5/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleApply extends BaseDomain {
    private UserAccount user;
    private Role role;
    private String status;
}
