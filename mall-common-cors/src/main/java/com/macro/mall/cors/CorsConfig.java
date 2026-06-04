package com.macro.mall.cors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS 跨域策略统一配置（mall-common-cors 模块）
 * <p>
 * 提供 {@link CorsConfigurationSource} Bean，供 4 个服务消费：
 * <ul>
 *   <li>mall-admin / mall-portal：通过 {@code SecurityConfig.filterChain().cors(...)} 接入 Security 链</li>
 *   <li>mall-search / mall-ai：通过 {@code FilterRegistrationBean} 包装为 servlet filter</li>
 * </ul>
 * </p>
 * <p>
 * <b>关于"*" + credentials 的处理</b>：当 allowedOrigins 仅一项为 "*" 时，
 * 自动改用 {@code addAllowedOriginPattern}，符合 RFC 6454 允许 credentials + 通配符 origin 的要求。
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    /**
     * 构建 CORS 策略源
     *
     * @param props 跨域策略配置（绑定 application.yml 的 mall.security.cors.*）
     * @return CorsConfigurationSource 实例，供各服务以不同方式消费
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();

        // 1. 允许的来源：单条 "*" 时用 addAllowedOriginPattern 兼容 credentials
        List<String> origins = props.getAllowedOrigins();
        if (origins != null && origins.size() == 1 && "*".equals(origins.get(0))) {
            cfg.addAllowedOriginPattern("*");
        } else if (origins != null) {
            cfg.setAllowedOrigins(origins);
        }

        // 2. 允许的 HTTP 方法
        cfg.setAllowedMethods(props.getAllowedMethods());

        // 3. 允许的请求头（Authorization 必须放行或用通配符）
        cfg.setAllowedHeaders(props.getAllowedHeaders());

        // 4. 是否携带凭证
        cfg.setAllowCredentials(props.getAllowCredentials());

        // 5. 预检缓存时间
        cfg.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
