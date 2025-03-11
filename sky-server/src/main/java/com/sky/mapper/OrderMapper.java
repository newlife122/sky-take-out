package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
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

    Integer getCntByStatusAndPeriod(LocalDateTime begin, LocalDateTime end, Integer status);

    Double getTurnOverByPeriod(LocalDateTime begin, LocalDateTime end);

    List<GoodsSalesDTO> getTop10Order(LocalDateTime begin, LocalDateTime end);
}
