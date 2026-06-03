package com.macro.mall.service;

import com.macro.mall.dto.UmsAdminParam;
import com.macro.mall.dto.UpdateAdminPasswordParam;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsResource;
import com.macro.mall.model.UmsRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台用户管理 Service 接口
 * 提供管理员的认证、授权、CRUD 等核心业务逻辑
 */
public interface UmsAdminService {
    /**
     * 根据用户名获取后台管理员信息
     * @param username 用户名
     * @return 管理员实体，不存在则返回 null
     */
    UmsAdmin getAdminByUsername(String username);

    /**
     * 管理员注册功能
     * 对密码进行加密处理后存储
     * @param umsAdminParam 注册参数
     * @return 注册成功返回管理员信息，失败返回 null
     */
    UmsAdmin register(UmsAdminParam umsAdminParam);

    /**
     * 管理员登录功能
     * 验证用户名和密码，成功后生成 JWT Token
     * @param username 用户名
     * @param password 密码
     * @return JWT Token，验证失败返回 null
     */
    String login(String username, String password);

    /**
     * 刷新 Token 功能
     * 当 Token 即将过期时，生成新的 Token
     * @param oldToken 旧的 Token
     * @return 新的 Token，刷新失败返回 null
     */
    String refreshToken(String oldToken);

    /**
     * 根据 ID 获取管理员信息
     * @param id 管理员 ID
     * @return 管理员实体
     */
    UmsAdmin getItem(Long id);

    /**
     * 分页查询管理员列表
     * 支持按用户名或昵称模糊搜索
     * @param keyword 搜索关键词
     * @param pageSize 每页条数
     * @param pageNum 页码
     * @return 管理员列表
     */
    List<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum);

    /**
     * 修改指定管理员信息
     * @param id 管理员 ID
     * @param admin 待更新的管理员信息
     * @return 影响行数
     */
    int update(Long id, UmsAdmin admin);

    /**
     * 删除指定管理员
     * @param id 管理员 ID
     * @return 影响行数
     */
    int delete(Long id);

    /**
     * 修改管理员角色关系
     * 先删除旧的角色关联，再插入新的角色关联
     * @param adminId 管理员 ID
     * @param roleIds 角色 ID 列表
     * @return 影响行数
     */
    @Transactional
    int updateRole(Long adminId, List<Long> roleIds);

    /**
     * 获取管理员对应的角色列表
     * @param adminId 管理员 ID
     * @return 角色列表
     */
    List<UmsRole> getRoleList(Long adminId);

    /**
     * 获取管理员可访问的资源列表
     * 用于权限控制
     * @param adminId 管理员 ID
     * @return 资源列表
     */
    List<UmsResource> getResourceList(Long adminId);

    /**
     * 修改管理员密码
     * 需要验证旧密码的正确性
     * @param updatePasswordParam 包含旧密码和新密码的参数
     * @return 操作结果：>0-成功，-1-参数不合法，-2-用户不存在，-3-旧密码错误
     */
    int updatePassword(UpdateAdminPasswordParam updatePasswordParam);

    /**
     * Spring Security 加载用户信息
     * 用于身份验证流程
     * @param username 用户名
     * @return UserDetails 对象
     */
    UserDetails loadUserByUsername(String username);

    /**
     * 获取缓存服务
     * 用于管理 Token 缓存
     * @return 缓存服务实例
     */
    UmsAdminCacheService getCacheService();

    /**
     * 管理员登出功能
     * 清除缓存中的 Token 信息
     * @param username 用户名
     */
    void logout(String username);
}
