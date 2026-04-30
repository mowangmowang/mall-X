package com.macro.mall.search.config;

import com.macro.mall.common.config.BaseSwaggerConfig;
import com.macro.mall.common.domain.SwaggerProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger API 文档配置类 (Swagger Configuration)
 * 用于生成和展示搜索服务的 RESTful API 接口文档
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig extends BaseSwaggerConfig {

    /**
     * 配置 Swagger 文档属性
     * @return SwaggerProperties 配置对象
     */
    @Override
    public SwaggerProperties swaggerProperties() {
        return SwaggerProperties.builder()
                .apiBasePackage("com.macro.mall.search.controller")  // 扫描的 Controller 包
                .title("mall搜索系统")  // 文档标题
                .description("mall搜索相关接口文档")  // 文档描述
                .contactName("macro")  // 联系人
                .version("1.0")  // API 版本
                .enableSecurity(false)  // 禁用安全认证（开发环境）
                .build();
    }

    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return generateBeanPostProcessor();
    }
}
