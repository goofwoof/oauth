package com.li.oauth.annotation;



import com.li.oauth.domain.RoleEnum;

import java.lang.annotation.*;

@Target({
        ElementType.TYPE,
        ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@LoginUser
public @interface Role {
    /**
     * 要求的用户角色
     *
     * @return
     */
    RoleEnum[] value() default RoleEnum.ROLE_ADMIN;
}