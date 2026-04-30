package com.macro.mall.portal.component;

import com.macro.mall.portal.service.OmsPortalOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时取消定时任务
 * 定期扫描数据库中超过指定时间未支付的订单，执行取消操作并释放库存
 */
@Component
public class OrderTimeOutCancelTask {
    private final Logger LOGGER = LoggerFactory.getLogger(OrderTimeOutCancelTask.class);
    @Autowired
    private OmsPortalOrderService portalOrderService;

    /**
     * 定时任务方法，每10分钟执行一次
     * cron表达式：Seconds Minutes Hours DayOfMonth Month DayOfWeek [Year]
     * 扫描超时未支付订单，进行取消操作并释放锁定库存
     */
    @Scheduled(cron = "0 0/10 * ? * ?")
    private void cancelTimeOutOrder(){
        Integer count = portalOrderService.cancelTimeOutOrder();
        LOGGER.info("取消订单，并根据sku编号释放锁定库存，取消订单数量：{}",count);
    }
}
