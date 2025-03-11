package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author raoxin
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderServiceImpl orderService;
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 每分钟检查一下，下单的订单超时15分钟就要取消
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void cancelOrder() {
        log.info("定时检查是否有超时订单");
        orderService.cancelTimeOutOrder();
    }
    /**
     * 每天凌晨1点检查是否存在派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void setDiliveryingCompleted(){
        log.info("定时检查是否有派送中的订单");
        orderService.setDiliveryingCompleted();
    }
}
