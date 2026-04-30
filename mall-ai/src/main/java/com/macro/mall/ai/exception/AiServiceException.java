package com.macro.mall.ai.exception;

/**
 * AI 服务异常基类 (AI Service Exception Base Class)
 * 用于封装 AI 服务相关的业务异常
 */
public class AiServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码 (Error Code)
     */
    private String errorCode;

    public AiServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
    }

    public AiServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
