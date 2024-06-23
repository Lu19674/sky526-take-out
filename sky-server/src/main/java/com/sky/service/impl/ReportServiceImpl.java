package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //存储 开始日期（begin） 到 结束日期（end） 之间的每一天日期集合
        List<LocalDate> dateList =new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            //计算后一天的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //使用lang3中 StringsUtils 工具类构建指定格式（“，”分隔）的 dateStr
        String dateStr = StringUtils.join(dateList, ",");

        //存储 每一天的营业额
        List<Double> amountList =new ArrayList<>();
        for (LocalDate date : dateList) {
            //统计每一天的营业额
            //订单列表条件：状态为 已完成 ，下单时间为 date 这一天时间段内
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);//这一天的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//这一天的结束时间

            Map<String,Object> map=new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin",beginTime);
            map.put("end",endTime);
            //执行查询
            Double amount = orderMapper.sumByMap(map);
            if(amount ==null){
                amount =0.0;
            }
            amountList.add(amount);
        }
        String amountStr = StringUtils.join(amountList,",");

        //封装VO返回
        return TurnoverReportVO.builder()
                .dateList(dateStr)
                .turnoverList(amountStr)
                .build();
    }

    /**
     * 用户数量统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(begin.equals(end)){
            dateList.add(begin);
            begin=begin.plusDays(1);
        }
        //日期列表串
        String dateStr=StringUtils.join(dateList,",");

        List<Integer> totalUserList=new ArrayList<>();//存储每天用户累计总量集合
        List<Integer> newUserList =new ArrayList<>();//存储每天新增用户数量集合
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);//此天的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//此天的结束时间
            Map<String,Object> map=new HashMap<>();
            map.put("end",endTime);
            //先只传 endTime 来查询截至的总用户量
            Integer allSum =  userMapper.sumByMap(map);
            map.put("begin",beginTime);
            //beginTime和endTime同时传参 - 查询当天用户量 即新增用户量
            Integer newSum = userMapper.sumByMap(map);

            totalUserList.add(allSum);
            newUserList.add(newSum);
        }
        //总用户量和新增用户量列表串
        String totalUserStr = StringUtils.join(totalUserList,",");
        String newUserStr = StringUtils.join(newUserList,",");

        //封装vo返回
        return UserReportVO.builder()
                .dateList(dateStr)
                .totalUserList(totalUserStr)
                .newUserList(newUserStr)
                .build();
    }
}
