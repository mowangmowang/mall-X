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
 * SpringSecurity相关配置，仅用于配置SecurityFilterChain
 * 主要实现filterChain，即Spring Security的安全过滤链的配置
 * Created by macro .
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
     * 配置Spring Security的安全过滤链
     *
     * @param httpSecurity HttpSecurity对象，用于配置安全策略
     * @return SecurityFilterChain 构建好的安全过滤链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 获取URL授权配置注册表，用于配置哪些URL需要什么样的权限
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity
                .authorizeRequests();

        // 1. 配置不需要保护的资源路径（白名单），允许所有用户直接访问
        // 例如：登录接口、注册接口、静态资源文件等
        for (String url : ignoreUrlsConfig.getUrls()) {
            registry.antMatchers(url).permitAll();
        }

        // 2. 允许跨域请求的OPTIONS预检请求直接通过
        // 前端发送跨域请求时，浏览器会先发送一个OPTIONS请求询问服务器是否允许，这里放行该请求
        registry.antMatchers(HttpMethod.OPTIONS)
                .permitAll();

        // 3. 配置其他安全策略
        registry.and()
                // 除了上面配置的白名单路径外，任何其他请求都需要进行身份认证
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                
                // 4. 关闭CSRF（跨站请求伪造）防护
                // 因为本项目使用JWT无状态认证，不依赖Session，所以可以关闭CSRF防护
                .and()
                .csrf()
                .disable()
                
                // 5. 配置会话管理策略为无状态（STATELESS）
                // 表示Spring Security不会创建或使用HttpSession来保存用户状态，每次请求都需要携带Token
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                
                // 6. 配置异常处理机制
                .and()
                .exceptionHandling()
                // 当用户访问没有权限的资源时，调用自定义的拒绝处理器
                .accessDeniedHandler(restfulAccessDeniedHandler)
                // 当用户未认证或认证失败时，调用自定义的认证入口点
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                
                // 7. 添加JWT认证过滤器
                // 将jwtAuthenticationTokenFilter添加到UsernamePasswordAuthenticationFilter之前执行
                // 这样可以在进入Spring Security默认的用户名密码认证流程之前，先完成JWT Token的校验
                .and()
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 8. 如果配置了动态权限服务，则添加动态权限校验过滤器
        // 动态权限过滤器会在FilterSecurityInterceptor之前执行，实现基于数据库的动态权限控制
        if(dynamicSecurityService!=null){
            registry.and().addFilterBefore(dynamicSecurityFilter, FilterSecurityInterceptor.class);
        }

        // 构建并返回安全过滤链
        return httpSecurity.build();
    }

}
