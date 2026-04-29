package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.OmsOrderReturnApplyMapper;
import com.macro.mall.model.OmsOrderReturnApply;
import com.macro.mall.portal.domain.OmsOrderReturnApplyParam;
import com.macro.mall.portal.service.OmsPortalOrderReturnApplyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 订单退货管理Service实现类 */
@Service
public class OmsPortalOrderReturnApplyServiceImpl implements OmsPortalOrderReturnApplyService {
    @Autowired
    private OmsOrderReturnApplyMapper returnApplyMapper;
    @Autowired
    private com.macro.mall.portal.service.UmsMemberService memberService;
    @Autowired
    private com.macro.mall.mapper.OmsOrderMapper orderMapper;
    @Override
    public int create(OmsOrderReturnApplyParam returnApply) {
        OmsOrderReturnApply realApply = new OmsOrderReturnApply();
        BeanUtils.copyProperties(returnApply,realApply);
        realApply.setCreateTime(new Date());
        realApply.setStatus(0);
        // 更新订单状态为已关闭，或者你可以保持原样，仅在前端显示状态
        // 为了演示效果，我们这里不改订单状态，而是让前端通过查询申请记录来显示“售后中”
        return returnApplyMapper.insert(realApply);
    }

    @Override
    public java.util.List<com.macro.mall.model.OmsOrderReturnApply> list() {
        com.macro.mall.model.UmsMember member = memberService.getCurrentMember();
        com.macro.mall.model.OmsOrderReturnApplyExample example = new com.macro.mall.model.OmsOrderReturnApplyExample();
        example.createCriteria().andMemberUsernameEqualTo(member.getUsername());
        return returnApplyMapper.selectByExample(example);
    }

    @Override
    public int cancel(Long id) {
        com.macro.mall.model.UmsMember member = memberService.getCurrentMember();
        com.macro.mall.model.OmsOrderReturnApply apply = returnApplyMapper.selectByPrimaryKey(id);
        if(apply != null && apply.getMemberUsername().equals(member.getUsername()) && apply.getStatus() == 0){
            // 状态改为已取消，由于没有定义已取消的状态，我们可以直接删除或者定义一个新状态
            // 简单处理：直接删除记录，让用户可以重新发起
            return returnApplyMapper.deleteByPrimaryKey(id);
        }
        return 0;
    }
}
