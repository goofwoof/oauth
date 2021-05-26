package com.li.oauth;

public abstract class ErrorCodeConstant {

    public static final Integer DEFAULT_SUCCESS = 200;

    /**
     * Oauth Service 100_XX_XX

     * Token 异常 100_00_XX
     *
     * Token 错误
     */
    public static final Integer TOKEN_ERROR = 1000000;

    /**
     * Token 过期
     */
    public static final Integer TOKEN_EXPIRED = 1000001;
    /**
     * Token 授权错误
     */
    public static final Integer TOKEN_GRANT_ERROR = 1000002;
    /**
     * Code 异常
     */
    public static final Integer TOKEN_CODE_EXPIRED = 1000003;

    /**
     * 系统异常 100_01_XX
     * 内部异常
     */
    public static final Integer INTERNAL_ERROR = 1000100;
    /**
     * handler 未定义
     */
    public static final Integer HANDLER_NOT_FOUND = 1000101;
    /**
     * handler 禁止
     */
    public static final Integer HANDLER_FORBIDDEN = 1000102;
    /**
     * 参数错误
     */
    public static final Integer PARAM_INVALID = 1000103;
    /**
     * 验证码过期
     */
    public static final Integer CAPTCHAR_EXPIRED = 1000104;

    /**
     * 账户异常100_02_XX
     * 账户错误
     */
    public static final Integer USER_ACCOUNT_ERROR = 1000200;

    /**
     *  权限不足
     */
    public static final Integer USER_ROLES_ACCESS_DINED = 1000201;

    /**
    *   角色申请失败
    */
    public static final Integer ROLE_APPLY_ERROR = 1000202;

    /**
     * Oauth接入CLIENT异常100_03_XX
     * client不存在
     */
    public static final Integer CLIENT_NOT_FOUND = 1000300;
}
