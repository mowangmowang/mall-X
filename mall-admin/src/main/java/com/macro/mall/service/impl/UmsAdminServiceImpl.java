package com.macro.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.bo.AdminUserDetails;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.common.util.RequestUtil;
import com.macro.mall.dao.UmsAdminRoleRelationDao;
import com.macro.mall.dto.UmsAdminParam;
import com.macro.mall.dto.UpdateAdminPasswordParam;
import com.macro.mall.mapper.UmsAdminLoginLogMapper;
import com.macro.mall.mapper.UmsAdminMapper;
import com.macro.mall.mapper.UmsAdminRoleRelationMapper;
import com.macro.mall.mapper.UmsRoleMapper;
import com.macro.mall.model.*;
import com.macro.mall.security.util.JwtTokenUtil;
import com.macro.mall.security.util.SpringUtil;
import com.macro.mall.service.UmsAdminCacheService;
import com.macro.mall.service.UmsAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 后台用户管理 Service 实现类
 * 实现管理员的注册、登录、权限管理等核心业务逻辑
 * 集成 Spring Security 和 JWT 进行身份认证
 */
@Service
public class UmsAdminServiceImpl implements UmsAdminService {
    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);
    
    /**
     * JWT Token 工具类
     */
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 密码加密器（BCrypt）
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理员 Mapper
     */
    @Autowired
    private UmsAdminMapper adminMapper;

    /**
     * 管理员-角色关系 Mapper
     */
    @Autowired
    private UmsAdminRoleRelationMapper adminRoleRelationMapper;

    /**
     * 管理员-角色关系 DAO（自定义查询）
     */
    @Autowired
    private UmsAdminRoleRelationDao adminRoleRelationDao;

    /**
     * 管理员登录日志 Mapper
     */
    @Autowired
    private UmsAdminLoginLogMapper loginLogMapper;

    /**
     * 角色 Mapper
     */
    @Autowired
    private UmsRoleMapper roleMapper;

    /**
     * 根据用户名获取后台管理员
     * 采用缓存优先策略：先查 Redis，再查数据库
     *
     * @param username 用户名
     * @return 后台管理员对象 (UmsAdmin)，若不存在则返回 null
     */
    @Override
    public UmsAdmin getAdminByUsername(String username) {
        // 先从缓存中获取数据
        UmsAdmin admin = getCacheService().getAdmin(username);
        if (admin != null) {
            return admin;
        }
        // 缓存中没有再从数据库中获取
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        if (adminList != null && !adminList.isEmpty()) {
            admin = adminList.get(0);
            // 将数据库中的数据存入缓存中
            getCacheService().setAdmin(admin);
            return admin;
        }
        return null;
    }

    /**
     * 注册后台用户
     * 对密码进行 BCrypt 加密后存储
     *
     * @param umsAdminParam 注册参数
     * @return 注册成功的后台管理员对象 (UmsAdmin)，若用户名已存在则返回 null
     */
    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        // 设置默认状态为启用
        umsAdmin.setStatus(1);
        
        // 查询是否有相同用户名的用户
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(umsAdmin.getUsername());
        List<UmsAdmin> umsAdminList = adminMapper.selectByExample(example);
        if (!umsAdminList.isEmpty()) {
            return null;
        }
        
        // 将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        adminMapper.insert(umsAdmin);
        return umsAdmin;
    }

    /**
     * 登录功能
     * 验证用户名和密码，成功后生成 JWT Token
     *
     * @param username 用户名
     * @param password 密码
     * @return JWT Token，若登录失败则返回 null
     */
    @Override
    public String login(String username, String password) {
        String token = null;
        try {
            // 第1步：加载用户详情（包含权限）
            // 加载用户详情（包含权限信息）
            // ↓ 调用链路：loadUserByUsername → getAdminByUsername + getResourceList → new AdminUserDetails()
            // ↓ AdminUserDetails 实现了 UserDetails 接口，包含用户名、密码、权限列表
            UserDetails userDetails = loadUserByUsername(username);
            
            // // 第2步：验证密码是否正确
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                Asserts.fail("密码不正确");
            }
            
            // 验证账号状态是否启用
            if (!userDetails.isEnabled()) {
                Asserts.fail("帐号已被禁用");
            }
            
            // 创建认证令牌并设置到 Spring Security 上下文
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails,  // Principal: 用户主体信息
                            null,     // Credentials: 凭证（密码），设为null表示已验证
                            userDetails.getAuthorities());  // Authorities: 权限列表
            // 第4步：将认证信息存入 Spring Security 上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成 JWT Token
            token = jwtTokenUtil.generateToken(userDetails);
            
            // 记录登录日志
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常: {}", e.getMessage());
        }
        return token;
    }

    /**
     * 添加登录记录
     * 记录管理员登录时间、IP 地址等信息
     *
     * @param username 用户名
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        if (admin == null) {
            return;
        }
        
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        
        // 获取请求 IP 地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            loginLog.setIp(RequestUtil.getRequestIp(request));
        }
        
        loginLogMapper.insert(loginLog);
    }

    /**
     * 根据用户名修改登录时间
     *
     * @param username 用户名
     */
    private void updateLoginTimeByUsername(String username) {
        UmsAdmin record = new UmsAdmin();
        record.setLoginTime(new Date());
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(username);
        adminMapper.updateByExampleSelective(record, example);
    }

    /**
     * 刷新 Token
     *
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    @Override
    public String refreshToken(String oldToken) {
        return jwtTokenUtil.refreshHeadToken(oldToken);
    }

    /**
     * 根据 ID 获取管理员信息
     *
     * @param id 管理员 ID
     * @return 管理员对象 (UmsAdmin)
     */
    @Override
    public UmsAdmin getItem(Long id) {
        return adminMapper.selectByPrimaryKey(id);
    }

    /**
     * 分页查询管理员列表
     *
     * @param keyword  搜索关键字（用户名或昵称）
     * @param pageSize 每页数量
     * @param pageNum  页码
     * @return 管理员列表
     */
    @Override
    public List<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsAdminExample example = new UmsAdminExample();
        UmsAdminExample.Criteria criteria = example.createCriteria();
        
        // 如果有关键字，按用户名或昵称模糊查询
        if (!StrUtil.isEmpty(keyword)) {
            criteria.andUsernameLike("%" + keyword + "%");
            example.or(example.createCriteria().andNickNameLike("%" + keyword + "%"));
        }
        
        return adminMapper.selectByExample(example);
    }

    /**
     * 更新指定管理员信息
     *
     * @param id    管理员 ID
     * @param admin 更新后的管理员信息
     * @return 更新影响的行数
     */
    @Override
    public int update(Long id, UmsAdmin admin) {
        admin.setId(id);
        UmsAdmin rawAdmin = adminMapper.selectByPrimaryKey(id);
        
        if (rawAdmin != null) {
            if (rawAdmin.getPassword().equals(admin.getPassword())) {
                // 与原加密密码相同的不需要修改
                admin.setPassword(null);
            } else {
                // 与原加密密码不同的需要加密修改
                if (StrUtil.isEmpty(admin.getPassword())) {
                    admin.setPassword(null);
                } else {
                    admin.setPassword(passwordEncoder.encode(admin.getPassword()));
                }
            }
        }
        
        int count = adminMapper.updateByPrimaryKeySelective(admin);
        // 清除缓存
        getCacheService().delAdmin(id);
        return count;
    }

    /**
     * 删除管理员
     *
     * @param id 管理员 ID
     * @return 删除影响的行数
     */
    @Override
    public int delete(Long id) {
        // 先获取该管理员的所有角色，用于后续更新 admin_count
        List<UmsRole> roles = getRoleList(id);
        
        // 清除缓存
        getCacheService().delAdmin(id);
        getCacheService().delResourceList(id);
        
        // 删除管理员记录
        int count = adminMapper.deleteByPrimaryKey(id);
        
        // 删除管理员后，更新相关角色的 admin_count
        if (CollUtil.isNotEmpty(roles)) {
            for (UmsRole role : roles) {
                updateRoleAdminCount(role.getId());
            }
        }
        
        return count;
    }

    /**
     * 修改管理员角色关系
     *
     * @param adminId 管理员 ID
     * @param roleIds 角色 ID 列表
     * @return 更新影响的角色数量
     */
    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        // 先获取该管理员原来的角色，用于后续更新 admin_count
        List<UmsRole> oldRoles = getRoleList(adminId);
        
        int count = roleIds == null ? 0 : roleIds.size();
        
        // 先删除原来的角色关系
        UmsAdminRoleRelationExample adminRoleRelationExample = new UmsAdminRoleRelationExample();
        adminRoleRelationExample.createCriteria().andAdminIdEqualTo(adminId);
        adminRoleRelationMapper.deleteByExample(adminRoleRelationExample);
        
        // 建立新的角色关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UmsAdminRoleRelation> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                UmsAdminRoleRelation roleRelation = new UmsAdminRoleRelation();
                roleRelation.setAdminId(adminId);
                roleRelation.setRoleId(roleId);
                list.add(roleRelation);
            }
            adminRoleRelationDao.insertList(list);
        }
        
        // 清除资源缓存
        getCacheService().delResourceList(adminId);
        
        // 更新角色的 admin_count
        // 1. 更新旧角色的 count（减少）
        if (CollUtil.isNotEmpty(oldRoles)) {
            for (UmsRole oldRole : oldRoles) {
                updateRoleAdminCount(oldRole.getId());
            }
        }
        // 2. 更新新角色的 count（增加）
        if (!CollectionUtils.isEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                updateRoleAdminCount(roleId);
            }
        }
        
        return count;
    }

    /**
     * 获取指定管理员的角色列表
     *
     * @param adminId 管理员 ID
     * @return 角色列表
     */
    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return adminRoleRelationDao.getRoleList(adminId);
    }

    /**
     * 获取指定管理员的资源列表（带缓存）
     *
     * @param adminId 管理员 ID
     * @return 资源列表
     */
    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        // 先从缓存中获取数据
        List<UmsResource> resourceList = getCacheService().getResourceList(adminId);
        if (CollUtil.isNotEmpty(resourceList)) {
            return resourceList;
        }
        
        // 缓存中没有从数据库中获取
        resourceList = adminRoleRelationDao.getResourceList(adminId);
        if (CollUtil.isNotEmpty(resourceList)) {
            // 将数据库中的数据存入缓存中
            getCacheService().setResourceList(adminId, resourceList);
        }
        return resourceList;
    }

    /**
     * 修改密码
     *
     * @param param 密码修改参数
     * @return 状态码：1-成功，-1-参数为空，-2-用户不存在，-3-原密码错误
     */
    @Override
    public int updatePassword(UpdateAdminPasswordParam param) {
        if (StrUtil.isEmpty(param.getUsername())
                || StrUtil.isEmpty(param.getOldPassword())
                || StrUtil.isEmpty(param.getNewPassword())) {
            return -1;
        }
        
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(param.getUsername());
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        
        if (CollUtil.isEmpty(adminList)) {
            return -2;
        }
        
        UmsAdmin umsAdmin = adminList.get(0);
        // 验证原密码
        if (!passwordEncoder.matches(param.getOldPassword(), umsAdmin.getPassword())) {
            return -3;
        }
        
        // 更新新密码
        umsAdmin.setPassword(passwordEncoder.encode(param.getNewPassword()));
        adminMapper.updateByPrimaryKey(umsAdmin);
        
        // 清除缓存
        getCacheService().delAdmin(umsAdmin.getId());
        return 1;
    }

    /**
     * 加载用户信息用于 Spring Security 认证
     *
     * @param username 用户名
     * @return 用户详情对象 (UserDetails)
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            List<UmsResource> resourceList = getResourceList(admin.getId());
            return new AdminUserDetails(admin, resourceList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    /**
     * 获取缓存服务实例
     *
     * @return 后台用户缓存服务 (UmsAdminCacheService)
     */
    @Override
    public UmsAdminCacheService getCacheService() {
        return SpringUtil.getBean(UmsAdminCacheService.class);
    }

    /**
     * 登出功能
     *
     * @param username 用户名
     */
    @Override
    public void logout(String username) {
        // 清空缓存中的用户相关数据
        UmsAdmin admin = getCacheService().getAdmin(username);
        if (admin != null) {
            getCacheService().delAdmin(admin.getId());
            getCacheService().delResourceList(admin.getId());
        }
    }

    /**
     * 更新角色的管理员数量
     *
     * @param roleId 角色 ID
     */
    private void updateRoleAdminCount(Long roleId) {
        int count = adminRoleRelationDao.countByRoleId(roleId);
        UmsRole role = new UmsRole();
        role.setId(roleId);
        role.setAdminCount(count);
        roleMapper.updateByPrimaryKeySelective(role);
    }
}
