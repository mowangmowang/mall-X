package com.macro.mall.common.config;

import com.macro.mall.common.domain.SwaggerProperties;

/**
 * Swagger基础配置抽象类
 * 已迁移至springdoc-openapi，SwaggerConfig使用OpenAPI Bean进行配置
 */
public abstract class BaseSwaggerConfig {

    public abstract SwaggerProperties swaggerProperties();
}