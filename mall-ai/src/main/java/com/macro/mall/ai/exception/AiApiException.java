package com.macro.mall.ai.exception;

/**
 * AI API 调用异常 (AI API Call Exception)
 * 用于封装调用外部 AI API 时发生的异常
 */
public class AiApiException extends AiServiceException {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP 状态码 (HTTP Status Code)
     */
    private Integer httpStatusCode;

    public AiApiException(String message) {
        super("AI_API_ERROR", message);
    }

    public AiApiException(String message, Throwable cause) {
        super("AI_API_ERROR", message, cause);
    }

    public AiApiException(Integer httpStatusCode, String message) {
        super("AI_API_ERROR", message);
        this.httpStatusCode = httpStatusCode;
    }

    public AiApiException(Integer httpStatusCode, String message, Throwable cause) {
        super("AI_API_ERROR", message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
