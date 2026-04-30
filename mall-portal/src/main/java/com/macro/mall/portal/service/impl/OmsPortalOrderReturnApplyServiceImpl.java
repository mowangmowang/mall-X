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
        // 1. 验证订单是否存在
        com.macro.mall.model.OmsOrder order = orderMapper.selectByPrimaryKey(returnApply.getOrderId());
        if (order == null) {
            com.macro.mall.common.exception.Asserts.fail("订单不存在");
        }
            
        // 2. 验证订单归属
        com.macro.mall.model.UmsMember member = memberService.getCurrentMember();
        if (!member.getId().equals(order.getMemberId())) {
            com.macro.mall.common.exception.Asserts.fail("不能申请他人订单的退货");
        }
            
        // 3. 验证订单状态（只有已完成的订单才能退货）
        if (order.getStatus() != 3) {
            com.macro.mall.common.exception.Asserts.fail("只有已完成的订单才能申请退货");
        }
            
        // 4. 检查是否已申请过退货
        com.macro.mall.model.OmsOrderReturnApplyExample example = new com.macro.mall.model.OmsOrderReturnApplyExample();
        example.createCriteria()
            .andOrderIdEqualTo(returnApply.getOrderId())
            .andStatusNotEqualTo(3); // 排除已拒绝的申请
        long count = returnApplyMapper.countByExample(example);
        if (count > 0) {
            com.macro.mall.common.exception.Asserts.fail("该订单已有退货申请在处理中");
        }
            
        // 5. 验证退货原因是否为空
        if (returnApply.getReason() == null || returnApply.getReason().trim().isEmpty()) {
            com.macro.mall.common.exception.Asserts.fail("请选择退货原因");
        }
            
        // 6. 创建申请
        OmsOrderReturnApply realApply = new OmsOrderReturnApply();
        BeanUtils.copyProperties(returnApply, realApply);
        realApply.setCreateTime(new Date());
        realApply.setStatus(0);
        realApply.setMemberUsername(member.getUsername());
            
        System.out.println("[DEBUG] 创建售后申请 - memberUsername: " + member.getUsername() + ", orderId: " + returnApply.getOrderId());
        int result = returnApplyMapper.insert(realApply);
        System.out.println("[DEBUG] 插入结果: " + result + ", 生成的ID: " + realApply.getId());
        return result;
    }

    @Override
    public java.util.List<com.macro.mall.model.OmsOrderReturnApply> list() {
        com.macro.mall.model.UmsMember member = memberService.getCurrentMember();
        System.out.println("[DEBUG] 查询售后列表 - 当前 memberUsername: " + member.getUsername());
        com.macro.mall.model.OmsOrderReturnApplyExample example = new com.macro.mall.model.OmsOrderReturnApplyExample();
        example.createCriteria().andMemberUsernameEqualTo(member.getUsername());
        java.util.List<com.macro.mall.model.OmsOrderReturnApply> result = returnApplyMapper.selectByExample(example);
        System.out.println("[DEBUG] 查询结果数量: " + (result == null ? 0 : result.size()));
        return result;
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
