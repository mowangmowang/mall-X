package com.macro.mall.security.component;

import cn.hutool.core.collection.CollUtil;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Iterator;

/**
 * 动态权限决策管理器 (Dynamic Access Decision Manager)
 * <p>
 * 实现 Spring Security 的 {@link AccessDecisionManager} 接口，负责判断用户是否有权限访问特定资源。
 * 核心逻辑：遍历当前 URL 所需的所有权限，检查用户的权限列表中是否包含任意一个所需权限。
 * </p>
 */
public class DynamicAccessDecisionManager implements AccessDecisionManager {

    /**
     * 核心决策方法：判断用户是否有权限访问资源
     *
     * @param authentication 当前用户的认证信息（包含用户权限列表）
     * @param object 被访问的安全对象（通常是 FilterInvocation）
     * @param configAttributes 访问该资源所需的权限集合
     * @throws AccessDeniedException 当用户没有足够权限时抛出
     * @throws InsufficientAuthenticationException 当认证信息不足时抛出
     */
    @Override
    public void decide(Authentication authentication, Object object,
                       Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        // 场景1：接口未配置权限规则，直接放行（允许匿名访问）
        if (CollUtil.isEmpty(configAttributes)) {
            return;
        }
        
        // 场景2：遍历所需权限，逐一与用户权限比对
        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()) {
            ConfigAttribute configAttribute = iterator.next();
            // 获取访问该资源所需的权限标识（如：ROLE_ADMIN、pms:product:create）
            String needAuthority = configAttribute.getAttribute();
            
            // 遍历用户拥有的所有权限，查找匹配项
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
                if (needAuthority.trim().equals(grantedAuthority.getAuthority())) {
                    // 找到匹配的权限，立即返回，允许访问
                    return;
                }
            }
        }
        
        // 场景3：所有权限都不匹配，拒绝访问
        throw new AccessDeniedException("抱歉，您没有访问权限");
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }

}

