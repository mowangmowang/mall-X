package com.macro.mall.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 状态约束校验器
 * 用于验证整数值是否在指定的允许值范围内
 * 
 * <p>通常用于验证状态字段（如启用/禁用、审核状态等）的合法性</p>
 * 
 * @author macro
 *
 * @see FlagValidator
 */
public class FlagValidatorClass implements ConstraintValidator<FlagValidator,Integer> {
    /**
     * 允许的状态值数组
     */
    private String[] values;

    /**
     * 初始化校验器，从注解中获取允许的值列表
     *
     * @param flagValidator 状态校验注解，包含允许的值数组
     */
    @Override
    public void initialize(FlagValidator flagValidator) {
        this.values = flagValidator.value();
    }

    /**
     * 执行状态值校验
     *
     * @param value 待校验的整数值
     * @param constraintValidatorContext 约束校验上下文，用于自定义错误信息
     * @return true-校验通过（值为null或在允许范围内），false-校验失败
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        // 当状态为空时使用默认值，校验通过
        if(value==null){
            return true;
        }
        
        // 遍历允许的值数组，检查当前值是否在允许范围内
        boolean isValid = false;
        for(int i=0;i<values.length;i++){
            if(values[i].equals(String.valueOf(value))){
                isValid = true;
                break;
            }
        }
        return isValid;
    }
}
