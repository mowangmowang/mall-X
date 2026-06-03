package com.macro.mall.portal.config;

import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Spring Security 安全配置类 (Spring Security Configuration)
 * 配置用户详情服务，用于会员登录认证和权限控制
 */
@Configuration
public class MallSecurityConfig {

    @Autowired
    private UmsMemberService memberService;

    /**
     * 配置 UserDetailsService Bean
     * 用于从数据库中加载用户信息，供 Spring Security 进行身份验证
     */
    @Bean
    public UserDetailsService userDetailsService() {
        //获取登录用户信息
        return username -> memberService.loadUserByUsername(username);
    }
}
