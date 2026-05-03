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
 * 订单退货管理服务实现类 (Order Return Apply Service Implementation)
 */
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
        // 注意：status=3 表示已拒绝，允许用户重新申请
        com.macro.mall.model.OmsOrderReturnApplyExample example = new com.macro.mall.model.OmsOrderReturnApplyExample();
        example.createCriteria()
            .andOrderIdEqualTo(returnApply.getOrderId())
            .andStatusIn(java.util.Arrays.asList(0, 1, 2)); // 只排除待处理(0)、退货中(1)、已完成(2)
        long count = returnApplyMapper.countByExample(example);
        if (count > 0) {
            com.macro.mall.common.exception.Asserts.fail("该订单已有退货申请在处理中，请勿重复提交");
        } else {
            System.out.println("[DEBUG] 订单 " + returnApply.getOrderId() + " 无进行中的售后申请，允许提交");
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
        String currentUsername = member.getUsername();
        System.out.println("[DEBUG] 查询售后列表 - 当前 memberUsername: " + currentUsername);
        
        com.macro.mall.model.OmsOrderReturnApplyExample example = new com.macro.mall.model.OmsOrderReturnApplyExample();
        example.createCriteria().andMemberUsernameEqualTo(currentUsername);
        // 按申请时间倒序排列，最新的在最前面
        example.setOrderByClause("create_time DESC");
        
        java.util.List<com.macro.mall.model.OmsOrderReturnApply> result = returnApplyMapper.selectByExample(example);
        System.out.println("[DEBUG] 查询结果数量: " + (result == null ? 0 : result.size()));
        
        if (result != null && !result.isEmpty()) {
            for (com.macro.mall.model.OmsOrderReturnApply apply : result) {
                System.out.println("[DEBUG] 售后记录 - ID: " + apply.getId() + ", orderSn: " + apply.getOrderSn() 
                    + ", memberUsername: " + apply.getMemberUsername() + ", status: " + apply.getStatus());
            }
        } else {
            System.out.println("[WARN] 未找到该用户的售后记录，请检查 memberUsername 是否匹配");
        }
        
        return result;
    }

    @Override
    public com.macro.mall.model.OmsOrderReturnApply getDetail(Long id) {
        com.macro.mall.model.UmsMember member = memberService.getCurrentMember();
        com.macro.mall.model.OmsOrderReturnApply apply = returnApplyMapper.selectByPrimaryKey(id);
        
        // 验证申请是否存在且属于当前用户
        if (apply != null && apply.getMemberUsername().equals(member.getUsername())) {
            System.out.println("[DEBUG] 获取售后详情 - ID: " + id + ", orderSn: " + apply.getOrderSn());
            return apply;
        } else {
            System.out.println("[WARN] 售后申请不存在或无权访问 - ID: " + id);
            return null;
        }
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
