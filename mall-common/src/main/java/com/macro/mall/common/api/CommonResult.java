package com.macro.mall.common.api;

/**
 * 通用返回结果封装类
 * 统一 API 响应格式，包含状态码、提示信息及数据内容
 * Created by macro
 */
public class CommonResult<T> {
    /**
     * 状态码
     */
    private long code;
    /**
     * 提示信息
     */
    private String message;
    /**
     * 数据封装
     */
    private T data;

    protected CommonResult() {
    }

    protected CommonResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果（无自定义消息）
     *
     * @param data 业务数据
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果（带自定义消息）
     *
     * @param data    业务数据
     * @param message 自定义提示信息
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果（使用错误码枚举）
     *
     * @param errorCode 错误码枚举
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> failed(IErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果（使用错误码枚举 + 自定义消息）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> failed(IErrorCode errorCode, String message) {
        return new CommonResult<T>(errorCode.getCode(), message, null);
    }

    /**
     * 失败返回结果（仅自定义消息）
     *
     * @param message 错误提示信息
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果（使用默认错误码和消息）
     *
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> failed() {
        return failed(ResultCode.FAILED);
    }

    /**
     * 参数验证失败返回结果（使用默认消息）
     *
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> validateFailed() {
        return failed(ResultCode.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果（带自定义消息）
     *
     * @param message 具体字段错误提示
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<T>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果（401 Unauthorized）
     *
     * @param data 额外数据（通常为空）
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<T>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权/无权限返回结果（403 Forbidden）
     *
     * @param data 额外数据（通常为空）
     * @return 统一响应对象
     */
    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<T>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
