package com.macro.mall.common.exception;

import cn.hutool.core.util.StrUtil;
import com.macro.mall.common.api.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.util.stream.Collectors;

/**
 * 全局异常处理类 (Global Exception Handler)
 * 使用 @ControllerAdvice 统一拦截并处理系统中抛出的各类异常
 * 返回标准化的 CommonResult 响应结果
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常 (ApiException)
     *
     * @param e 自定义异常对象
     * @return 包含错误码或错误信息的通用响应结果
     */
    @ResponseBody // ResponseBody is for json response
    @ExceptionHandler(value = ApiException.class) // ExceptionHandler is for specify exception
    public CommonResult handle(ApiException e) {
        // 优先使用错误码，若无则使用纯文本消息
        if (e.getErrorCode() != null) {
            return CommonResult.failed(e.getErrorCode());
        }
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理请求参数校验异常 (MethodArgumentNotValidException)
     * 通常用于 @RequestBody 参数校验失败(如 @Valid 注解触发)
     *
     * @param e 方法参数无效异常
     * @return 包含具体字段错误信息的验证失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        // 提取所有字段的错误信息,用逗号分隔
        if (bindingResult.hasErrors()) {
            message = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        log.warn("参数验证失败: {}", message);
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理绑定异常 (BindException)
     * 通常用于 @ModelAttribute 参数绑定失败的情况
     *
     * @param e 绑定异常
     * @return 包含具体字段错误信息的验证失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public CommonResult handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            message = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        log.warn("参数绑定失败: {}", message);
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理约束违反异常 (ConstraintViolationException)
     * 通常用于方法参数或字段级别的约束校验失败(如 @NotNull, @Size 等)
     *
     * @param e 约束违反异常
     * @return 包含约束错误信息的验证失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = ConstraintViolationException.class)
    public CommonResult handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("约束验证失败: {}", message);
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理非法参数异常 (IllegalArgumentException)
     *
     * @param e 非法参数异常
     * @return 包含参数错误信息的失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public CommonResult handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return CommonResult.failed("请求参数错误: " + e.getMessage());
    }

    /**
     * 处理非法状态异常 (IllegalStateException)
     *
     * @param e 非法状态异常
     * @return 包含状态错误信息的失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = IllegalStateException.class)
    public CommonResult handleIllegalStateException(IllegalStateException e) {
        log.error("状态异常: {}", e.getMessage(), e);
        return CommonResult.failed("服务状态异常: " + e.getMessage());
    }

    /**
     * 处理 SQL 语法错误异常 (SQLSyntaxErrorException)
     * 特别针对演示环境下的权限控制提示
     *
     * @param e SQL 语法错误异常
     * @return 包含特定提示信息的失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = SQLSyntaxErrorException.class)
    public CommonResult handleSQLSyntaxErrorException(SQLSyntaxErrorException e) {
        String message = e.getMessage();
        // 检测是否包含权限拒绝相关关键词，如果是演示环境则返回友好提示
        if (StrUtil.isNotEmpty(message) && message.contains("denied")) {
            message = "演示环境暂无修改权限，如需修改数据可本地搭建后台服务！";
        }
        return CommonResult.failed(message);
    }

    /**
     * 处理 AI API 调用异常 (AiApiException)
     * 使用反射方式避免模块间循环依赖
     *
     * @param e 运行时异常对象
     * @return 包含 AI 服务错误信息的失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = RuntimeException.class)
    public CommonResult handleAiApiException(RuntimeException e) {
        String className = e.getClass().getName();
        
        // 检测是否为 AI API 异常
        if ("com.macro.mall.ai.exception.AiApiException".equals(className)) {
            try {
                // 通过反射获取 HTTP 状态码
                java.lang.reflect.Method getHttpStatusCode = e.getClass().getMethod("getHttpStatusCode");
                Integer httpStatusCode = (Integer) getHttpStatusCode.invoke(e);
                
                log.error("AI API 调用失败 [errorCode={}, httpStatus={}]: {}", 
                    e.getClass().getSimpleName(), httpStatusCode, e.getMessage());
                
                // 根据 HTTP 状态码返回不同的提示
                if (httpStatusCode != null) {
                    if (httpStatusCode == 401 || httpStatusCode == 403) {
                        return CommonResult.failed("AI 服务认证失败，请检查 API Key 配置");
                    } else if (httpStatusCode == 429) {
                        return CommonResult.failed("AI 服务请求过于频繁，请稍后重试");
                    } else if (httpStatusCode >= 500) {
                        return CommonResult.failed("AI 服务暂时不可用，请稍后重试");
                    }
                }
                
                return CommonResult.failed("AI 服务调用失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("处理 AI API 异常时出错", ex);
                return CommonResult.failed("AI 服务调用失败");
            }
        }
        
        // 检测是否为 AI 服务异常
        if ("com.macro.mall.ai.exception.AiServiceException".equals(className)) {
            try {
                // 通过反射获取错误码
                java.lang.reflect.Method getErrorCode = e.getClass().getMethod("getErrorCode");
                String errorCode = (String) getErrorCode.invoke(e);
                
                log.warn("AI 业务异常 [errorCode={}]: {}", errorCode, e.getMessage());
                return CommonResult.failed(e.getMessage());
            } catch (Exception ex) {
                log.error("处理 AI 服务异常时出错", ex);
                return CommonResult.failed("AI 服务业务异常");
            }
        }
        
        // 非 AI 异常，继续抛出由其他处理器处理
        throw e;
    }

    /**
     * 处理未捕获的通用异常 (Exception)
     * 作为兜底策略，递归获取根因并返回简洁的错误信息
     *
     * @param e 异常对象
     * @return 包含异常类型和消息的失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public CommonResult handleException(Exception e) {
        // 递归获取根因，避免嵌套异常导致信息冗长
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getClass().getSimpleName() + ": " + root.getMessage();
        return CommonResult.failed(msg);
    }
}
