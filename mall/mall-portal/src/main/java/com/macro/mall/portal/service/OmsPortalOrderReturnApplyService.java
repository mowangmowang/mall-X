package com.macro.mall.portal.service;

import com.macro.mall.portal.domain.OmsOrderReturnApplyParam;

/**
 * 前台订单退货管理Service */
public interface OmsPortalOrderReturnApplyService {
    /**
     * 提交申请
     */
    int create(OmsOrderReturnApplyParam returnApply);

    /**
     * 获取申请列表
     */
    java.util.List<com.macro.mall.model.OmsOrderReturnApply> list();

    /**
     * 取消申请
     */
    int cancel(Long id);
}
