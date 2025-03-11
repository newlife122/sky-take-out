package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFIll;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;


@Mapper
public interface OrderMapper {

    void insert(Orders order);

    Orders selectById(Long id);

    Orders selectByUserIdAndNumber(Long currentId, String outTradeNo);

    void updateById(Orders order);

    Page<OrderVO> selectAllOrderVOByUserIdAndStatus(OrdersPageQueryDTO ordersPageQueryDTO);

    Page<OrderVO> selectAllOrderVOByConditions(OrdersPageQueryDTO ordersPageQueryDTO);

    Integer getStatusCnt(Integer confirmed);

    Page<OrderVO> selectAllOrderVOWithoutListByConditions(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     *
     * @param status 状态
     * @param outOfTime 超时时间
     * @return
     */
    List<Orders> selectAllOrdersByStatusAndTimeout(Integer status, LocalDateTime outOfTime);
}
