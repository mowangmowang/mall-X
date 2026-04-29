package com.macro.mall.security.component;

import com.macro.mall.security.config.IgnoreUrlsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 动态权限过滤器，用于实现基于路径的动态权限过滤
 * <p>
 * 该过滤器继承自 AbstractSecurityInterceptor 并实现了 Filter 接口。
 * 它的主要作用是在请求到达目标资源之前，根据配置动态地判断当前用户是否有权限访问该资源。
 * Created by macro on 2020/2/7.
 */
public class DynamicSecurityFilter extends AbstractSecurityInterceptor implements Filter {

    /**
     * 动态安全元数据源，用于获取受保护资源（URL）与所需权限（角色）之间的映射关系
     */
    @Autowired
    private DynamicSecurityMetadataSource dynamicSecurityMetadataSource;

    /**
     * 忽略URL配置，存储不需要进行权限校验的白名单URL路径
     */
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    /**
     * 设置访问决策管理器
     * Spring Security 需要通过 AccessDecisionManager 来决定用户是否拥有访问资源的权限
     *
     * @param dynamicAccessDecisionManager 自定义的动态访问决策管理器
     */
    @Autowired
    public void setMyAccessDecisionManager(DynamicAccessDecisionManager dynamicAccessDecisionManager) {
        super.setAccessDecisionManager(dynamicAccessDecisionManager);
    }

    /**
     * 过滤器初始化方法
     * 在过滤器创建时调用，通常用于初始化资源，此处无需特殊处理
     *
     * @param filterConfig 过滤器配置对象
     * @throws ServletException 如果发生Servlet相关异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * 核心过滤逻辑处理方法
     * 当请求经过此过滤器时，会执行以下流程：
     * 1. 检查是否为 OPTIONS 请求（跨域预检请求），如果是则直接放行
     * 2. 检查请求路径是否在白名单中，如果在则直接放行
     * 3. 如果以上都不满足，则进行权限校验
     *
     * @param servletRequest  Servlet 请求对象
     * @param servletResponse Servlet 响应对象
     * @param filterChain     过滤器链，用于将请求传递给下一个过滤器或目标资源
     * @throws IOException      如果发生I/O错误
     * @throws ServletException 如果发生Servlet相关异常
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 将 ServletRequest 转换为 HttpServletRequest，以便获取HTTP特定的信息（如请求方法、URI等）
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        // 创建 FilterInvocation 对象，它封装了请求、响应和过滤器链，是 Spring Security 处理Web请求的核心对象
        FilterInvocation fi = new FilterInvocation(servletRequest, servletResponse, filterChain);
        
        // 1. 处理 OPTIONS 请求
        // OPTIONS 请求通常用于 CORS（跨域资源共享）预检，不需要进行权限验证，直接放行
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
            return;
        }
        
        // 2. 处理白名单请求
        // 从配置中获取所有忽略鉴权的URL路径，如果当前请求路径匹配其中任何一个，则直接放行
        PathMatcher pathMatcher = new AntPathMatcher(); // Ant风格的路径匹配器，支持通配符如 ** 和 *
        for (String path : ignoreUrlsConfig.getUrls()) {
            // 使用 pathMatcher 判断当前请求URI是否与白名单中的路径模式匹配
            if (pathMatcher.match(path, request.getRequestURI())) {
                fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
                return;
            }
        }
        
        // 3. 执行权限校验
        // beforeInvocation 方法会触发 SecurityMetadataSource 获取当前URL所需的权限，
        // 然后调用 AccessDecisionManager 进行投票决策，判断当前用户是否有权限访问
        InterceptorStatusToken token = super.beforeInvocation(fi);
        
        try {
            // 如果权限校验通过，继续执行过滤器链中的后续过滤器或目标Controller
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } finally {
            // afterInvocation 方法在请求处理完成后执行，用于清理工作或处理返回结果的安全逻辑
            // 即使发生异常，也需要确保调用 afterInvocation 以保持状态一致
            super.afterInvocation(token, null);
        }
    }

    /**
     * 过滤器销毁方法
     * 在过滤器被容器移除时调用，用于释放资源，此处无需特殊处理
     */
    @Override
    public void destroy() {
    }

    /**
     * 返回受保护对象的类类型
     * 告诉 Spring Security 这个拦截器处理的是哪种类型的对象，这里是 FilterInvocation
     *
     * @return FilterInvocation.class
     */
    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    /**
     * 获取安全元数据源
     * 返回之前注入的 DynamicSecurityMetadataSource 实例，
     * 用于在权限校验时获取当前请求URL对应的配置属性（如所需角色）
     *
     * @return 动态安全元数据源实例
     */
    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return dynamicSecurityMetadataSource;
    }

}
