package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @author raoxin
 */

public interface ReportService {
    @Transactional
    OrderReportVO getOrderStatisticsDuringPeriod(LocalDate begin, LocalDate end);

    @Transactional
    TurnoverReportVO getTurnoverStatisticsDuringPeriod(LocalDate begin, LocalDate end);

    @Transactional
    UserReportVO getUserStatisticsDuringPeriod(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10ReportVO(LocalDate begin, LocalDate end);
}
