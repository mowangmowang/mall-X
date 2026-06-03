package com.macro.mall.security.config;

import com.macro.mall.security.component.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Spring Security 核心配置类 (Security Configuration)
 * <p>
 * 负责构建和配置 Spring Security 的安全过滤链 (SecurityFilterChain)，定义以下安全策略：
 * 1. 白名单路径放行（无需认证）
 * 2. JWT 令牌验证
 * 3. 动态权限校验（可选）
 * 4. 异常处理（认证失败、权限不足）
 * 5. 无状态会话管理
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 用于获取不需要安全认证的资源路径配置（如登录接口、静态资源等）
     */
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    /**
     * 当用户访问没有权限的资源时，使用的自定义拒绝处理器
     */
    @Autowired
    private RestfulAccessDeniedHandler restfulAccessDeniedHandler;

    /**
     * 当用户未登录或Token失效时，使用的自定义认证入口点（返回401错误）
     */
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    /**
     * JWT认证过滤器，用于解析请求头中的Token并验证用户身份
     */
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 动态权限服务，用于从数据库加载权限规则（可选组件）
     * required = false 表示该依赖是可选的：如果容器中存在 DynamicSecurityService 的 Bean 则注入，否则忽略而不报错
     */
    @Autowired(required = false)
    private DynamicSecurityService dynamicSecurityService;

    /**
     * 动态权限过滤器，用于根据动态权限规则进行拦截校验（可选组件）
     */
    @Autowired(required = false)
    private DynamicSecurityFilter dynamicSecurityFilter;

    /**
     * 构建并配置 Spring Security 安全过滤链
     * <p>
     * 该方法定义了完整的请求处理流程：
     * 白名单检查 → OPTIONS 预检放行 → JWT 认证 → 动态权限校验 → 目标资源
     * </p>
     *
     * @param httpSecurity HttpSecurity 对象，用于配置安全策略
     * @return SecurityFilterChain 构建完成的安全过滤链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 获取 URL 授权配置注册表，用于声明式地配置 URL 访问规则
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity
                .authorizeRequests();

        // ========== 步骤1：配置白名单路径 ==========
        // 允许所有用户（包括未登录用户）直接访问这些路径
        // 典型场景：登录接口、注册接口、静态资源、API 文档等
for (String url : ignoreUrlsConfig.getUrls()) {
            registry.requestMatchers(org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher(url)).permitAll();
        }

        // ========== 步骤2：放行 CORS 预检请求 ==========
        registry.requestMatchers(org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS.toString()))
                .permitAll();

        // ========== 步骤3-7：配置其他安全策略 ==========
        registry.and()
                // 步骤3：除白名单外，所有请求都需要身份认证
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                
                // 步骤4：关闭 CSRF 防护
                // 原因：本项目使用 JWT 无状态认证，不依赖 Session，CSRF 攻击无法利用 Cookie
                .and()
                .csrf()
                .disable()
                
                // 步骤5：配置无状态会话管理
                // STATELESS 表示 Spring Security 不会创建或使用 HttpSession，每次请求都需携带 Token
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                
                // 步骤6：配置异常处理器
                .and()
                .exceptionHandling()
                // 当已登录用户访问无权资源时，调用自定义的 403 处理器
                .accessDeniedHandler(restfulAccessDeniedHandler)
                // 当用户未登录或 Token 失效时，调用自定义的 401 入口点
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                
                // 步骤7：添加 JWT 认证过滤器
                // 将过滤器插入到 UsernamePasswordAuthenticationFilter 之前执行
                // 确保在进入 Spring Security 默认认证流程前完成 Token 验证
                .and()
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // ========== 步骤8：条件加载动态权限过滤器 ==========
        // 若业务模块提供了 DynamicSecurityService，则启用基于数据库的动态权限控制
        // 该过滤器会在 FilterSecurityInterceptor 之前执行，实现细粒度的 URL 级别权限校验
        if(dynamicSecurityService!=null){
            registry.and().addFilterBefore(dynamicSecurityFilter, FilterSecurityInterceptor.class);
        }

        // 构建并返回最终的安全过滤链
        return httpSecurity.build();
    }

}

