package com.macro.mall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Swagger API 文档配置类 (Swagger API Documentation Configuration)
 * <p>
 * mall-admin 后台管理 API 文档 (基于 springdoc-openapi 3.x).
 * </p>
 * <p>
 * 访问地址：http://localhost:8080/swagger-ui/index.html
 * </p>
 */
@Configuration("adminSwaggerConfig")
public class SwaggerConfig {

    @Bean("adminOpenAPI")
    @Primary
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-admin API")
                .description("后台管理系统API文档")
                .version("1.0")
                .contact(new Contact()
                    .name("macro")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}