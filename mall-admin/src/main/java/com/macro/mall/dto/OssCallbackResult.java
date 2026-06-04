package com.macro.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class OssCallbackResult {
    @Schema(description = "文件名称")
    private String filename;
    @Schema(description = "文件大小")
    private String size;
    @Schema(description = "文件的mimeType")
    private String mimeType;
    @Schema(description = "图片文件的宽")
    private String width;
    @Schema(description = "图片文件的高")
    private String height;
}
