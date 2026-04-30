package com.macro.mall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域 (CORS) 配置类
 * 允许前端应用跨域访问后台 API 接口
 * 注意：生产环境应限制具体的域名，而非使用通配符 "*"
 */
@Configuration
public class GlobalCorsConfig {

    /**
     * 配置并注册跨域过滤器
     * 允许所有来源、所有方法、所有请求头的跨域请求
     * @return CorsFilter 跨域过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名进行跨域调用（生产环境建议指定具体域名）
        // 有安全问题，理论上应该填写指定的域名，如 http://api.example.com
        // 可以写到配置文件，读取配置文件
        config.addAllowedOriginPattern("*");
        // 允许跨越发送 cookie（用于携带认证信息）
        config.setAllowCredentials(true);
        // 放行全部原始头信息
        config.addAllowedHeader("*");
        // 允许所有请求方法跨域调用（GET, POST, PUT, DELETE等）
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用跨域配置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
