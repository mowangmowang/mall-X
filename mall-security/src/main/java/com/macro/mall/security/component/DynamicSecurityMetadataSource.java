package com.macro.mall.security.component;

import cn.hutool.core.util.URLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 动态权限数据源 (Dynamic Security Metadata Source)
 * <p>
 * 实现 Spring Security 的 {@link FilterInvocationSecurityMetadataSource} 接口，
 * 负责根据当前请求的 URL 路径，从内存中查找该 URL 对应的权限配置。
 * </p>
 * <p>
 * 工作流程：
 * 1. 在应用启动时通过 {@link #loadDataSource()} 加载所有 URL-权限映射关系
 * 2. 每次请求到达时，通过 {@link #getAttributes(Object)} 匹配当前 URL 所需的权限
 * 3. 将匹配的权限返回给 {@link DynamicAccessDecisionManager} 进行决策
 * </p>
 */
public class DynamicSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    /**
     * 存储 URL 模式与权限配置的映射关系
     * Key: URL 模式（支持 Ant 风格通配符，如 /api/admin/**）
     * Value: 该 URL 所需的权限配置（如 ROLE_ADMIN）
     */
    private static Map<String, ConfigAttribute> configAttributeMap = null;
    
    /**
     * 动态权限服务接口，由具体业务模块实现，负责从数据库加载权限规则
     */
    @Autowired
    private DynamicSecurityService dynamicSecurityService;

    /**
     * 应用启动时自动调用，从数据库加载所有 URL-权限映射关系
     */
    @PostConstruct
    public void loadDataSource() {
        configAttributeMap = dynamicSecurityService.loadDataSource();
    }

    /**
     * 清空已加载的权限数据源（用于权限规则更新后重新加载）
     */
    public void clearDataSource() {
        configAttributeMap.clear();
        configAttributeMap = null;
    }

    /**
     * 根据当前请求获取所需的权限配置
     *
     * @param o 安全对象，类型为 {@link FilterInvocation}，封装了当前 HTTP 请求信息
     * @return 当前 URL 所需的权限集合，若未配置则返回空集合
     * @throws IllegalArgumentException 如果参数类型不正确
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        // 懒加载：若权限数据尚未加载，则立即加载
        if (configAttributeMap == null) this.loadDataSource();
        
        List<ConfigAttribute> configAttributes = new ArrayList<>();
        
        // 提取当前请求的 URL 路径（去除查询参数）
        String url = ((FilterInvocation) o).getRequestUrl();
        String path = URLUtil.getPath(url);
        
        // 使用 Ant 风格路径匹配器进行 URL 模式匹配
        PathMatcher pathMatcher = new AntPathMatcher();
        Iterator<String> iterator = configAttributeMap.keySet().iterator();
        
        // 遍历所有已配置的 URL 模式，查找与当前请求路径匹配的项
        while (iterator.hasNext()) {
            String pattern = iterator.next();
            // 若当前 URL 模式与请求路径匹配，则将对应的权限配置加入结果集
            if (pathMatcher.match(pattern, path)) {
                configAttributes.add(configAttributeMap.get(pattern));
            }
        }
        
        // 返回匹配的权限配置（可能为空集合，表示该 URL 无需特定权限）
        return configAttributes;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }

}

