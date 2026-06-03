package com.macro.mall.search.config;

import com.macro.mall.common.config.BaseSwaggerConfig;
import com.macro.mall.common.domain.SwaggerProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger API 文档配置类 (Swagger API Documentation Configuration)
 * <p>
 * 配置并启用 Swagger2，自动生成搜索服务的 RESTful API 接口文档，
 * 便于开发人员测试和调试 API 接口。
 * </p>
 * <p>
 * 访问地址：http://localhost:8085/swagger-ui.html
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig extends BaseSwaggerConfig {

    /**
     * 配置 Swagger 文档属性 (Configure Swagger Properties)
     * <p>
     * 设置 API 文档的标题、描述、版本等元数据信息，
     * 并指定扫描的 Controller 包路径。
     * </p>
     *
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
