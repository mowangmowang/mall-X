package com.macro.mall.search.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 过滤器显式注册（搜索服务版）
 * <p>
 * mall-search 不依赖 mall-security，没有 Security 过滤器链消费
 * {@link CorsConfigurationSource}，因此需要显式包装为 servlet filter。
 * </p>
 * <p>
 * <b>为什么不用 {@code CorsFilter} Bean 直接注册</b>：Spring Boot 3 自动注册
 * {@code CorsFilter} Bean 的 order 是 {@code LOWEST_PRECEDENCE}（最后执行），
 * 但预检 OPTIONS 必须在 Spring MVC DispatcherServlet 之前处理，
 * 否则 Spring MVC 会因找不到 handler 路由返回 404/500。手动用
 * {@code FilterRegistrationBean} 设置 {@code HIGHEST_PRECEDENCE} 是最稳的做法。
 * </p>
 * <p>
 * CORS 策略来源：{@code com.macro.mall.cors.CorsConfig#corsConfigurationSource}
 * （mall-common-cors 模块，所有 4 个服务共享）
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@Configuration
public class CorsFilterRegistration {

    /**
     * 显式注册 CORS 过滤器，order = HIGHEST_PRECEDENCE
     *
     * @param source mall-common-cors 提供的共享 CORS 策略源
     * @return FilterRegistrationBean 实例
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource source) {
        CorsFilter filter = new CorsFilter(source);
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
