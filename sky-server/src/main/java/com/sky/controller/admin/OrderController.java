package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.core.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author raoxin
 */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理接口")
public class OrderController {
     @Autowired
     private OrderService orderService;

     @GetMapping("/details/{id}")
     @ApiOperation("根据id查询订单详情")
     public Result<OrderVO> getOrderDetailById(@PathVariable Long id) {
          OrderVO orderVO = orderService.getOrderVOById(id);
          log.info("查询订单详情：{}", orderVO);
          return Result.success(orderVO);
     }

     @GetMapping("/conditionSearch")
     @ApiOperation("订单搜索")
     public Result<PageResult> getOrderByCondition(OrdersPageQueryDTO ordersPageQueryDTO) {
          log.info("订单搜索：{}", ordersPageQueryDTO);
          PageResult pageResult = orderService.getOrdersByCondition(ordersPageQueryDTO);
          return Result.success(pageResult);
     }

     @GetMapping("/statistics")
     @ApiOperation("统计订单信息")
     public Result<OrderStatisticsVO> getOrderStatistics() {
          OrderStatisticsVO orderStatisticsVO = orderService.getOrderStatistics();
          log.info("统计订单信息：{}", orderStatisticsVO);
          return Result.success(orderStatisticsVO);
     }

     @PutMapping("confirm")
     @ApiOperation("确认订单")
     public Result confirm(@RequestBody @Valid OrdersConfirmDTO ordersConfirmDTO) {
          log.info("确认订单：{}", ordersConfirmDTO);
          orderService.confirm(ordersConfirmDTO.getId());
          return Result.success();
     }

     @PutMapping("delivery/{id}")
     @ApiOperation("派送订单")
     public Result delivery(@PathVariable Long id) {
          log.info("派送订单：{}", id);
          orderService.delivery(id);
          return Result.success();
     }

     @PutMapping("rejection")
     @ApiOperation("拒绝订单")
     public Result rejection(@RequestBody @Valid OrdersRejectionDTO ordersRejectionDTO) throws Exception {
          log.info("拒绝订单：{}", ordersRejectionDTO);
          orderService.rejectionOrder(ordersRejectionDTO);
          return Result.success();
     }

     @PutMapping("cancel")
     @ApiOperation("取消订单")
     public Result cancel(@RequestBody @Valid OrdersCancelDTO ordersCancelDTO){
          log.info("取消订单：{}", ordersCancelDTO);
          orderService.adminCancelOrder(ordersCancelDTO);
          return Result.success();
     }

     @PutMapping("complete/{id}")
     @ApiOperation("完成订单")
     public Result complete(@PathVariable Long id) {
          log.info("完成订单：{}", id);
          orderService.complete(id);
          return Result.success();
     }
}
