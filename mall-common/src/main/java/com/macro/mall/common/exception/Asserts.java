package com.macro.mall.common.exception;

import com.macro.mall.common.api.IErrorCode;

/**
 * 断言处理类 (Assertion Utility)
 * 提供便捷的参数校验方法，校验失败时直接抛出 ApiException
 */
public class Asserts {
    /**
     * 校验失败，抛出带自定义消息的异常
     *
     * @param message 错误提示消息
     */
    public static void fail(String message) {
        throw new ApiException(message);
    }

    /**
     * 校验失败，抛出带错误码的异常
     *
     * @param errorCode 错误码枚举
     */
    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }
}
