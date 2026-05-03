package com.macro.mall.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置类 (Global CORS Configuration)
 * <p>
 * 配置跨域资源共享 (Cross-Origin Resource Sharing, CORS) 策略，
 * 允许前端应用（如 mall-admin-web、mall-app-web）跨域访问搜索服务 API。
 * </p>
 * <p>
 * 安全提示：生产环境应限制 allowedOriginPattern 为具体的域名，避免使用 "*" 通配符。
 * </p>
 *
 * @author macro
 * @since 1.0
 */
@Configuration
public class MallCorsConfig {
    /**
     * 创建跨域过滤器 (Create CORS Filter)
     * <p>
     * 配置跨域访问规则，允许所有来源、请求头及 HTTP 方法，
     * 并支持携带凭证（Cookie、Authorization Header 等）。
     * </p>
     *
     * @return CorsFilter 跨域过滤器实例
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
