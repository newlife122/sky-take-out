package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.BaiDuLocationUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author raoxin
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private BaiDuLocationUtil baiDuLocationUtil;

    /**
     * 创建提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        // TODO 未进行检查
        //0.
        //  0.1检查处理前端传来的参数 如何检查，地址是否为空，还有购物车是否为空
        //  0.2设置后端应该设置的参数
        //  0.3插入order的同时获得orderid 然后根据获得的购物车列表设置orderdetail 同时删除所有的购物车信息(根据userId)
        //  0.4生成返回信息

        //1
        //根据id找到用户
        //找到地址的详情
        //地址不完整或者不是本人抛出异常
        //购物车为空抛出异常

        //前端传递的参数 共9个
        //这些参数是前端直接提供的，在下单时由用户选择或输
        //address_book_id（地址 ID）
        //amount（订单金额）
        //remark（备注信息）
        //estimated_delivery_time（预计送达时间）
        //delivery_status（配送状态）
        //pack_amount（打包费）
        //tableware_number（餐具数量）
        //tableware_status（餐具数量状态）
        //pay_method（支付方式）✅ 如果前端选择了支付方式，则传递
        //将属性拷贝进对象

        Long currentId = BaseContext.getCurrentId();
        User user = userMapper.selectById(currentId);
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        // TODO 根据地址请求距离，如果超过5000，那么就要抛出异常
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        checkOutOfRange(address);

        if(addressBook == null || addressBook.getUserId() != user.getId() ||addressBook.getPhone() == null || addressBook.getDetail()==null){
            throw new RuntimeException("地址信息不完整");
        }
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByUserId(currentId);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new RuntimeException("购物车为空");
        }
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);


        // 2. 后端设置参数（共 10 个）
        // 这些参数由后端逻辑自动生成或从数据库查询：
        // id（订单 ID，后端自动生成）
        // number（订单号，后端生成）
        // status（订单状态，默认 "1待付款"）
        // user_id（用户 ID，从会话获取）
        // order_time（下单时间，后端生成）
        // pay_status（支付状态，默认未支付 0）
        // phone（手机号，从地址簿获取）
        // user_name（用户名，从数据库获取）
        // consignee（收货人，从地址簿获取）
        //address（详细地址信息）
        //设置用户id

        //生成订单号 时间戳+userId+3位随机数
        String orderNumber = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + currentId
                + String.format("%03d", new Random().nextInt(1000));


        //设置订单状态为待付款  1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        order.setNumber(orderNumber);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setUserId(currentId);
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
        order.setPhone(addressBook.getPhone());
        order.setUserName(user.getName());
        order.setConsignee(addressBook.getConsignee());
        order.setAddress(addressBook.getDetail());

        //3.插入order的同时获得orderid 然后根据获得的购物车列表设置orderdetail 同时删除所有的购物车信息(根据userId)
        orderMapper.insert(order);
        Long orderId = order.getId();
        List<OrderDetail> orderDetailList = new LinkedList<>();
        //循环将order_detail的参数设置好，注意将id清空还有将order_id设置好
        shoppingCartList.forEach(
                shoppingCart -> {
                    OrderDetail orderDetail = new OrderDetail();
                    BeanUtils.copyProperties(shoppingCart, orderDetail);
                    orderDetail.setOrderId(orderId);
                    orderDetail.setId(null);
                    orderDetailList.add(orderDetail);
                }
        );

        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartMapper.deleteByUserId(currentId);

        //
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orderNumber)
                .orderAmount(ordersSubmitDTO.getAmount())
                .orderTime(LocalDateTime.now())
                .build();
        return orderSubmitVO;
    }


    /**
     * 根据id查询订单,用户端，因为要检查这个订单是都属于这个用户
     * @param id
     * @return
     */
    @Transactional
    @Override
    public OrderVO getOrderVOById(Long id) {
        //1.根据id查询订单，注意这里要判断这个订单是否是属于这个user的
        Long currentId = BaseContext.getCurrentId();

        Orders order =  orderMapper.selectById(id);

        if(order == null || order.getId() == null){
            throw new RuntimeException("订单不存在");
        }
        if(!BaseContext.getIsAdmin()){
            if( order.getUserId() == null || !order.getUserId().equals(currentId)){
                throw new RuntimeException("订单不属于你");
            }
        }

        List<OrderDetail> orderDetailList =  orderDetailMapper.selectByOrderId(order.getId());

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Transactional
    @Override
    public void userCancelOrder(Long id) throws Exception {
        // TODO 取消订单 这里取消分几种情况 如果是待付款的订单，直接取消
        // 根据id查询订单
        Orders ordersDB = orderMapper.selectById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders);

    }

    /**
     * 支付成功后更改订单状态
     * @param outTradeNo
     */
    @Transactional
    @Override
    public void paySuccess(String outTradeNo) {
        //1.修改订单状态
        Long currentId = BaseContext.getCurrentId();
        Orders order = orderMapper.selectByUserIdAndNumber(currentId, outTradeNo);
        //既要该支付状态，也要改状态，还有支付时间
        order.setPayStatus(Orders.PAID);
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setCheckoutTime(LocalDateTime.now());
        orderMapper.updateById(order);

        //2.进行来单提醒
        JSONObject webSocketMessageFrame = new JSONObject();
        webSocketMessageFrame.put("type", WebSocketServer.TYPE_ORDER_COMMING);
        webSocketMessageFrame.put("orderId", order.getNumber());
        webSocketMessageFrame.put("content","来单提醒");
        WebSocketServer.sendToAll(webSocketMessageFrame.toString());
    }

    @Transactional
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

        //       进行来单提醒 仅仅作为测试，实际上要支付成功以后才会有提醒，这里，这里申请支付就提醒了 ，这块内容应该在支付成功后的回调函数中
        //        JSONObject webSocketMessageFrame = new JSONObject();
        //        webSocketMessageFrame.put("type", WebSocketServer.TYPE_ORDER_COMMING);
        //        webSocketMessageFrame.put("orderId", 123456);
        //        webSocketMessageFrame.put("content","来单提醒");
        //        WebSocketServer.sendToAll(webSocketMessageFrame.toString());

        //调用微信支付接口，生成预支付交易单
        //会对请求进行加密，还有对给小程序的数据签名，让前端验证签名，看数据是否被更改
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        //如果已经支付，返回的响应体中会有code字段，值为ORDERPAID，则说明已经支付了
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }
    @Transactional
    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        Long currentId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(currentId);
        //使用pageHelp，然后mapper层使用resultmap 用left join拼接，会有问题，因为拼接后的结果条数比较多，然后映射到OrderVO是没有问题的,这个limit会直接限制为拼接后的条数，所以这里会报错
        //但是这里统计总的大小是根据查询的结果来统计的
        //        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //        Page<OrderVO> page = orderMapper.selectAllOrderVOByUserIdAndStatus(ordersPageQueryDTO);
        //        PageResult pageResult = PageResult.builder()
        //                .total(page.getResult().size())//注意，这里一定要这样写，这样写也有问题，因为limit是拼接以后，我们想要的是最后的结果的limit
        //                .records(page.getResult())
        //                .build();

        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.selectAllOrderVOWithoutListByConditions(ordersPageQueryDTO);
        List<OrderVO> orderVOList = page.getResult();
        if (orderVOList!=null && orderVOList.size()>0){
            orderVOList.forEach(
                    orderVO -> {
                        //查询订单详情
                        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orderVO.getId());
                        orderVO.setOrderDetailList(orderDetailList);
                    }
            );
        }
        PageResult pageResult = PageResult.builder()
                .total(page.getTotal())
                .records(orderVOList)
                .build();
        return pageResult;
    }


    @Transactional
    @Override
    public void repeatOrder(Long id) {
        //1.根据userid和id查询订单
        //2.查询出所有的订单详情
        //3.根据订单详情来插入套餐
        Long currentId = BaseContext.getCurrentId();
        Orders orders = orderMapper.selectById(id);
        if (orders == null){
            throw new OrderBusinessException("订单不存在");
        }

        //只有用户才需要进行这一步的验证，管理员就不用
        if(!BaseContext.getIsAdmin()){
            if (!orders.getUserId().equals(currentId)){
                throw new OrderBusinessException("订单不属于你");
            }
        }


        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(id);
        if (orderDetailList == null || orderDetailList.size() == 0){
            throw new OrderBusinessException("订单详情不存在");
        }
        List<ShoppingCart> shoppingCartList =
                        orderDetailList.stream().map(
                                orderDetail -> {
                                    ShoppingCart shoppingCart = new ShoppingCart();
                                    //注意，这里的id不可以赋值,所以要忽略
                                    BeanUtils.copyProperties(orderDetail,shoppingCart,"id");
                                    shoppingCart.setCreateTime(LocalDateTime.now());
                                    shoppingCart.setUserId(currentId);
                                    return shoppingCart;
                                }
                        ).collect(Collectors.toList());
//        orderDetailList.forEach(
//                orderDetail -> {
//                    ShoppingCart shoppingCart = new ShoppingCart();
//                    BeanUtils.copyProperties(orderDetail,shoppingCart);
//                    shoppingCart.setCreateTime(LocalDateTime.now());
//                    shoppingCart.setUserId(currentId);
//                    shoppingCartList.add(shoppingCart);
//                }
//        );

        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    @Transactional
    @Override
    public PageResult getOrdersByCondition(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.selectAllOrderVOWithoutListByConditions(ordersPageQueryDTO);
        // TODO 注意查询出来，我们要将OrderVO中orderDetailList中的名字进行一个拼接
//        List<OrderVO> orderVOList = page.getResult();
//        if (orderVOList!=null && orderVOList.size()>0){
//            orderVOList.forEach(
//                    orderVO -> {
//                        //查询订单详情
//                        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orderVO.getId());
//                        orderVO.setOrderDetailList(orderDetailList);
//                    }
//            );
//        }
        if(page.getResult()!=null){
            page.getResult().forEach(
                    orderVO -> {
                        //根据每一个orderVO的id查询相应的orderDetailList
                        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orderVO.getId());
                        orderVO.setOrderDetailList(orderDetailList);
                        StringBuilder orderDishes = new StringBuilder();
                        if(orderDetailList != null){
                            orderDetailList.forEach(
                                    orderDetail -> {
                                        //这里是将其拼接为字符串返回给前端
                                        orderDishes.append(orderDetail.getName()).append("*").append(orderDetail.getNumber()).append(",");
                                    }
                            );
                        }
                        orderVO.setOrderDishes(orderDishes.toString());
                    }
            );
        }
        PageResult pageResult = PageResult.builder()
                .total(page.getTotal())
                .records(page.getResult())
                .build();
        return pageResult;
    }

    @Transactional
    @Override
    public OrderStatisticsVO getOrderStatistics() {
        Integer confirmedCnt = orderMapper.getStatusCnt(Orders.CONFIRMED);
        Integer deliveryInProgressCnt = orderMapper.getStatusCnt(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmedCnt = orderMapper.getStatusCnt(Orders.TO_BE_CONFIRMED);

        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmedCnt)
                .confirmed(confirmedCnt)
                .deliveryInProgress(deliveryInProgressCnt)
                .build();
        return orderStatisticsVO;
    }

    /**
     * 确认订单
     * @param id
     */
    @Transactional
    @Override
    public void confirm(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null || orders.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException("还未付款或状态不对");
        }
        orders.setStatus(Orders.CONFIRMED);
        orders.setOrderTime(LocalDateTime.now());
        orderMapper.updateById(orders);
    }

    /**
     * 取消订单更新状态
     * @param ordersRejectionDTO
     */
    @Transactional
    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        //1.查询订单 不存在报错
        //2.如果订单状态是待确认才可以拒绝
        //3.退钱
        //4.设置订单状态  拒绝理由 取消时间
        //4.1支付状态不应该更改，应该等回调的时候再进行更改
        //4.同时还要退钱
        Orders orders = orderMapper.selectById(ordersRejectionDTO.getId());
        if (orders == null || orders.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException("订单不存在或状态不对");
        }
        // TODO 正式完成时退钱
//        if (orders.getPayMethod() == Orders.PAID){
//            try {
//                //outTradeNo – 商户订单号 outRefundNo – 商户退款单号 refund – 退款金额 total – 原订单金额
//                weChatPayUtil.refund(orders.getNumber(),
//                        orders.getNumber(),
//                        orders.getAmount(),
//                        orders.getAmount()
//                );
//            } catch (Exception e) {
//                throw new RuntimeException("退款失败");
//            }
//        }
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.updateById(orders);
    }

    /**
     * 管理员取消订单,在接单后取消
     * @param ordersCancelDTO 取消的订单id 和 时间的DTO
     */
    @Transactional
    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        // 1.查询订单 若订单不存在报错
        // 2.任何状态都可以取消订单
        // 3.如果是支付状态，要退款，其它不用
        // 4.设置 订单状态 取消时间 取消理由
        Orders orders = orderMapper.selectById(ordersCancelDTO.getId());
        if (orders == null){
            throw new OrderBusinessException("订单不存在");
        }
        if (orders.getStatus() == Orders.CANCELLED){
            throw new OrderBusinessException("订单已取消");
        }
        if(orders.getStatus() == Orders.COMPLETED){
            throw new OrderBusinessException("订单已完成");
        }
// TODO 正式完成时退钱
//        if(orders.getPayStatus() == Orders.PAID){
//            try {
//                //outTradeNo – 商户订单号 outRefundNo – 商户退款单号 refund – 退款金额 total – 原订单金额
//                weChatPayUtil.refund(orders.getNumber(),
//                        orders.getNumber(),
//                        orders.getAmount(),
//                        orders.getAmount()
//                );
//            } catch (Exception e) {
//                throw new RuntimeException("退款失败");
//            }
//        }
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.updateById(orders);

    }

    /**
     * 派送了，更新状态
     * @param id
     */
    @Transactional
    @Override
    public void delivery(Long id) {
        //1.查询订单，订单不存在报错
        //2.更改状态为配送中
        // TODO 不知道是否 要判断 用户如果选了送达时间，我们是不是要根据送达时间来配送
        Orders orders = orderMapper.selectById(id);
        if (orders == null || orders.getStatus() != Orders.CONFIRMED){
            throw new RuntimeException("订单不存在或还未接单");
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateById(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Transactional
    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null || orders.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw new RuntimeException("订单不存在或还未派送");
        }
        orders.setStatus(Orders.COMPLETED);
        orderMapper.updateById(orders);
    }

    @Override
    public void checkOutOfRange(String destination) {
        BaiDuLocationUtil.Location source = baiDuLocationUtil.getLocalLocation();
        BaiDuLocationUtil.Location des = baiDuLocationUtil.getLocation(destination,null);
        Integer distance = baiDuLocationUtil.getDistance(source,des);
        if (distance > 5000){
            throw new OrderBusinessException("超出配送范围");
        }
    }

    /**
     * 用来取消超时订单
     */
    @Transactional
    @Override
    public void cancelTimeOutOrder() {
        //1.查询状态为待接单的订单，并且下单时间小于等于30分钟前的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.selectAllOrdersByStatusAndTimeout(Orders.PENDING_PAYMENT,time);
        if (ordersList == null || ordersList.size()==0){
            return;
        }
        //设置 取消原因 取消时间 订单状态
        ordersList.forEach(
            orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.updateById(orders);
            }
        );
    }

    /**
     * 每天凌晨1点检查是否有派送中的订单
     */
    @Transactional
    @Override
    public void setDiliveryingCompleted() {
        List<Orders> ordersList = orderMapper.selectAllOrdersByStatusAndTimeout(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now());
        if (ordersList == null || ordersList.size()==0){
            return;
        }
        ordersList.forEach(
                orders -> {
                    orders.setStatus(Orders.COMPLETED);
                    orders.setDeliveryTime(LocalDateTime.now());
                    orderMapper.updateById(orders);
                }
        );
    }

    @Transactional
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.selectById(id);
        //只有订单在待接单 待配送 配种中 才可以被催单
        if (orders == null || orders.getStatus()!=Orders.TO_BE_CONFIRMED
                && orders.getStatus()!=Orders.CONFIRMED
                && orders.getStatus()!=Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException("订单不存在或还未派送");
        }
        JSONObject webSocketMessageFrame = new JSONObject();
        webSocketMessageFrame.put("type", WebSocketServer.TYPE_ORDER_REMIND);
        webSocketMessageFrame.put("orderId", orders.getId());
        webSocketMessageFrame.put("content","用户催单");
        WebSocketServer.sendToAll(webSocketMessageFrame.toString());
    }


}
