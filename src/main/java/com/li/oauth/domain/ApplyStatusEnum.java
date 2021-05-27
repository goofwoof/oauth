/*
 * Copyright (c) 2021-2021. com.li,Inc. All rights reserved.
 *     项目名称: oauth
 *     文件名称: ApplyStatusEnum.java
 */

package com.li.oauth.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<String> names() {
        return Arrays.asList(REVIEWING.name(), APPROVED.name(), REJECTED.name());
    }

    public String getMeaning() {
        return meaning;
    }

    ApplyStatusEnum() {
    }

    ApplyStatusEnum(String meaning) {
        this.meaning = meaning;
    }
}
