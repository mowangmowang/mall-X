package com.macro.mall.security.component;

import org.springframework.security.access.ConfigAttribute;

import java.util.Map;

/**
 * 动态权限业务接口 (Dynamic Security Service)
 * <p>
 * 定义动态权限加载的规范，由具体业务模块（如 mall-admin）实现。
 * 负责从数据库或缓存中读取 URL 与权限的映射关系，供 {@link DynamicSecurityMetadataSource} 使用。
 * </p>
 */
public interface DynamicSecurityService {
    /**
     * 加载资源路径与权限的映射关系
     *
     * @return Map<Key, Value> 其中：
     *         - Key: 资源路径模式（支持 Ant 通配符，如 /api/admin/**）
     *         - Value: 该路径所需的权限配置（如 ROLE_ADMIN、pms:product:create）
     */
    Map<String, ConfigAttribute> loadDataSource();
}

