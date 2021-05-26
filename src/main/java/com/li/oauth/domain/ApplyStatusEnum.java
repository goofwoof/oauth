/*
 * Copyright (c) 2021-2021. com.li,Inc. All rights reserved.
 *     项目名称: oauth
 *     文件名称: ApplyStatusEnum.java
 */

package com.li.oauth.domain;

/**
 * @EnumName: ApplyStatusEnum
 * @Description:
 * @author: puthlive
 * @date: 2021/5/26
 */
public enum ApplyStatusEnum {
    /**
     * 待审核
     */
    REVIEWING("待审核"),
    /**
     * 已批准
     */
    APPROVED("已批准"),
    /**
     * 已驳回
     */
    REJECTED("已驳回");

    private String meaning;

    public String getMeaning() {
        return meaning;
    }

    ApplyStatusEnum() {
    }

    ApplyStatusEnum(String meaning) {
        this.meaning = meaning;
    }
}
