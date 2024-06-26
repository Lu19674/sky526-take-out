package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //存储 开始日期（begin） 到 结束日期（end） 之间的每一天日期集合
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //计算后一天的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //使用lang3中 StringsUtils 工具类构建指定格式（“，”分隔）的 dateStr
        String dateStr = StringUtils.join(dateList, ",");

        //存储 每一天的营业额
        List<Double> amountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //统计每一天的营业额
            //订单列表条件：状态为 已完成 ，下单时间为 date 这一天时间段内
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//这一天的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//这一天的结束时间

            Map<String, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin", beginTime);
            map.put("end", endTime);
            //执行查询
            Double amount = orderMapper.sumByMap(map);
            if (amount == null) {
                amount = 0.0;
            }
            amountList.add(amount);
        }
        String amountStr = StringUtils.join(amountList, ",");

        //封装VO返回

        return TurnoverReportVO.builder()
                .dateList(dateStr)
                .turnoverList(amountStr)
                .build();
    }

    /**
     * 用户数量统计
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        //日期列表串
        String dateStr = StringUtils.join(dateList, ",");

        List<Integer> totalUserList = new ArrayList<>();//存储每天用户累计总量集合
        List<Integer> newUserList = new ArrayList<>();//存储每天新增用户数量集合
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//此天的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//此天的结束时间
            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            //先只传 endTime 来查询截至的总用户量
            Integer allSum = userMapper.countByMap(map);
            map.put("begin", beginTime);
            //beginTime和endTime同时传参 - 查询当天用户量 即新增用户量
            Integer newSum = userMapper.countByMap(map);

            allSum = allSum == null ? 0 : allSum;
            newSum = newSum == null ? 0 : newSum;
            totalUserList.add(allSum);
            newUserList.add(newSum);
        }
        //总用户量和新增用户量列表串
        String totalUserStr = StringUtils.join(totalUserList, ",");
        String newUserStr = StringUtils.join(newUserList, ",");

        //封装vo返回
        return UserReportVO.builder()
                .dateList(dateStr)
                .totalUserList(totalUserStr)
                .newUserList(newUserStr)
                .build();
    }

    /**
     * 订单数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        //日期列表串
        String dateStr = StringUtils.join(dateList, ",");

        List<Integer> validOrderCountList = new ArrayList<>(); //存储每日有效订单数集合
        List<Integer> orderCountList = new ArrayList<>(); //存储每日订单数集合
        Integer validOrderCountSum = 0;//有效订单总数
        Integer orderCountSum = 0;//订单总数

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//此天的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//此天的结束时间
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);

            //先不传参 status 来查询每天总订单数
            Integer orderCount = orderMapper.countByMap(map);

            map.put("status", Orders.COMPLETED);
            //传参status查询每天总有效订单数
            Integer validOrderCount = orderMapper.countByMap(map);

            orderCount = orderCount == null ? 0 : orderCount;
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;

            //计算时间段总订单数和有效订单数
            orderCountSum += orderCount;
            validOrderCountSum += validOrderCount;

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //订单完成率
        Double orderCompletionRate = orderCountSum == 0 ?
                0.0 : validOrderCountSum.doubleValue() / orderCountSum;
        //每天有效订单数列表串
        String validOrderCountStr = StringUtils.join(validOrderCountList, ",");
        //每天订单列表串
        String orderCountStr = StringUtils.join(orderCountList, ",");

        //封装vo返回
        return OrderReportVO.builder()
                .dateList(dateStr)
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(orderCountStr)
                .validOrderCountList(validOrderCountStr)
                .validOrderCount(validOrderCountSum)
                .totalOrderCount(orderCountSum)
                .build();
    }

    /**
     * 商品销量排名top10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO top10Statistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);//开始时间
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);//结束时间

        Map<String,Object> map=new HashMap<>();
        map.put("begin",beginTime);
        map.put("end",endTime);
        map.put("status",Orders.COMPLETED);
        //查询订单列表
        List<Orders> ordersList = orderMapper.getByMap(map);

        //存放订单id集
        List<Long> ordersIds= new ArrayList<>();
        for (Orders orders : ordersList) {
            ordersIds.add(orders.getId());
        }

        //根据订单id集批量查询订单明细top10列表
        List<GoodsSalesDTO> goodsList  =orderDetailMapper.queryTop10ByOederIds(ordersIds);

        //存放商品名列表
        List<String> nameList =new ArrayList<>();
        //存放商品销售数量列表
        List<Integer> numberList =new ArrayList<>();

        //遍历订单明细top10列表，拿出商品名及销售数量存如对应集合
        for (GoodsSalesDTO good : goodsList) {
            nameList.add(good.getName());
            numberList.add(good.getNumber());
        }

        //封装VO返回
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**
     * 导出excel运营数据报表
     * @param response
     */
    public void export(HttpServletResponse response) {
        //1.查询前30天运营数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDateTime begin =LocalDateTime.of(beginDate,LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate,LocalTime.MAX);
        //查询数据（ WorkspaceService 已经实现）
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);

        //2.读取excel模板文件并修改
        try {
            FileInputStream in = new FileInputStream(new File("H:\\CodeDeom\\java\\526take_out\\sky-take-out\\sky-server\\src\\main\\resources\\template\\运营数据报表模板.xlsx"));
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填入时间
            XSSFRow row = sheet.getRow(1);
            row.getCell(1).setCellValue(String.valueOf("时间："+beginDate+"至"+endDate));

            //填入概览数据
            row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填入明细数据
            for (int i = 0; i < 30; i++) {
                //查询当天营业数据
                LocalDateTime dateTimeBegin = LocalDateTime.of(beginDate.plusDays(i), LocalTime.MIN);
                LocalDateTime dateTimeEnd = LocalDateTime.of(beginDate.plusDays(i), LocalTime.MAX);
                businessData = workspaceService.getBusinessData(dateTimeBegin, dateTimeEnd);
                //填入对应表格
                row = sheet.getRow(i + 7);
                row.getCell(1).setCellValue(beginDate.plusDays(i).toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());

            }

            //3.输出excel表格到客户端
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //4.关闭资源
            out.close();
            excel.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
