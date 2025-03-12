package com.sky.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author raoxin
 */
@Data
@Builder
public class ExcelDuring30DaysDTO {
    LocalDate date;
    Double orderAmount;
    Integer validOrderCount;
    Double  completeRate;
    Double avgAmount;
    Integer newUserCount;
}
