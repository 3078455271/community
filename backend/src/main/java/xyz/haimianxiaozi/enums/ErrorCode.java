package xyz.haimianxiaozi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 * 约定：
 * - 1xxxx 通用 / 系统
 * - 2xxxx 认证授权
 * - 3xxxx 用户
 * - 4xxxx 帖子 / 评论 / 内容
 *
 * @author haimianxiaozi
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* ===== 通用 ===== */
    SUCCESS(200, "success"),
    SYSTEM_ERROR(10000, "系统繁忙，请稍后再试"),
    PARAM_INVALID(10001, "参数校验失败"),
    RESOURCE_NOT_FOUND(10002, "请求的资源不存在"),

    /* ===== 认证授权 ===== */
    UNAUTHORIZED(20001, "请先登录"),
    TOKEN_INVALID(20002, "登录状态无效或已过期"),
    FORBIDDEN(20003, "无权访问"),
    BAD_CREDENTIALS(20004, "用户名或密码错误"),
    ACCOUNT_DISABLED(20005, "账号已被禁用"),

    /* ===== 用户 ===== */
    USERNAME_EXISTS(30001, "用户名已存在"),
    USER_NOT_FOUND(30002, "用户不存在"),
    OLD_PASSWORD_WRONG(30003, "原密码错误"),

    /* ===== 内容 ===== */
    POST_NOT_FOUND(40001, "帖子不存在"),
    COMMENT_NOT_FOUND(40002, "评论不存在"),
    ALREADY_LIKED(40003, "已点赞"),
    NOT_LIKED(40004, "未点赞");

    private final int code;
    private final String message;
}
