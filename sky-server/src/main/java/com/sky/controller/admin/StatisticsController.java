package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * @author raoxin
 */
@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "数据统计接口")
public class StatisticsController {

    @Autowired
    private ReportService reportService;
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计接口")
    public Result<OrderReportVO> ordersStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        OrderReportVO orderReportVO = reportService.getOrderStatisticsDuringPeriod(begin,end);
        return Result.success(orderReportVO);
    }

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计接口")
    public Result<TurnoverReportVO> turnoverStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatisticsDuringPeriod(begin, end);
        return Result.success(turnoverReportVO);
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户统计接口")
    public  Result<UserReportVO> userStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        UserReportVO userReportVO = reportService.getUserStatisticsDuringPeriod(begin, end);
        return Result.success(userReportVO);
    }

    @GetMapping("/top10")
    @ApiOperation("销量排名")
    public Result<SalesTop10ReportVO> top10(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        SalesTop10ReportVO salesTop10ReportVO = reportService.getSalesTop10ReportVO(begin, end);
        return Result.success(salesTop10ReportVO);
    }

    @GetMapping("/export")
    @ApiOperation("导出数据")
    public void export(HttpServletResponse response){
        reportService.exprotExcel(response);
    }
}
