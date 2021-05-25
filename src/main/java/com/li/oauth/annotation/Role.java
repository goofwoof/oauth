package com.li.oauth.annotation;


import com.li.oauth.domain.RoleEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
        ElementType.TYPE,
        ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Role {
    /**
     * 要求的用户角色
     *
     * @return
     */
    RoleEnum[] value() default RoleEnum.ROLE_ADMIN;
}