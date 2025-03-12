package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author raoxin
 */
@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Transactional
    @Override
    public DishOverViewVO getOverviewDishes() {
        Integer enableDishCnt = dishMapper.selectDishCountByStatus(StatusConstant.ENABLE);
        Integer disableSaleDishCnt = dishMapper.selectDishCountByStatus(StatusConstant.DISABLE);
        DishOverViewVO dishOverViewVO = DishOverViewVO.builder()
                .sold(enableDishCnt)
                .discontinued(disableSaleDishCnt)
                .build();
        return dishOverViewVO;
    }

    @Transactional
    @Override
    public SetmealOverViewVO getSetMealOverView() {
        Integer enableDishCnt = setMealMapper.selectSetMealCountByStatus(StatusConstant.ENABLE);
        Integer disableSaleDishCnt = setMealMapper.selectSetMealCountByStatus(StatusConstant.DISABLE);
        SetmealOverViewVO setmealOverViewVO = SetmealOverViewVO.builder()
                .sold(enableDishCnt)
                .discontinued(disableSaleDishCnt)
                .build();

        return setmealOverViewVO;
    }

    @Transactional
    @Override
    public OrderOverViewVO getOverviewOrders() {
        Integer allOrders = orderMapper.getOrderCountByStatus(null);
        Integer cancelledOrders = orderMapper.getOrderCountByStatus(Orders.CANCELLED);
        Integer completedOrders = orderMapper.getOrderCountByStatus(Orders.COMPLETED);
        Integer deliveredOrders = orderMapper.getOrderCountByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer waitingOrders = orderMapper.getOrderCountByStatus(Orders.TO_BE_CONFIRMED);
        OrderOverViewVO orderOverViewVO = OrderOverViewVO.builder()
                .allOrders(allOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .build();
        return orderOverViewVO;
    }

    @Override
    public BusinessDataVO getBusinessData(LocalDate begin, LocalDate end) {
        LocalDateTime bg = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime ed = LocalDateTime.of(end, LocalTime.MAX);
        //1.这段时间的用户总数
        //2.这段时间的订单总数
        //3.这段时间的总营业额
        //4.这段时间的有效订单总数
        //5. 可以计算出的 订单完成率 = 有效订单总数 / 订单总数
        //6. 可以计算出的 平均客单价 = 总营业额 / 有效订单总数
        Integer userCnt = userMapper.getUserCntByPeriod(bg, ed);
        Integer orderCnt = orderMapper.getCntByStatusAndPeriod(bg, ed,Orders.COMPLETED);
        Double turnover = orderMapper.getTurnOverByPeriod(bg, ed);
        Integer validOrderCount = orderMapper.getCntByStatusAndPeriod(bg, ed,Orders.COMPLETED);
        userCnt = userCnt==null?0:userCnt;
        orderCnt = orderCnt==null?0:orderCnt;
        turnover = turnover==null?0.0:turnover;
        validOrderCount = validOrderCount==null?0:validOrderCount;

        Double orderCompletionRate = 0.0;
        Double unitPrice = 0.0;
        if(orderCnt!=0){
            unitPrice = turnover/orderCnt;
        }
        if (orderCnt!=0){
            orderCompletionRate = validOrderCount/(double)orderCnt;
        }

        BusinessDataVO businessDataVO = BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(userCnt)
                .build();
        return businessDataVO;
    }
}
