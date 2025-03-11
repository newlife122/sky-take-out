package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @author raoxin
 */
public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    OrderVO getOrderVOById(Long id);

    void userCancelOrder(Long id) throws Exception;

    void paySuccess(String outTradeNo);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    PageResult getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    void repeatOrder(Long id);

    PageResult getOrdersByCondition(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO getOrderStatistics();

    void confirm(Long id);

    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);

    void adminCancelOrder(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void complete(Long id);

    void checkOutOfRange(String destination);

    void cancelTimeOutOrder();

    void setDiliveryingCompleted();

    void reminder(Long id);
}
