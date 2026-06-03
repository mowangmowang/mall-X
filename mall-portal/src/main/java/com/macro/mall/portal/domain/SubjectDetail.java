package com.macro.mall.portal.domain;

import com.macro.mall.model.CmsSubject;
import com.macro.mall.model.PmsProduct;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubjectDetail {
    @ApiModelProperty("专题信息")
    private CmsSubject subject;
    @ApiModelProperty("关联商品")
    private List<PmsProduct> productList;
}
