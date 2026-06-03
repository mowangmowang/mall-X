package com.macro.mall.config;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS（对象存储服务）配置类
 * 用于上传图片、文件等资源到云存储
 */
@Configuration
public class OssConfig {
    /**
     * OSS 访问端点（从配置文件读取）
     */
    @Value("${aliyun.oss.endpoint}")
    private String ALIYUN_OSS_ENDPOINT;
    
    /**
     * OSS AccessKey ID（从配置文件读取）
     */
    @Value("${aliyun.oss.accessKeyId}")
    private String ALIYUN_OSS_ACCESSKEYID;
    
    /**
     * OSS AccessKey Secret（从配置文件读取）
     */
    @Value("${aliyun.oss.accessKeySecret}")
    private String ALIYUN_OSS_ACCESSKEYSECRET;
    
    /**
     * 创建 OSS 客户端 Bean
     * 用于执行文件上传、删除等操作
     * @return OSSClient 实例
     */
    @Bean
    public OSSClient ossClient() {
        return new OSSClient(ALIYUN_OSS_ENDPOINT, ALIYUN_OSS_ACCESSKEYID, ALIYUN_OSS_ACCESSKEYSECRET);
    }
}
