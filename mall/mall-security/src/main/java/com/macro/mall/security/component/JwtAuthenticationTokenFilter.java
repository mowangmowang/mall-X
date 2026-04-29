package com.macro.mall.security.component;

import com.macro.mall.security.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT登录授权过滤器
 * <p>
 * 继承自 OncePerRequestFilter，确保每个请求只执行一次过滤逻辑。
 * 主要职责：从 HTTP 请求头中提取 JWT 令牌 (JWT Token)，验证用户身份，
 * 并将认证信息存入 Spring Security 上下文 (Security Context)。
 * </p> */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);
    
    /**
     * 用户详情服务，用于根据用户名加载用户信息及权限
     */
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * JWT 令牌工具类，提供令牌解析、验证等功能
     */
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    /**
     * JWT 令牌在请求头中的键名（如：Authorization）
     * 从配置文件中读取：jwt.tokenHeader
     */
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    
    /**
     * JWT 令牌前缀（如：Bearer ）
     * 从配置文件中读取：jwt.tokenHead
     */
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    /**
     * 核心过滤逻辑：处理每个 HTTP 请求的 JWT 认证
     *
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param chain    过滤器链，用于将请求传递给下一个过滤器或目标资源
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 从请求头中获取 JWT 令牌（格式通常为："Bearer xxxxx.yyyyy.zzzzz"）
        String authHeader = request.getHeader(this.tokenHeader);
        
        // 判断请求头是否存在且以指定的前缀开头（如 "Bearer "）
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            // 提取令牌字符串（去除 "Bearer " 前缀）
            String authToken = authHeader.substring(this.tokenHead.length());
            
            // 从令牌中解析用户名
            String username = jwtTokenUtil.getUserNameFromToken(authToken);
            LOGGER.info("checking username:{}", username);
            
            // 如果用户名不为空，且当前安全上下文中没有认证信息（未登录状态，避免重复认证）
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 根据用户名加载用户详细信息（包括密码、权限等）
                //此处会从数据库中获取登录用户信息，使用AOP实现redis缓存
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // 验证令牌是否有效（检查签名、过期时间等）且与用户信息匹配
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    // 创建认证令牌对象，包含用户信息和权限列表
                    // 第二个参数为 null，表示不需要密码验证（已通过 JWT 验证）
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    // 设置认证详情（如请求 IP、会话 ID 等）
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    LOGGER.info("authenticated user:{}", username);
                    
                    // 将认证信息存入 Spring Security 上下文，后续请求可直接获取用户身份
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        // 继续执行过滤器链中的下一个过滤器或目标资源
        chain.doFilter(request, response);
    }
}


//完整的调用链路
//JwtAuthenticationTokenFilter.doFilterInternal() (第 87 行)
//        ↓
//        userDetailsService.loadUserByUsername(username)
//    ↓
//            MallSecurityConfig.userDetailsService() (你选中的代码，第 45 行)
//        ↓
//        adminService.loadUserByUsername(username) (UmsAdminServiceImpl 第 435 行)
//        ↓
//        ┌─────────────────────────────────────────┐
//        │ 第 437 行: getAdminByUsername(username) │ ← 查询用户基本信息（缓存+DB）
//        │ 第 439 行: getResourceList(admin.getId)│ ← 查询用户权限列表（缓存+DB）
//        └─────────────────────────────────────────┘
//        ↓
//返回 AdminUserDetails (包含完整用户信息和权限)
