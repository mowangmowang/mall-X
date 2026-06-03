package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

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
    @ApiModelProperty(value = "用户名", required = true)
    private String username;
    
    /**
     * 密码（必填）
     */
    @NotEmpty
    @ApiModelProperty(value = "密码", required = true)
    private String password;
}
