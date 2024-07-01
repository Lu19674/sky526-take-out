package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@Api(tags = "数据统计相关接口")
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     *
     * @return
     */
    @ApiOperation("营业额统计")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        log.info("营业额统计：{}--{}", begin, end);
        TurnoverReportVO vo = reportService.turnoverStatistics(begin, end);
        return Result.success(vo);
    }

    /**
     * 用户数量统计
     *
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("用户数量统计")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        log.info("用户数量统计：{}=={}", begin, end);
        UserReportVO vo = reportService.userStatistics(begin, end);
        return Result.success(vo);
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("订单数据统计")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("订单数据统计，{}--{}",begin,end);
        OrderReportVO vo= reportService.ordersStatistics(begin,end);
        return Result.success(vo);
    }

    /**
     * 订单销量排名top10统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("商品销量排名top10统计")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10Statistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("商品销量top10统计，{}--{}",begin,end);
        SalesTop10ReportVO vo= reportService.top10Statistics(begin,end);
        return Result.success(vo);
    }

    /**
     * 到处excel运营数据报表
     * @param response
     */
    @ApiOperation("导出excel运营数据报表")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        reportService.export(response);
    }
}
