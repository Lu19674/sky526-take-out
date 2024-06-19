package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1、处理各种业务异常
        //查询地址簿
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null)
            //地址簿信息为空异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        //查询当前用户购物车数据
        ShoppingCart cart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        cart.setUserId(userId);
        List<ShoppingCart> cartList = shoppingCartMapper.list(cart);
        if (cartList == null || cartList.size() == 0)
            //购物车为空异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        //2、向订单表插入1条数据
        Orders orders = Orders.builder()
                .userId(userId)
                .address(addressBook.getDetail())
                .number(String.valueOf(System.currentTimeMillis())) //设置订单好为时间戳串
                .status(Orders.PENDING_PAYMENT) //设置订单状态为 待支付
                .phone(addressBook.getPhone())
                .payStatus(Orders.UN_PAID) //设置支付状态为 未支付
                .orderTime(LocalDateTime.now())
                .consignee(addressBook.getConsignee())
                .build();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orderMapper.insert(orders);

        //3、向订单明细表插入n条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : cartList) {
            OrderDetail orderDetail = new OrderDetail(); //订单明细
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId()); //设置当前订单明细关联的订单id
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails); //批量插入

        //4、清空当前用户的购物车
        shoppingCartMapper.clearByUserId(userId);

        //5.封装OrderSubmitVO对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /**
         * 跳过微信支付接口调用
         */
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/

//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

        paySuccess(ordersPaymentDTO.getOrderNumber());
        OrderPaymentVO vo = new OrderPaymentVO();
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 根据订单状态分页查询订单
     *
     * @param
     * @return
     */
    public PageResult page(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = OrdersPageQueryDTO.builder()
                .status(status)
                .userId(BaseContext.getCurrentId())
                .build();
        //执行分页条查询
        Page<Orders> pages = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = new ArrayList<>();

        if (pages != null && pages.getTotal() > 0) {
            //遍历分页查询到的 orders 集合 查询每个订单的 详细信息（ordersDetail） 并完成封装
            for (Orders orders : pages) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetails);
                orderVOList.add(orderVO);
            }
        }

        assert pages != null;
        return new PageResult(pages.getTotal(), orderVOList);
    }

    /**
     * 根据订单id查询订单详情
     *
     * @param id
     * @return
     */
    public OrderVO getOrderDetail(Long id) {
        OrderVO orderVO = new OrderVO();
        //根据订单id查询订单
        Orders orders = orderMapper.getById(id);
        //根据订单id查询订单详情
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        //封装OrderVO返回
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    public void cancelById(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            //订单不存在异常
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Integer ordersStatus = orders.getStatus();//订单状态

        if (ordersStatus.equals(Orders.TO_BE_CONFIRMED)) {
            //待接单状态下取消订单，需要给用户退款（退款过程跳过）
            orders.setPayStatus(Orders.REFUND);
        } else if (ordersStatus.equals(Orders.CONFIRMED) || ordersStatus.equals(Orders.DELIVERY_IN_PROGRESS)) {
            //已接单 或 派送中 状态下取消订单，需要电话沟通商家
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setCancelTime(LocalDateTime.now());//订单取消时间
        orders.setCancelReason("用户取消");//订单取消原因
        orders.setStatus(Orders.CANCELLED);//修改订单状态
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Transactional
    public void repetitionById(Long id) {
        //查询订单明细列表
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        List<ShoppingCart> cartList = new ArrayList<>();

        //遍历 ，把此订单明细信息添加进购物车
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());//购物车单条数据的创建时间
            shoppingCart.setUserId(BaseContext.getCurrentId());//要给用户id 才能在购物车查到数据！（踩过的坑）
            cartList.add(shoppingCart);
        }
        //批量插入购物车数据
        shoppingCartMapper.insertBatch(cartList);
    }

    /**
     * 条件分页查询订单
     *
     * @param dto
     * @return
     */
    public PageResult pageByDTO(OrdersPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        //管理端要显示全部用户订单，无需设置用户id！！
//        dto.setUserId(BaseContext.getCurrentId());
        //执行分页条查询
        Page<Orders> pages = orderMapper.pageQuery(dto);
        List<OrderVO> orderVOList = new ArrayList<>();

        if (pages != null && pages.getTotal() > 0) {
            //遍历分页查询到的 orders 集合 查询每个订单的 详细信息（ordersDetail） 并完成封装
            for (Orders orders : pages) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

                //订单包含的菜品字段 OrderDishes，以String显示
                StringBuilder dishesSB = new StringBuilder();
                for (OrderDetail orderDetail : orderDetails) {
                    dishesSB.append(orderDetail.getName()).append("*").append(orderDetail.getNumber()).append(";");
                }
                String dishesString = dishesSB.toString();
                orderVO.setOrderDishes(dishesString);
                orderVO.setOrderDetailList(orderDetails);
                orderVOList.add(orderVO);
            }
        }

        assert pages != null;
        return new PageResult(pages.getTotal(), orderVOList);

    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    public OrderStatisticsVO queryStatistics() {
        //查出所有订单列表
        Page<Orders> orders = orderMapper.pageQuery(OrdersPageQueryDTO.builder().build());
        int toBeConfirmed = 0, confirmed = 0, deliveryInProgress = 0;

        //遍历得到每种状态的订单数量
        for (Orders order : orders) {
            if(order.getStatus().equals(Orders.TO_BE_CONFIRMED))
                //待接单
                toBeConfirmed++;
            else if(order.getStatus().equals(Orders.CONFIRMED))
                //待派送
                confirmed++;
            else if(order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS))
                //派送中
                deliveryInProgress++;
        }

        //封装返回结果
        return OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
    }

    /**
     * 商家接单
     * @param dto
     */
    public void confirm(OrdersConfirmDTO dto) {
        orderMapper.update(Orders.builder()
                .status(Orders.CONFIRMED)//修改订单状态为已接单（待派送）
                .id(dto.getId())
                .build());
    }

    /**
     * 商家拒单
      * @param dto
     */
    public void rejection(OrdersRejectionDTO dto) {
        Orders orders =orderMapper.getById(dto.getId());

        if(!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //不是待接单状态的订单不能拒单
            throw new OrderBusinessException(MessageConstant.UNKNOWN_ERROR);
        }
        if(orders.getPayStatus().equals(Orders.PAID)){
            //如果已支付。则把支付状态改为已退款（退款业务跳过）
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setRejectionReason(dto.getRejectionReason());//拒单原因
        orders.setStatus(Orders.CANCELLED); //订单状态设置为已取消
        orders.setCancelTime(LocalDateTime.now()); //取消时间
        orders.setCancelReason("商家拒单"); //取消原因
        //执行修改
        orderMapper.update(orders);

    }

    /**
     * 商家取消订单
     * @param dto
     */
    public void cancelByDTO(OrdersCancelDTO dto) {
        Orders orders = orderMapper.getById(dto.getId());
        if (orders == null) {
            //订单不存在异常
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Integer ordersStatus = orders.getStatus();//订单状态

        if (!ordersStatus.equals(Orders.TO_BE_CONFIRMED) || ordersStatus.equals(Orders.CANCELLED)) {
            //不是待接单/已取消状态下取消订单，需要给用户退款（退款过程跳过）
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setCancelTime(LocalDateTime.now());//订单取消时间
        orders.setCancelReason(dto.getCancelReason());//订单取消原因
        orders.setStatus(Orders.CANCELLED);//修改订单状态
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    public void delivery(Long id) {
        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS) //订单状态设置为派送中
                .build());
    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {
        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.COMPLETED) //订单状态设置为已完成
                .deliveryTime(LocalDateTime.now()) //订单送达时间设置为现在
                .build());
    }
}
