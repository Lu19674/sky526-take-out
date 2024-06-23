package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
/**
 * 订单状态定时处理
 */
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 订单待支付超时状态定时处理
     */
//    @Scheduled(cron = "1/10 * * * * ? ")
    @Scheduled(cron = "0 0/5 * * * ? ") //每五分钟自动执行
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        //超时订单时间满足：下单时间（order_time） < 现在时间-15分钟（outTime）
        LocalDateTime outTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getBystatusAndOrderTimeLT(Orders.PENDING_PAYMENT,outTime);

        if(ordersList !=null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED); //设置状态为取消
                orders.setCancelTime(LocalDateTime.now()); //取消时间
                orders.setCancelReason("订单未支付超时，已直动取消");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 派送中订单定时处理
     */
//    @Scheduled(cron = "0/10 * * * * ? ")
    @Scheduled(cron ="0 0 1 * * ? ")// 每天凌晨一点
    public void processDeliveryOrder(){
        log.info("定时处理派送中订单：{}",LocalDateTime.now());

        //查出上一个工作日派送中的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getBystatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,time);

        if(ordersList != null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
