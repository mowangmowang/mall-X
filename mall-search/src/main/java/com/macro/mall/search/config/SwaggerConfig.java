package com.macro.mall.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Swagger API 文档配置类 (Swagger API Documentation Configuration)
 * <p>
 * 配置并启用 Swagger2，自动生成搜索服务的 RESTful API 接口文档，
 * 便于开发人员测试和调试 API 接口。
 * </p>
 * <p>
 * 访问地址：http://localhost:8081/swagger-ui/index.html
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Configuration("searchSwaggerConfig")
public class SwaggerConfig {

    @Bean("searchOpenAPI")
    @Primary
    public OpenAPI searchOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-search API")
                .description("商品搜索API文档")
                .version("1.0"));
    }
}