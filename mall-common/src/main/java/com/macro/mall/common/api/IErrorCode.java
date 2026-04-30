package com.macro.mall.common.api;

/**
 * API 返回码接口 (Error Code Interface)
 * 定义统一的错误码规范，所有错误码枚举类需实现此接口
 */
public interface IErrorCode {
    /**
     * 返回码
     */
    long getCode();

    /**
     * 返回信息
     */
    String getMessage();
}
