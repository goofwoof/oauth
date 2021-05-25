package com.li.oauth.domain;

public enum RoleEnum {

    /**
     * 普通用户
     */
    ROLE_USER("普通用户"),
    /**
     * 开发者
     */
    ROLE_DEVELOPER("开发者"),
    /**
     * 管理员
     */
    ROLE_ADMIN("管理员"),
    /**
     * 超级
     */
    ROLE_SUPER("超级");

    private String meaning;

    public String getMeaning() {
        return meaning;
    }

    RoleEnum() {
    }

    RoleEnum(String meaning) {
        this.meaning = meaning;
    }
}
