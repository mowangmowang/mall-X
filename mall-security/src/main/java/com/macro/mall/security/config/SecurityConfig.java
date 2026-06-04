package com.macro.mall.security.config;

import com.macro.mall.security.component.DynamicSecurityFilter;
import com.macro.mall.security.component.DynamicSecurityService;
import com.macro.mall.security.component.JwtAuthenticationTokenFilter;
import com.macro.mall.security.component.RestAuthenticationEntryPoint;
import com.macro.mall.security.component.RestfulAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security 核心配置类 (Security Configuration)
 * <p>
 * 负责构建并配置 Spring Security 6 的安全过滤链 (SecurityFilterChain)，定义以下安全策略：
 * <ol>
 *   <li>启用 CORS（关键修复点：从 {@code CorsConfigurationSource} Bean 加载策略）</li>
 *   <li>白名单路径放行（无需认证）</li>
 *   <li>CORS 预检 (OPTIONS) 放行</li>
 *   <li>JWT 令牌验证</li>
 *   <li>动态权限校验（可选）</li>
 *   <li>异常处理（认证失败、权限不足）</li>
 *   <li>无状态会话管理</li>
 * </ol>
 * </p>
 * <p>
 * <b>Spring Security 6 升级要点</b>（与 5.x 的关键差异）：
 * <ul>
 *   <li>{@code authorizeRequests()} 已弃用，改用 {@code authorizeHttpRequests()}</li>
 *   <li>链式 {@code .and()} 调用已弃用，改用 Lambda DSL（{@code .csrf(c -> ...)}）</li>
 *   <li>{@code FilterSecurityInterceptor} 已弃用，由 {@code AuthorizationFilter} 取代</li>
 *   <li>CORS 必须显式调用 {@code .cors(...)} 才会进入 Security 过滤器链，
 *       仅注册 {@code CorsFilter} Bean 已不再生效</li>
 * </ul>
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
     * 当用户未登录或 Token 失效时，使用的自定义认证入口点（返回 401 错误）
     */
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    /**
     * JWT 认证过滤器，用于解析请求头中的 Token 并验证用户身份
     */
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * CORS 跨域策略源
     * <p>
     * 显式注入而非依赖 {@code Customizer.withDefaults()} 的自动发现，原因：
     * <ul>
     *   <li>某些 Spring Security 6.x 版本下 {@code Customizer.withDefaults()} 对 CORS 的处理
     *       不稳定（取决于内部 Bean 查找顺序），显式注入保证可靠性</li>
     *   <li>便于单元测试和依赖追踪</li>
     * </ul>
     * </p>
     */
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * 动态权限服务，用于从数据库加载权限规则（可选组件）
     * <p>
     * {@code required = false} 表示该依赖是可选的：如果容器中存在 {@code DynamicSecurityService}
     * 的 Bean 则注入，否则忽略而不报错。
     * </p>
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
     * CORS 拦截 → 白名单检查 → OPTIONS 预检放行 → JWT 认证 → 动态权限校验 → 目标资源
     * </p>
     *
     * @param httpSecurity HttpSecurity 对象，用于配置安全策略
     * @return SecurityFilterChain 构建完成的安全过滤链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 收集所有需要 permitAll 的 RequestMatcher（白名单 + OPTIONS 预检）
        // 一次性传入 requestMatchers(...) 比循环调用更清晰，也避免在 DSL 中出现副作用
        List<RequestMatcher> permitAllMatchers = new ArrayList<>();
        // 步骤1：放行 CORS 预检请求
        // 修复点：原代码 AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS.toString()) 把
        // "OPTIONS" 当成了路径模式，导致所有 OPTIONS 请求都未被 permitAll。正确做法是
        // 显式传入 HTTP method + 路径通配符。
        permitAllMatchers.add(AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS, "/**"));
        // 步骤2：放行白名单（登录接口、静态资源、Swagger、Actuator 等）
        for (String url : ignoreUrlsConfig.getUrls()) {
            permitAllMatchers.add(AntPathRequestMatcher.antMatcher(url));
        }

        httpSecurity
                // 步骤3：启用 CORS（关键修复点）
                // 显式传入 corsConfigurationSource 而非依赖 Customizer.withDefaults()，
                // 确保 Spring Security 6 一定会将我们配置的 CorsConfigurationSource 接入
                // Security 过滤器链。如果不调用此方法，Spring Security 6 不会处理跨域，
                // 浏览器预检 OPTIONS 请求会被 AuthorizationFilter 拦截并返回 401/403。
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 步骤4：关闭 CSRF 防护
                // 原因：本项目使用 JWT 无状态认证，不依赖 Session，CSRF 攻击无法利用 Cookie
                .csrf(AbstractHttpConfigurer::disable)
                // 步骤5：配置无状态会话管理
                // STATELESS 表示 Spring Security 不会创建或使用 HttpSession，每次请求都需携带 Token
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 步骤6：URL 授权规则（白名单 + 预检放行，其余必须认证）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permitAllMatchers.toArray(new RequestMatcher[0])).permitAll()
                        .anyRequest().authenticated())
                // 步骤7：异常处理器
                // 7a. 当已登录用户访问无权资源时，调用自定义的 403 处理器
                // 7b. 当用户未登录或 Token 失效时，调用自定义的 401 入口点
                .exceptionHandling(eh -> eh
                        .accessDeniedHandler(restfulAccessDeniedHandler)
                        .authenticationEntryPoint(restAuthenticationEntryPoint))
                // 步骤8：添加 JWT 认证过滤器
                // 将过滤器插入到 UsernamePasswordAuthenticationFilter 之前执行
                // 确保在进入 Spring Security 默认认证流程前完成 Token 验证
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 步骤9：条件加载动态权限过滤器
        // 若业务模块提供了 DynamicSecurityService，则启用基于数据库的动态权限控制
        // 该过滤器会在 AuthorizationFilter 之前执行（Spring Security 6 中已用
        // AuthorizationFilter 取代 FilterSecurityInterceptor），实现细粒度的 URL 级别权限校验
        if (dynamicSecurityService != null) {
            httpSecurity.addFilterBefore(dynamicSecurityFilter, AuthorizationFilter.class);
        }

        return httpSecurity.build();
    }
}
