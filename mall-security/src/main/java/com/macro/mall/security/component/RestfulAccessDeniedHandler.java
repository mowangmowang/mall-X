package com.macro.mall.security.component;

import cn.hutool.json.JSONUtil;
import com.macro.mall.common.api.CommonResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义无权限访问的返回结果
 * <p>
 * 实现 Spring Security 的 AccessDeniedHandler 接口，用于处理授权失败的场景。
 * 当用户已通过身份验证（有有效的 JWT 令牌），但尝试访问没有权限的资源时，
 * Spring Security 会调用此类的 handle 方法，返回 HTTP 403 禁止访问响应。
 * </p>
 * <p>
 * 典型触发场景：
 * 1. 普通用户尝试访问管理员接口
 * 2. 用户角色权限不足（如只有读取权限却尝试删除数据）
 * 3. 访问未分配给当前角色的受保护资源
 * </p> */
public class RestfulAccessDeniedHandler implements AccessDeniedHandler{
    
    /**
     * 处理访问被拒绝的方法
     * <p>
     * 当已认证用户尝试访问无权访问的资源时，Spring Security 会自动调用此方法。
     * 与 RestAuthenticationEntryPoint 的区别：
     * - AuthenticationEntryPoint：用户未登录或令牌无效（HTTP 401）
     * - AccessDeniedHandler：用户已登录但权限不足（HTTP 403）
     * </p>
     *
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param e        访问拒绝异常，包含具体的权限不足信息
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException, ServletException {
        // 设置跨域资源共享 (CORS) 允许所有来源访问
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        // 禁止浏览器缓存响应
        response.setHeader("Cache-Control","no-cache");
        
        // 设置响应字符编码为 UTF-8
        response.setCharacterEncoding("UTF-8");
        
        // 设置响应内容类型为 JSON 格式
        response.setContentType("application/json");
        
        // 构建统一的 403 禁止访问响应
        // CommonResult.forbidden() 创建标准的权限不足响应（HTTP 403）
        // e.getMessage() 获取具体的权限拒绝原因
        response.getWriter().println(JSONUtil.parse(CommonResult.forbidden(e.getMessage())));
        
        // 强制刷新输出流，确保响应立即发送
        response.getWriter().flush();
    }
}
