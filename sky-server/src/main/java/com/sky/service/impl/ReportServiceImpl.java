package com.sky.service.impl;

import com.sky.dto.ExcelDuring30DaysDTO;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author raoxin
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkSpaceService workSpaceService;


    /**
     * 数据统计的接口
     * @param begin 开始时间
     * @param end 结束时间
     * @return
     */
    @Transactional
    @Override
    public OrderReportVO getOrderStatisticsDuringPeriod(LocalDate begin, LocalDate end) {
        // TODO 修改传入参数为LocalDateTime
        //      1.返回开始到结束 每一天的日期 每天的日期压入链表，然后最后转换为字符串
        //      2.订单完成率 = 订单完成数量/订单总数
        //      3.订单数字符串 每天查询出数量，然后压入链表，然后转换为字符串
        //      4.计算订单总数，根据链表转为流，然后进行统计
        //      5.计算有效订单数 就是订单完成的数量，那么还要统计一个有效订单数，根据6的链表转为流然后统计
        //      6.有效订单数链表 就是完成的订单，每天根据日期查询

        List<LocalDate> localDateList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();
        List<Integer> orderCntList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        Double orderCompletionRate = 0.0;
        while (!begin.isAfter(end)){
            //1.求出区间 就是 开始时间 +1天
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            localDateList.add(begin);
            Integer tempValidOrderCnt   = orderMapper.getCntByStatusAndPeriod(beginTime,endTime, Orders.COMPLETED); //注意，如果当天没有订单会查询出0还是null
            Integer tempOrderCnt        = orderMapper.getCntByStatusAndPeriod(beginTime,endTime,null);
            tempValidOrderCnt           = tempValidOrderCnt==null?0:tempValidOrderCnt;
            tempOrderCnt                = tempOrderCnt==null?0:tempOrderCnt;
            validOrderList.add(tempValidOrderCnt);
            orderCntList.add(tempOrderCnt);
            totalOrderCount += tempOrderCnt;
            validOrderCount += tempValidOrderCnt;
            begin = begin.plusDays(1);
        }
        //因为分母有几率为0，所以要注意除0异常
        orderCompletionRate = totalOrderCount.doubleValue() == 0.0? 0.0 : validOrderCount.doubleValue()/totalOrderCount.doubleValue();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(localDateList.stream().map(date -> date.format(dateTimeFormatter)).collect(Collectors.joining(",")))
                .orderCountList(orderCntList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .validOrderCountList(validOrderList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
        return orderReportVO;
    }

    /**
     * 统计这段时间内的营业额
     * @param begin
     * @param end
     * @return
     */
    @Transactional
    @Override
    public TurnoverReportVO getTurnoverStatisticsDuringPeriod(LocalDate begin, LocalDate end) {
        // 可以先统计每一天的一个日期，加到列表中
        // 然后每遍历出一天，就去统计一天的所有营业额，将其放到链表中
        List<LocalDate> localDateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        while(!begin.isAfter(end)){
            localDateList.add(begin);
            begin = begin.plusDays(1L);
        }
        localDateList.forEach(
                dayBegin -> {
                    LocalDateTime beginTime = LocalDateTime.of(dayBegin, LocalTime.MIN);
                    LocalDateTime endTime = LocalDateTime.of(dayBegin, LocalTime.MAX);
                    Double turnover = orderMapper.getTurnOverByPeriod(beginTime,endTime);
                    turnoverList.add(turnover==null?0.0:turnover);
                }
        );

        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(localDateList.stream().map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .collect(Collectors.joining(",")))
                .turnoverList(turnoverList.stream().map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .build();

        return turnoverReportVO;
    }

    @Transactional
    @Override
    public UserReportVO getUserStatisticsDuringPeriod(LocalDate begin, LocalDate end) {
        // 1.得到每天的日期，创建一个链表
        // 2.根据日期去遍历，得到每天的新增用户数，然后压入链表 [begin,end)
        // 3.根据每天的最后时间作为截至时间 (-无穷,today+1) 统计人数
        List<LocalDate> localDateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        while(!begin.isAfter(end)){
            localDateList.add(begin);
            begin = begin.plusDays(1L);
        }

        localDateList.forEach(
                dayBegin -> {
                    LocalDateTime beginTime = LocalDateTime.of(dayBegin, LocalTime.MIN);
                    LocalDateTime endTime = LocalDateTime.of(dayBegin, LocalTime.MAX);
                    Integer newUserCnt = userMapper.getUserCntByPeriod(beginTime,endTime);
                    Integer totalUserCnt = userMapper.getUserCntByPeriod(null,endTime);
                    newUserList.add(newUserCnt==null?0:newUserCnt);
                    totalUserList.add(totalUserCnt==null?0:totalUserCnt);
                }
        );


        return UserReportVO.builder()
                .dateList(localDateList.stream().map(date->date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).collect(Collectors.joining(",")))
                .newUserList(newUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .totalUserList(totalUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }

    /**
     * 统计销量前10的菜品和套餐
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10ReportVO(LocalDate begin, LocalDate end) {
        // 这里直接找到order_detail 然后内连接，根据order的status = 完成的条件查询，然后limit查找前10
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getTop10Order(beginTime,endTime);
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(",")))
                .numberList(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).map(String::valueOf).collect(Collectors.joining(",")))
                .build();
        return salesTop10ReportVO;
    }

    /**
     * 近30天的运营数据导出
     * @param response
     */
    @Transactional
    @Override
    public void exprotExcel(HttpServletResponse response) {
        // 这个查询的数据在WorkSpaceService中有，所以通过这个也可以查询,先查询总的
        // step
        // 1. 查询出近30天的日期，创建一个链表
        // 2. 根据每日日期遍历得到每天的数据存入excel中
        // 3. 根据整体范围统计数据，存入excel中

        LocalDate today = LocalDate.now();
        LocalDate end =  today.minusDays(1L);
        LocalDate thirtyDaysAgo = today.minusDays(30);
        List<LocalDate> localDateList = new ArrayList<>();
        while(!thirtyDaysAgo.isAfter(end)){
            localDateList.add(thirtyDaysAgo);
            thirtyDaysAgo = thirtyDaysAgo.plusDays(1L);
        }

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //查询整体数据
        BusinessDataVO businessData = workSpaceService.getBusinessData(today.minusDays(30L),end);
        Sheet sheet = workbook.getSheetAt(0);
        //分别获得营业额单元格 订单完成率单元格 新增用户单元格 都在row4
        //分别获得有效订单 平均客单价 都在row5
        Row row4 = sheet.getRow(4-1);
        Row row5 = sheet.getRow(5-1);
        row4.getCell(2).setCellValue(businessData.getTurnover());
        row4.getCell(4).setCellValue(businessData.getOrderCompletionRate());
        row4.getCell(6).setCellValue(businessData.getNewUsers());
        row5.getCell(2).setCellValue(businessData.getValidOrderCount());
        row5.getCell(4).setCellValue(businessData.getUnitPrice());

        for (int i = 0,startRow = 7; i < localDateList.size();i++,startRow++){
            LocalDate tempBg = localDateList.get(i);
            LocalDate tempEnd = tempBg;
            BusinessDataVO businessData1 = workSpaceService.getBusinessData(tempBg, tempEnd);
            Row row = sheet.getRow(startRow);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            row.getCell(1).setCellValue(tempBg.format(dateTimeFormatter));
            row.getCell(2).setCellValue(businessData1.getTurnover());
            row.getCell(3).setCellValue(businessData1.getValidOrderCount());
            row.getCell(4).setCellValue(businessData1.getOrderCompletionRate());
            row.getCell(5).setCellValue(businessData1.getUnitPrice());
            row.getCell(6).setCellValue(businessData1.getNewUsers());
        }

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
