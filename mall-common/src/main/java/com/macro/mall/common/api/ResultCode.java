package com.macro.mall.common.api;

/**
 * API 返回码枚举类 (Result Code Enum)
 * 定义系统常用的标准错误码，实现 IErrorCode 接口
 * Created by macro
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),           // 请求成功
    FAILED(500, "操作失败"),             // 服务器内部错误
    VALIDATE_FAILED(404, "参数检验失败"), // 参数校验失败（注意：此处使用 404，通常应为 400）
    UNAUTHORIZED(401, "暂未登录或token已经过期"), // 未认证
    FORBIDDEN(403, "没有相关权限");       // 无权限访问
    private long code;
    private String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
