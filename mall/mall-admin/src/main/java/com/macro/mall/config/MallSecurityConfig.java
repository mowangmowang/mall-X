package com.macro.mall.config;

import com.macro.mall.model.UmsResource;
import com.macro.mall.security.component.DynamicSecurityService;
import com.macro.mall.service.UmsAdminService;
import com.macro.mall.service.UmsResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mall-security模块相关配置
 * 主要配置用户详情服务和动态权限数据源
 *
 */
@Configuration
public class MallSecurityConfig {

    /**
     * 后台用户管理Service
     */
    @Autowired
    private UmsAdminService adminService;

    /**
     * 后台资源管理Service
     */
    @Autowired
    private UmsResourceService resourceService;

    /**
     * 配置UserDetailsService，用于Spring Security加载用户信息
     *
     * @return UserDetailsService实例
     */
    @Bean
    public UserDetailsService userDetailsService() {
        //获取登录用户信息
        return username -> adminService.loadUserByUsername(username);
    }

    /**
     * 配置动态权限服务，用于从数据库加载资源与权限的对应关系
     *
     * @return DynamicSecurityService实例
     */
    @Bean
    public DynamicSecurityService dynamicSecurityService() {
        return new DynamicSecurityService() {
            @Override
            public Map<String, ConfigAttribute> loadDataSource() {
                // 使用ConcurrentHashMap保证线程安全
                Map<String, ConfigAttribute> map = new ConcurrentHashMap<>();
                // 获取所有资源列表
                List<UmsResource> resourceList = resourceService.listAll();
                // 遍历资源列表，构建URL到权限属性的映射
                for (UmsResource resource : resourceList) {
                    // key为资源URL，value为权限标识（ID:名称）
                    map.put(resource.getUrl(), new org.springframework.security.access.SecurityConfig(resource.getId() + ":" + resource.getName()));
                }
                return map;
            }
        };
    }
}
