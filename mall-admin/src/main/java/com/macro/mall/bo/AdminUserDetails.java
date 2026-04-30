package com.macro.mall.bo;

import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 用户信息封装类
 * 用于将后台管理员信息转换为 Spring Security 所需的 UserDetails 格式
 * 实现认证和授权功能的核心数据载体
 */
public class AdminUserDetails implements UserDetails {
    /**
     * 后台管理员基本信息
     */
    private final UmsAdmin umsAdmin;
    
    /**
     * 管理员拥有的资源权限列表（用于接口访问控制）
     */
    private final List<UmsResource> resourceList;

    /**
     * 构造函数：初始化用户详情对象
     * @param umsAdmin 后台管理员实体
     * @param resourceList 该管理员可访问的资源列表
     */
    public AdminUserDetails(UmsAdmin umsAdmin, List<UmsResource> resourceList) {
        this.umsAdmin = umsAdmin;
        this.resourceList = resourceList;
    }

    /**
     * 获取当前用户的授权权限集合
     * 将资源列表转换为 Spring Security 的 GrantedAuthority 格式
     * 格式为：资源ID:资源名称（如：1:商品管理）
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回当前用户所拥有的资源
        return resourceList.stream()
                .map(resource -> new SimpleGrantedAuthority(resource.getId() + ":" + resource.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户密码（用于身份验证）
     * @return 加密后的密码
     */
    @Override
    public String getPassword() {
        return umsAdmin.getPassword();
    }

    /**
     * 获取用户名（用于身份验证）
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return umsAdmin.getUsername();
    }

    /**
     * 判断账户是否未过期（默认未过期）
     * @return true-未过期，false-已过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 判断账户是否未锁定（默认未锁定）
     * @return true-未锁定，false-已锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 判断凭证（密码）是否未过期（默认未过期）
     * @return true-未过期，false-已过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 判断账户是否启用
     * 根据管理员状态字段判断：1-启用，0-禁用
     * @return true-启用，false-禁用
     */
    @Override
    public boolean isEnabled() {
        return umsAdmin.getStatus().equals(1);
    }
}
