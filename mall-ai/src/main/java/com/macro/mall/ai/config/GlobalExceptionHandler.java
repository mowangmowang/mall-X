package com.macro.mall.ai.config;

import com.macro.mall.ai.exception.AiApiException;
import com.macro.mall.ai.exception.AiServiceException;
import com.macro.mall.common.api.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理参数验证异常和其他业务异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 @Valid 注解抛出的参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("参数验证失败: {}", message);
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理 BindException 异常
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("参数绑定失败: {}", message);
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理 ConstraintViolationException 异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("约束验证失败: {}", message);
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理 AI API 调用异常
     */
    @ExceptionHandler(AiApiException.class)
    public CommonResult<Void> handleAiApiException(AiApiException e) {
        log.error("AI API 调用失败 [errorCode={}, httpStatus={}]: {}", e.getErrorCode(), e.getHttpStatusCode(), e.getMessage());
        
        // 根据 HTTP 状态码返回不同的提示
        if (e.getHttpStatusCode() != null) {
            if (e.getHttpStatusCode() == 401 || e.getHttpStatusCode() == 403) {
                return CommonResult.failed("AI 服务认证失败，请检查 API Key 配置");
            } else if (e.getHttpStatusCode() == 429) {
                return CommonResult.failed("AI 服务请求过于频繁，请稍后重试");
            } else if (e.getHttpStatusCode() >= 500) {
                return CommonResult.failed("AI 服务暂时不可用，请稍后重试");
            }
        }
        
        return CommonResult.failed("AI 服务调用失败: " + e.getMessage());
    }

    /**
     * 处理 AI 业务异常
     */
    @ExceptionHandler(AiServiceException.class)
    public CommonResult<Void> handleAiServiceException(AiServiceException e) {
        log.warn("AI 业务异常 [errorCode={}]: {}", e.getErrorCode(), e.getMessage());
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理 IllegalArgumentException 异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return CommonResult.failed("请求参数错误: " + e.getMessage());
    }

    /**
     * 处理 IllegalStateException 异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public CommonResult<Void> handleIllegalStateException(IllegalStateException e) {
        log.error("状态异常: {}", e.getMessage(), e);
        return CommonResult.failed("服务状态异常: " + e.getMessage());
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return CommonResult.failed("系统内部错误，请稍后重试");
    }
}
