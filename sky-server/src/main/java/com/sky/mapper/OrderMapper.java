package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入1条订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据订单状态分页查询订单
     * @param
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 查询下单时间小于 time 的 status 状态订单列表
     * @param status
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getBystatusAndOrderTimeLT(Integer status, LocalDateTime time);

    /**
     * 根据订单状态查询订单
     * @param status
     * @return
     */
    @Select("select * from orders where status = #{status}")
    List<Orders> getBystatus(Integer status);

    /**
     * 根据时间端统计营业额
     * @param map
     * @return
     */
    Double sumByMap(Map<String, Object> map);

    /**
     * 根据时间段统计订单量数据
     * @param map
     * @return
     */
    Integer countByMap(Map<String, Object> map);

    /**
     * 根据时间段查询订单列表
     * @param map
     * @return
     */
    List<Orders> getByMap(Map<String, Object> map);
}
