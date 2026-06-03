package com.macro.mall.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 标志验证注解 (Flag Validator Annotation)
 * <p>
 * 用于验证字段或参数的值是否在指定的允许范围内。
 * 通常用于状态码、类型标识等枚举型或有限集合值的校验。
 * </p>
 *
 * @author alan
 * @see FlagValidatorClass
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = FlagValidatorClass.class)
public @interface FlagValidator {

    /**
     * 允许的值列表
     * <p>
     * 被注解的元素值必须包含在此数组中，否则校验失败。
     * </p>
     *
     * @return 允许的字符串值数组
     */
    String[] value() default {};

    /**
     * 校验失败时的错误消息
     *
     * @return 错误消息模板
     */
    String message() default "flag is not found";

    /**
     * 分组信息
     * <p>
     * 用于指定校验所属的组 (Validation Groups)，默认使用 Default 组。
     * </p>
     *
     * @return 校验组类数组
     */
    Class<?>[] groups() default {};

    /**
     * 负载信息
     * <p>
     * 用于客户端添加额外的元数据信息，通常不用于校验逻辑本身。
     * </p>
     *
     * @return 负载类数组
     */
    Class<? extends Payload>[] payload() default {};
}
