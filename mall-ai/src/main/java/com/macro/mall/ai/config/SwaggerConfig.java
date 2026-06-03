package com.macro.mall.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Swagger API 文档配置类 (Swagger API Documentation Configuration)
 * <p>
 * 配置 AI 助手服务的 OpenAPI 3 文档。
 * </p>
 * <p>
 * 访问地址：http://localhost:8086/swagger-ui/index.html
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Configuration("aiSwaggerConfig")
public class SwaggerConfig {

    @Bean("aiOpenAPI")
    @Primary
    public OpenAPI aiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-ai API")
                .description("AI购物助手API文档")
                .version("1.0"));
    }
}