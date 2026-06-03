package com.macro.mall.ai.service;

import com.macro.mall.mapper.OmsOrderReturnReasonMapper;
import com.macro.mall.model.OmsOrderReturnReason;
import com.macro.mall.model.OmsOrderReturnReasonExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 退货原因服务
 * 用于获取数据库中启用的退货原因列表
 */
@Service
public class ReturnReasonService {

    private static final Logger log = LoggerFactory.getLogger(ReturnReasonService.class);

    @Autowired
    private OmsOrderReturnReasonMapper returnReasonMapper;

    /**
     * 获取所有启用的退货原因名称列表
     * 从数据库动态查询，确保与后台配置一致
     * 
     * @return 启用的退货原因名称列表
     */
    public List<String> getEnabledReturnReasons() {
        try {
            OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
            // 只查询启用的原因（status=1）
            example.createCriteria().andStatusEqualTo(1);
            // 按 sort 降序排列
            example.setOrderByClause("sort desc");
            
            List<OmsOrderReturnReason> reasons = returnReasonMapper.selectByExample(example);
            
            List<String> reasonNames = reasons.stream()
                    .map(OmsOrderReturnReason::getName)
                    .collect(Collectors.toList());
            
            log.info("从数据库加载了 {} 个启用的退货原因: {}", reasonNames.size(), reasonNames);
            return reasonNames;
        } catch (Exception e) {
            log.error("获取退货原因列表失败", e);
            // 降级策略：返回默认列表
            return getDefaultReturnReasons();
        }
    }

    /**
     * 默认退货原因列表（降级策略）
     * 当数据库查询失败时使用
     * 
     * @return 默认的退货原因列表
     */
    private List<String> getDefaultReturnReasons() {
        log.warn("使用默认退货原因列表（降级策略）");
        return java.util.Arrays.asList(
                "质量问题",
                "尺码太大",
                "颜色不喜欢",
                "7天无理由退货",
                "其他"
        );
    }
}
