package com.macro.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class OssCallbackParam {
    @Schema(description = "请求的回调地址")
    private String callbackUrl;
    @Schema(description = "回调是传入request中的参数")
    private String callbackBody;
    @Schema(description = "回调时传入参数的格式，比如表单提交形式")
    private String callbackBodyType;
}
