package com.macro.mall.portal.domain;

import com.macro.mall.model.CmsPrefrenceArea;
import com.macro.mall.model.PmsProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrefrenceAreaResult {
    @Schema(description = "优选专区")
    private CmsPrefrenceArea area;
    @Schema(description = "专区关联商品")
    private List<PmsProduct> productList;
}
