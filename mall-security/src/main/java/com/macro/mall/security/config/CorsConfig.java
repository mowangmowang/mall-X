package com.macro.mall.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 全局 CORS 配置（Spring Security 6 推荐写法）
 * <p>
 * 关键点：与 Spring Security 5.x 不同，6.x 不会自动从 {@code CorsFilter} Bean 推断 CORS，
 * 必须显式提供 {@link CorsConfigurationSource} Bean，并在 SecurityFilterChain 中调用
 * {@code http.cors(...)}，否则 Spring Security 不会处理跨域。
 * </p>
 * <p>
 * 本类被 mall-admin / mall-portal 通过依赖继承获得；mall-search / mall-ai 因不依赖
 * mall-security，保留各自的本地副本（{@code MallCorsConfig}），结构与本类保持一致。
 * </p>
 * <p>
 * 为什么不用 {@code CorsFilter} Bean：Spring Security 6 中，注册 {@code CorsFilter} 会被
 * 自动作为 Servlet 过滤器运行在 Security 过滤器链之前，造成与 {@code AuthorizationFilter}
 * 之间的边界条件（预检后带 Authorization 头进入 Security 链时行为不一致）。
 * {@code CorsConfigurationSource} + {@code http.cors()} 是官方推荐路径。
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@Configuration
public class CorsConfig {

    /**
     * 构建 CORS 策略源
     * <p>
     * Spring Security 通过此 Bean 在过滤器链中拦截 CORS 预检请求 (OPTIONS)，
     * 并根据返回值决定在响应头中写入哪些 CORS 头。
     * </p>
     *
     * @param props 跨域策略配置（绑定 application.yml 的 mall.security.cors.*）
     * @return CorsConfigurationSource 实例
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();

        // 1. 允许的来源：使用通配符 "*" 时必须用 addAllowedOriginPattern（兼容 credentials）
        List<String> origins = props.getAllowedOrigins();
        if (origins != null && origins.size() == 1 && "*".equals(origins.get(0))) {
            cfg.addAllowedOriginPattern("*");
        } else if (origins != null) {
            cfg.setAllowedOrigins(origins);
        }

        // 2. 允许的 HTTP 方法（GET / POST / PUT / DELETE 等）
        cfg.setAllowedMethods(props.getAllowedMethods());

        // 3. 允许的请求头（含 Authorization、Content-Type 等）
        //    关键：Authorization 头必须在此列中或使用通配符，否则浏览器预检失败
        cfg.setAllowedHeaders(props.getAllowedHeaders());

        // 4. 是否允许携带凭证（Cookie / Authorization）
        cfg.setAllowCredentials(props.getAllowCredentials());

        // 5. 预检缓存时间（秒），减少 OPTIONS 请求频次
        cfg.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
