package com.macro.mall.common.exception;

import com.macro.mall.common.api.IErrorCode;

/**
 * 自定义 API 异常 (Custom API Exception)
 * 用于封装业务逻辑中的错误信息，支持通过 IErrorCode 或直接消息字符串构造
 * Created by alan
 */
public class ApiException extends RuntimeException {
    /** 错误码 (Error Code)，可为 null（当使用纯文本消息时） */
    private IErrorCode errorCode;

    /**
     * 使用错误码构造异常
     *
     * @param errorCode 错误码接口
     */
    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 使用错误消息构造异常
     *
     * @param message 错误消息
     */
    public ApiException(String message) {
        super(message);
    }

    /**
     * 使用异常原因构造异常
     *
     * @param cause 异常原因
     */
    public ApiException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用错误消息和异常原因构造异常
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 获取错误码
     *
     * @return 错误码接口，可能为 null
     */
    public IErrorCode getErrorCode() {
        return errorCode;
    }
}
