package com.macro.mall.common.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Swagger 自定义配置属性类 (Swagger Properties)
 * 用于封装 Swagger 文档的元数据信息，如标题、描述、版本等
 */
@Data
@EqualsAndHashCode
@Builder
public class SwaggerProperties {
    /**
     * API 文档生成基础路径（扫描的 Controller 包名）
     */
    private String apiBasePackage;
    /**
     * 是否要启用登录认证（开启后需在请求头携带 Token）
     */
    private boolean enableSecurity;
    /**
     * 文档标题
     */
    private String title;
    /**
     * 文档描述
     */
    private String description;
    /**
     * 文档版本
     */
    private String version;
    /**
     * 文档联系人姓名
     */
    private String contactName;
    /**
     * 文档联系人网址
     */
    private String contactUrl;
    /**
     * 文档联系人邮箱
     */
    private String contactEmail;
}
