package com.hzq.core.result;

import lombok.Getter;

/**
 * @author hua
 * @enum com.hua.common.exception ResultEnum
 * @date 2024/8/24 10:33
 * @description 非 HTTP 状态码，用于结果集返回码，便于前端的成功、错误、警告处理
 */
@Getter
public enum ResultEnum {
    // 客户端操作失败 4xx
    BAD_REQUEST_FORMAT(400, "请求消息内容格式不正确"),
    BAD_REQUEST_METHOD(400, "请求方法异常"),
    ACCESS_UNAUTHORIZED(401, "用户访问未被授权"),
    TOKEN_INVALID_OR_EXPIRED(401, "Token无效或已过期"),
    ACCESS_FORBIDDEN(403, "用户无权限访问"),
    // 服务端操作失败 5xx
    BAD_RESPONSE_FORMAT(500, "响应消息内容格式不正确"),
    SERVER_ERROR(500, "服务器内部错误"),
    // 自定义业务成功提示 1xxx
    OPERATION_SUCCESS(1000, "操作成功"),
    // 自定义业务警告提示 2xxx
    JWT_PARSE_ERROR(2000, "JWT 格式不正确，解析失败"),
    USERNAME_OR_PASSWORD_ERROR(2000, "用户名或密码错误"),
    USER_DISABLED(2000, "用户被禁用"),
    USER_NO_ROLE(2000, "用户未拥有角色"),
    SYSTEM_CLIENT_NOT_REGISTERED(2000, "系统客户端未注册"),
    SYSTEM_CLIENT_NO_SCOPE(2000, "系统客户端没有该访问范围"),
    ACCESS_TOKEN_GENERATE_ERROR(2000, "访问令牌生成失败"),
    REFRESH_TOKEN_GENERATE_ERROR(2000, "刷新令牌生成失败"),
    // 自定义业务失败提示 3xxx
    AUTHORIZATION_MODE_ERROR(2000, "登录请求授权模式错误"),
    CUSTOM_ERROR(3000, "自定义异常");

    private final int code;
    private final String message;

    ResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
