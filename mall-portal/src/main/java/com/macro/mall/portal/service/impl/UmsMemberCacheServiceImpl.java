package com.macro.mall.portal.service.impl;

import com.macro.mall.common.service.RedisService;
import com.macro.mall.mapper.UmsMemberMapper;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.service.UmsMemberCacheService;
import com.macro.mall.security.annotation.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 会员缓存管理Service实现类 (Member Cache Service Implementation)
 * <p>
 * 负责会员信息和验证码的Redis缓存操作，提高系统性能。
 * 使用@CacheException注解处理缓存异常，确保缓存失败不影响主业务流程。
 */
@Service
public class UmsMemberCacheServiceImpl implements UmsMemberCacheService {
    /** Redis服务，用于缓存操作 */
    @Autowired
    private RedisService redisService;
    
    /** 会员Mapper，用于数据库查询 */
    @Autowired
    private UmsMemberMapper memberMapper;
    
    /** Redis数据库前缀 */
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    
    /** 通用缓存过期时间 */
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    
    /** 验证码缓存过期时间 */
    @Value("${redis.expire.authCode}")
    private Long REDIS_EXPIRE_AUTH_CODE;
    
    /** 会员缓存Key前缀 */
    @Value("${redis.key.member}")
    private String REDIS_KEY_MEMBER;
    
    /** 验证码缓存Key前缀 */
    @Value("${redis.key.authCode}")
    private String REDIS_KEY_AUTH_CODE;

    /**
     * 删除会员缓存
     * <p>
     * 当会员信息更新时，清除对应的缓存以保证数据一致性
     *
     * @param memberId 会员唯一标识符 (Member ID)
     */
    @Override
    public void delMember(Long memberId) {
        UmsMember umsMember = memberMapper.selectByPrimaryKey(memberId);
        if (umsMember != null) {
            String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + umsMember.getUsername();
            redisService.del(key);
        }
    }

    /**
     * 从缓存获取会员信息
     * <p>
     * 根据用户名从Redis中获取会员信息
     *
     * @param username 用户名
     * @return 会员对象，缓存未命中则返回null
     */
    @Override
    public UmsMember getMember(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + username;
        return (UmsMember) redisService.get(key);
    }

    /**
     * 将会员信息存入缓存
     * <p>
     * 登录或注册后将会员信息写入Redis，设置过期时间
     *
     * @param member 会员对象
     */
    @Override
    public void setMember(UmsMember member) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + member.getUsername();
        redisService.set(key, member, REDIS_EXPIRE);
    }

    /**
     * 将验证码存入缓存
     * <p>
     * 生成验证码后存储到Redis，用于后续校验
     * 使用@CacheException注解，缓存失败不抛出异常
     *
     * @param telephone 手机号
     * @param authCode 验证码
     */
    @CacheException
    @Override
    public void setAuthCode(String telephone, String authCode) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        redisService.set(key,authCode,REDIS_EXPIRE_AUTH_CODE);
    }

    /**
     * 从缓存获取验证码
     * <p>
     * 根据手机号从Redis中获取验证码进行校验
     * 使用@CacheException注解，缓存失败不抛出异常
     *
     * @param telephone 手机号
     * @return 验证码，缓存未命中则返回null
     */
    @CacheException
    @Override
    public String getAuthCode(String telephone) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        return (String) redisService.get(key);
    }
}
