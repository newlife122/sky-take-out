package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author raoxin
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端-订单管理接口")
public class OrderController {
     @Autowired
     private OrderService orderService;

     @PostMapping("/submit")
     @ApiOperation("用户下单")
     public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
          OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
          log.info("下单结果：{}", orderSubmitVO);
         return Result.success(orderSubmitVO);
     }

     @GetMapping("/orderDetail/{id}")
     @ApiOperation("根据id查询订单详情")
     public Result<OrderVO> getOrderDetailById(@PathVariable Long id) {
          OrderVO orderVO = orderService.getOrderVOById(id);
          log.info("查询订单详情：{}", orderVO);
          return Result.success(orderVO);
     }

     @PutMapping("/cancel/{id}")
     @ApiOperation("取消订单")
     public Result cancel(@PathVariable Long id) throws Exception {
          // TODO 未完成，待完善
          orderService.userCancelOrder(id);
          return Result.success();
     }

     @PutMapping("/payment")
     @ApiOperation("订单支付")
     public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
          OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
          log.info("支付结果：{}", orderPaymentVO);
          return Result.success(orderPaymentVO);
     }

     @GetMapping("historyOrders")
     @ApiOperation("查看历史订单")
     public Result<PageResult> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
          PageResult pageResult = orderService.getHistoryOrders(ordersPageQueryDTO);
          return Result.success(pageResult);
     }

     @PostMapping("/repetition/{id}")
     @ApiOperation("再来一单")
     public Result repetition(@PathVariable Long id) {
          orderService.repeatOrder(id);
          return Result.success();
     }

     @PutMapping("/rejection")
     @ApiOperation("拒绝订单")
     public Result rejection(@RequestBody @Valid OrdersRejectionDTO ordersRejectionDTO) throws Exception {
          orderService.rejectionOrder(ordersRejectionDTO);
          return Result.success();
     }

     @GetMapping("/reminder/{id}")
     @ApiOperation("订单催单")
     public Result reminder(@PathVariable(required = true) Long id) {
          orderService.reminder(id);
          return Result.success();
     }

}
