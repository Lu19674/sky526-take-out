package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    PageResult page(int page,int pageSize,Integer status);

    OrderVO getOrderDetail(Long id);

    void cancelById(Long id);

    void repetitionById(Long id);

    PageResult pageByDTO(OrdersPageQueryDTO dto);

    OrderStatisticsVO queryStatistics();

    void confirm(OrdersConfirmDTO dto);

    void rejection(OrdersRejectionDTO dto);

    void cancelByDTO(OrdersCancelDTO dto);

    void delivery(Long id);

    void complete(Long id);

    void reminder(Long id);
}
