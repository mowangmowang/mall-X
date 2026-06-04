package com.macro.mall.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 搜索服务 CORS 配置
 * <p>
 * 注意：mall-search 不依赖 mall-security（搜索服务面向内网调用为主，不强制鉴权），
 * 故本地保留一份 CORS 配置。结构与 mall-security 的 CorsConfig 保持完全一致，
 * 后续如需统一，可将 CORS 规则下沉到 mall-common 模块。
 * </p>
 * <p>
 * 本类从 {@code CorsFilter} Bean 模式迁移到 {@code CorsConfigurationSource} Bean 模式，
 * 与 Spring Security 6 推荐的配置风格保持一致。Spring Boot 会自动将本 Bean 注入到
 * 内置的 MVC CORS 处理（{@code WebMvcConfigurer.addCorsMappings} 行为相同）。
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@Configuration
public class MallCorsConfig {

    /**
     * 创建跨域策略源（搜索服务版）
     * <p>
     * 与 mall-security 中 CorsConfig 的策略保持完全一致：
     * - 允许所有来源（生产环境必须改为具体域名）
     * - 允许携带凭证（Cookie / Authorization）
     * - 允许所有请求头（含 Authorization）
     * - 允许所有 HTTP 方法
     * - 预检结果缓存 3600 秒
     * </p>
     *
     * @return CorsConfigurationSource 实例
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
