package com.macro.mall.portal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Swagger API 文档配置类 (Swagger API Documentation Configuration)
 * <p>
 * mall-portal 用户端 API 文档 (基于 springdoc-openapi 3.x).
 * </p>
 * <p>
 * 访问地址：http://localhost:8085/swagger-ui/index.html
 * </p>
 */
@Configuration("portalSwaggerConfig")
public class SwaggerConfig {

    @Bean("portalOpenAPI")
    @Primary
    public OpenAPI portalOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-portal API")
                .description("用户端API文档")
                .version("1.0")
                .contact(new Contact()
                    .name("macro")));
    }
}