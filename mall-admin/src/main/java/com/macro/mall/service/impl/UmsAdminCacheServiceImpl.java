package com.macro.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.dao.UmsAdminRoleRelationDao;
import com.macro.mall.mapper.UmsAdminRoleRelationMapper;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsAdminRoleRelation;
import com.macro.mall.model.UmsAdminRoleRelationExample;
import com.macro.mall.model.UmsResource;
import com.macro.mall.service.UmsAdminCacheService;
import com.macro.mall.service.UmsAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台用户缓存管理 Service 实现类
 * 使用 Redis 缓存管理员信息和资源权限列表，提升系统性能
 */
@Service
public class UmsAdminCacheServiceImpl implements UmsAdminCacheService {
    /**
     * 后台用户服务
     */
    @Autowired
    private UmsAdminService adminService;
    
    /**
     * Redis 服务
     */
    @Autowired
    private RedisService redisService;
    
    /**
     * 管理员-角色关系 Mapper
     */
    @Autowired
    private UmsAdminRoleRelationMapper adminRoleRelationMapper;
    
    /**
     * 管理员-角色关系 DAO
     */
    @Autowired
    private UmsAdminRoleRelationDao adminRoleRelationDao;
    
    /**
     * Redis 数据库前缀（从配置文件读取）
     */
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    
    /**
     * Redis 过期时间（从配置文件读取）
     */
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    
    /**
     * Redis 管理员信息 Key 前缀（从配置文件读取）
     */
    @Value("${redis.key.admin}")
    private String REDIS_KEY_ADMIN;
    
    /**
     * Redis 资源列表 Key 前缀（从配置文件读取）
     */
    @Value("${redis.key.resourceList}")
    private String REDIS_KEY_RESOURCE_LIST;

    /**
     * 删除管理员缓存
     *
     * @param adminId 管理员 ID
     */
    @Override
    public void delAdmin(Long adminId) {
        UmsAdmin admin = adminService.getItem(adminId);
        if (admin != null) {
            String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
            redisService.del(key);
        }
    }

    /**
     * 删除管理员资源列表缓存
     *
     * @param adminId 管理员 ID
     */
    @Override
    public void delResourceList(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        redisService.del(key);
    }

    /**
     * 根据角色 ID 删除相关管理员的资源列表缓存
     *
     * @param roleId 角色 ID
     */
    @Override
    public void delResourceListByRole(Long roleId) {
        UmsAdminRoleRelationExample example = new UmsAdminRoleRelationExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        List<UmsAdminRoleRelation> relationList = adminRoleRelationMapper.selectByExample(example);
        if (CollUtil.isNotEmpty(relationList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            List<String> keys = relationList.stream().map(relation -> keyPrefix + relation.getAdminId()).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    /**
     * 根据角色 ID 列表批量删除相关管理员的资源列表缓存
     *
     * @param roleIds 角色 ID 列表
     */
    @Override
    public void delResourceListByRoleIds(List<Long> roleIds) {
        UmsAdminRoleRelationExample example = new UmsAdminRoleRelationExample();
        example.createCriteria().andRoleIdIn(roleIds);
        List<UmsAdminRoleRelation> relationList = adminRoleRelationMapper.selectByExample(example);
        if (CollUtil.isNotEmpty(relationList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            List<String> keys = relationList.stream().map(relation -> keyPrefix + relation.getAdminId()).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    /**
     * 根据资源 ID 删除拥有该资源的管理员的资源列表缓存
     *
     * @param resourceId 资源 ID
     */
    @Override
    public void delResourceListByResource(Long resourceId) {
        List<Long> adminIdList = adminRoleRelationDao.getAdminIdList(resourceId);
        if (CollUtil.isNotEmpty(adminIdList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            List<String> keys = adminIdList.stream().map(adminId -> keyPrefix + adminId).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    /**
     * 从缓存中获取管理员信息
     *
     * @param username 用户名
     * @return 管理员对象，不存在则返回 null
     */
    @Override
    public UmsAdmin getAdmin(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + username;
        return (UmsAdmin) redisService.get(key);
    }

    /**
     * 将管理员信息存入缓存
     *
     * @param admin 管理员对象
     */
    @Override
    public void setAdmin(UmsAdmin admin) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
        redisService.set(key, admin, REDIS_EXPIRE);
    }

    /**
     * 从缓存中获取管理员的资源列表
     *
     * @param adminId 管理员 ID
     * @return 资源列表，不存在则返回 null
     */
    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        return (List<UmsResource>) redisService.get(key);
    }

    /**
     * 将管理员的资源列表存入缓存
     *
     * @param adminId 管理员 ID
     * @param resourceList 资源列表
     */
    @Override
    public void setResourceList(Long adminId, List<UmsResource> resourceList) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        redisService.set(key, resourceList, REDIS_EXPIRE);
    }
}
