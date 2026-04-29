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
 * 用于统一处理系统中抛出的各类异常，并返回标准化的响应结果
 * Created by macro
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常 (ApiException)
     *
     * @param e 自定义异常对象
     * @return 包含错误码或错误信息的通用响应结果,用于封装错误信息
     */
    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public CommonResult handle(ApiException e) {
        // errorCode不为空则返回错误码，否则返回错误信息
        if (e.getErrorCode() != null) {
            return CommonResult.failed(e.getErrorCode());
        }
        // 返回错误信息
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理请求参数校验异常 (MethodArgumentNotValidException)
     * 通常用于 @RequestBody 参数校验失败的情况
     *
     * @param e 方法参数无效异常
     * @return 包含具体字段错误信息的验证失败响应,用于封装字段错误信息
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult(); // 获取绑定结果
        String message = null;
        // 获取第一个错误信息
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError(); // 获取第一个字段错误
            if (fieldError != null) {
                // 拼接字段名和默认错误消息
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
     * @return 包含具体字段错误信息的验证失败响应,用于封装字段错误信息
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
     * @return 包含特定提示信息的失败响应,用于封装错误信息
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

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public CommonResult handleException(Exception e) {
        // 递归获取根因
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getClass().getSimpleName() + ": " + root.getMessage();
        return CommonResult.failed(msg);
    }
}
