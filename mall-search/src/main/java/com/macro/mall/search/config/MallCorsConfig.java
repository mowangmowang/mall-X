package com.macro.mall.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置类 (CORS Configuration)
 * 允许前端应用跨域访问搜索服务 API
 */
@Configuration
public class MallCorsConfig {
    /**
     * 创建跨域过滤器 (CORS Filter)
     * @return CorsFilter 过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);  // 允许携带凭证（Cookie）
        config.addAllowedOriginPattern("*");  // 允许所有来源
        config.addAllowedHeader("*");  // 允许所有请求头
        config.addAllowedMethod("*");  // 允许所有 HTTP 方法（GET, POST, PUT, DELETE 等）
        source.registerCorsConfiguration("/**", config);  // 应用到所有路径
        return new CorsFilter(source);
    }
}
