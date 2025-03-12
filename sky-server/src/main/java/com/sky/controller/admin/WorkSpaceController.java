package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author raoxin
 */
@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags = "工作台相关接口")
public class WorkSpaceController {
    @Autowired
    private WorkSpaceService workSpaceService;

    @GetMapping("/overviewDishes")
    @ApiOperation("菜品总览")
    public Result<DishOverViewVO> getOverviewDishes() {
        DishOverViewVO dishOverViewVO = workSpaceService.getOverviewDishes();
        return Result.success(dishOverViewVO);
    }

    @GetMapping("/overviewSetmeals")
    @ApiOperation("套餐总览")
    public Result<SetmealOverViewVO> getOverviewSetmeal() {
        SetmealOverViewVO setmealOverViewVO = workSpaceService.getSetMealOverView();
        return Result.success(setmealOverViewVO);
    }

    @GetMapping("/overviewOrders")
    @ApiOperation("订单总览")
    public Result<OrderOverViewVO> getOverviewOrders() {
        OrderOverViewVO orderOverViewVO = workSpaceService.getOverviewOrders();
        return Result.success(orderOverViewVO);
    }

    @GetMapping("businessData")
    @ApiOperation("获取营业额数据")
    public Result<BusinessDataVO> getBusinessData() {
        LocalDate now = LocalDate.now();
        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(now,now);
        return Result.success(businessDataVO);
    }

}
