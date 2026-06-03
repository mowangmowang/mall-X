package com.macro.mall.common.exception;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.api.ResultCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleApiExceptionWithErrorCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiException e = new ApiException(ResultCode.FAILED);
        CommonResult result = handler.handle(e);
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMessage());
    }

    @Test
    void handleApiExceptionWithMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiException e = new ApiException("资源不存在");
        CommonResult result = handler.handle(e);
        assertEquals(500, result.getCode());
        assertEquals("资源不存在", result.getMessage());
    }

    @Test
    void handleException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception e = new Exception("系统错误");
        CommonResult result = handler.handleException(e);
        assertEquals(500, result.getCode());
    }
}