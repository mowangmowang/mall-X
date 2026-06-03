package com.macro.mall.common.api;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.api.IErrorCode;
import com.macro.mall.common.api.ResultCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommonResultTest {

    @Test
    void successWithData() {
        CommonResult result = CommonResult.success("data");
        assertEquals(200, result.getCode());
        assertEquals("data", result.getData());
    }

    @Test
    void successWithMessage() {
        CommonResult result = CommonResult.success("操作成功");
        assertEquals(200, result.getCode());
    }

    @Test
    void failedWithErrorCode() {
        CommonResult result = CommonResult.failed(ResultCode.FAILED);
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMessage());
    }

    @Test
    void failedWithMessage() {
        CommonResult result = CommonResult.failed("系统错误");
        assertEquals(500, result.getCode());
        assertEquals("系统错误", result.getMessage());
    }

    @Test
    void unauthorized() {
        CommonResult result = CommonResult.unauthorized("未登录");
        assertEquals(401, result.getCode());
    }

    @Test
    void forbidden() {
        CommonResult result = CommonResult.forbidden("无权限");
        assertEquals(403, result.getCode());
    }
}