package com.macro.mall.security.config;

import com.macro.mall.security.component.*;
import com.macro.mall.security.util.JwtTokenUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 通用配置类 (Common Security Configuration)
 * <p>
 * 负责注册 Security 模块所需的所有核心 Bean，包括：
 * 1. 密码加密器
 * 2. JWT 相关组件（令牌工具、认证过滤器）
 * 3. 异常处理器（认证失败、权限不足）
 * 4. 动态权限组件（条件加载，仅在业务模块提供 DynamicSecurityService 时生效）
 * 5. CORS 配置属性绑定（{@link CorsProperties}，绑定 {@code application.yml} 的
 *    {@code mall.security.cors.*} 配置）
 * </p>
 * <p>
 * 本类是 mall-security 模块对外暴露的"门面"，业务模块（mall-admin / mall-portal）通过
 * Maven 依赖继承即可获得所有安全相关 Bean，无需各自实现。
 * </p>
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CommonSecurityConfig {

    /**
     * 注册密码加密器 Bean
     * 使用 BCrypt 算法对密码进行单向加密，确保用户密码安全存储
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注册白名单 URL 配置 Bean
     * 从配置文件读取 secure.ignored.urls 属性，存储不需要鉴权的路径
     *
     * @return IgnoreUrlsConfig 实例
     */
    @Bean
    public IgnoreUrlsConfig ignoreUrlsConfig() {
        return new IgnoreUrlsConfig();
    }

    /**
     * 注册 JWT 令牌工具 Bean
     * 提供令牌的生成、解析、验证等功能
     *
     * @return JwtTokenUtil 实例
     */
    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }

    /**
     * 注册权限不足处理器 Bean
     * 当已登录用户访问无权访问的资源时，返回 HTTP 403 响应
     *
     * @return RestfulAccessDeniedHandler 实例
     */
    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler() {
        return new RestfulAccessDeniedHandler();
    }

    /**
     * 注册认证失败入口点 Bean
     * 当用户未登录或令牌失效时，返回 HTTP 401 响应
     *
     * @return RestAuthenticationEntryPoint 实例
     */
    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    /**
     * 注册 JWT 认证过滤器 Bean
     * 拦截请求并验证 JWT 令牌，将用户信息存入 SecurityContext
     *
     * @return JwtAuthenticationTokenFilter 实例
     */
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter(){
        return new JwtAuthenticationTokenFilter();
    }

    /**
     * 注册动态权限决策管理器 Bean（条件加载）
     * 仅当容器中存在名为 "dynamicSecurityService" 的 Bean 时才创建
     *
     * @return DynamicAccessDecisionManager 实例
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicAccessDecisionManager dynamicAccessDecisionManager() {
        return new DynamicAccessDecisionManager();
    }

    /**
     * 注册动态权限数据源 Bean（条件加载）
     * 仅当容器中存在名为 "dynamicSecurityService" 的 Bean 时才创建
     *
     * @return DynamicSecurityMetadataSource 实例
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicSecurityMetadataSource dynamicSecurityMetadataSource() {
        return new DynamicSecurityMetadataSource();
    }

    /**
     * 注册动态权限过滤器 Bean（条件加载）
     * 仅当容器中存在名为 "dynamicSecurityService" 的 Bean 时才创建
     *
     * @return DynamicSecurityFilter 实例
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicSecurityFilter dynamicSecurityFilter(){
        return new DynamicSecurityFilter();
    }
}

