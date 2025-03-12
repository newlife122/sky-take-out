package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author raoxin
 */
public interface WorkSpaceService {
    DishOverViewVO getOverviewDishes();

    SetmealOverViewVO getSetMealOverView();

    OrderOverViewVO getOverviewOrders();


    BusinessDataVO getBusinessData(LocalDate begin, LocalDate end);
}
