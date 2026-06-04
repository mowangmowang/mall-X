package com.macro.mall.security.component;

import cn.hutool.json.JSONUtil;
import com.macro.mall.common.api.CommonResult;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义未登录或者token失效时的返回结果
 * <p>
 * 实现 Spring Security 的 AuthenticationEntryPoint 接口，用于处理认证失败的场景。
 * 当用户访问需要认证的资源但未提供有效的 JWT 令牌 (JWT Token) 时，
 * Spring Security 会调用此类的 commence 方法，返回统一的 JSON 格式错误响应。
 * </p>
 * <p>
 * 典型触发场景：
 * 1. 请求头中缺少 Authorization 字段
 * 2. JWT 令牌已过期
 * 3. JWT 令牌签名无效或被篡改
 * 4. 令牌格式不正确
 * </p>
 * Created by macro
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    /**
     * 处理认证失败的入口方法
     * <p>
     * 当用户尝试访问受保护资源但未能通过身份验证时，Spring Security 会自动调用此方法。
     * 该方法负责向客户端返回标准化的错误响应，而不是默认的 HTTP 401 页面。
     * </p>
     *
     * @param request       HTTP 请求对象，包含请求信息
     * @param response      HTTP 响应对象，用于构建返回给客户端的响应
     * @param authException 认证异常对象，包含失败的具体原因（如：令牌过期、签名无效等）
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 注意：不再手工设置 Access-Control-Allow-Origin 响应头
        // 历史原因：早期 GlobalCorsConfig 未生效时，由这里兜底写入 CORS 头。
        // 现在 CorsConfigurationSource 已在 SecurityConfig.filterChain 中通过 .cors() 接入，
        // 所有响应（包括 401 异常响应）都会经过 CORS 处理，无需也不应在异常处理器中重复注入。
        // 手工注入反而会与 setAllowCredentials(true) 冲突（Origin: * + credentials 会被浏览器拒绝）。

        // 禁止浏览器缓存响应，确保每次都能获取最新的认证状态
        response.setHeader("Cache-Control","no-cache");

        // 设置响应字符编码为 UTF-8，支持中文内容
        response.setCharacterEncoding("UTF-8");

        // 设置响应内容类型为 JSON 格式
        response.setContentType("application/json");

        // 构建统一的错误响应结果
        // CommonResult.unauthorized() 创建标准的未授权响应（HTTP 401）
        // authException.getMessage() 获取具体的错误信息（如："Full authentication is required to access this resource"）
        // JSONUtil.parse() 将 CommonResult 对象序列化为 JSON 字符串
        response.getWriter().println(JSONUtil.parse(CommonResult.unauthorized(authException.getMessage())));

        // 强制刷新输出流，确保响应立即发送给客户端
        response.getWriter().flush();
    }
}

