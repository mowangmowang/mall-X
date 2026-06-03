package com.macro.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;

/**
 * 管理员登录参数 DTO
 * 用于接收前端传递的登录凭据
 */
@Data
@EqualsAndHashCode
public class UmsAdminLoginParam {
    /**
     * 用户名（必填）
     */
    @NotEmpty
    @Schema(description = "用户名", required = true)
    private String username;
    
    /**
     * 密码（必填）
     */
    @NotEmpty
    @Schema(description = "密码", required = true)
    private String password;
}