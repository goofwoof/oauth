package com.li.oauth.utils;

import java.util.UUID;

public class UuidCreateUtils {
    public static String createUserOpenId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String createOauthClientOpenId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String createTokenCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String createUniqueCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
