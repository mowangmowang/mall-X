package com.macro.mall.common.exception;

import cn.hutool.core.util.StrUtil;
import com.macro.mall.common.api.CommonResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLSyntaxErrorException;

/**
 * 全局异常处理类 (Global Exception Handler)
 * 使用 @ControllerAdvice 统一拦截并处理系统中抛出的各类异常
 * 返回标准化的 CommonResult 响应结果
 * Created by macro
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常 (ApiException)
     *
     * @param e 自定义异常对象
     * @return 包含错误码或错误信息的通用响应结果
     */
    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public CommonResult handle(ApiException e) {
        // 优先使用错误码，若无则使用纯文本消息
        if (e.getErrorCode() != null) {
            return CommonResult.failed(e.getErrorCode());
        }
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理请求参数校验异常 (MethodArgumentNotValidException)
     * 通常用于 @RequestBody 参数校验失败（如 @Valid 注解触发）
     *
     * @param e 方法参数无效异常
     * @return 包含具体字段错误信息的验证失败响应
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        // 提取第一个字段的错误信息
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                // 拼接字段名和默认错误消息，例如："username不能为空"
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
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
    public CommonResult handleValidException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                // 拼接字段名和默认错误消息
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return CommonResult.validateFailed(message);
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
