package com.macro.mall.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * MinIO Bucket 访问策略配置数据传输对象 (Data Transfer Object)
 * 用于定义存储桶的权限策略，遵循 AWS S3 兼容的策略格式
 */
@Data
@EqualsAndHashCode
@Builder
public class BucketPolicyConfigDto {

    /**
     * 策略语言版本 (Policy Language Version)
     * 通常固定为 "2012-10-17"
     */
    private String Version;

    /**
     * 策略声明列表 (Statement List)
     * 包含一个或多个权限声明，定义允许或拒绝的操作
     */
    private List<Statement> Statement;

    /**
     * 策略声明内部类
     * 定义具体的权限规则，包括效果、主体、操作和资源
     */
    @Data
    @EqualsAndHashCode
    @Builder
    public static class Statement {
        /**
         * 效果 (Effect)
         * 指定是允许 ("Allow") 还是拒绝 ("Deny") 该操作
         */
        private String Effect;

        /**
         * 主体 (Principal)
         * 指定策略应用的用户或账户，"*" 表示所有用户
         */
        private String Principal;

        /**
         * 操作 (Action)
         * 指定允许或拒绝的具体操作，如 "s3:GetObject", "s3:PutObject" 等
         */
        private String Action;

        /**
         * 资源 (Resource)
         * 指定策略应用的存储桶或对象路径，如 "arn:aws:s3:::bucket-name/*"
         */
        private String Resource;
    }
}
