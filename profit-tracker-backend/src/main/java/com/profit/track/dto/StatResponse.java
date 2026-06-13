package com.profit.track.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 统计卡片数据
 */
@Data
public class StatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 今日收入 */
    private BigDecimal todayIncome;
    /** 今日成本 */
    private BigDecimal todayCost;
    /** 今日净利 */
    private BigDecimal todayProfit;

    /** 本周收入 */
    private BigDecimal weekIncome;
    /** 本周成本 */
    private BigDecimal weekCost;
    /** 本周净利 */
    private BigDecimal weekProfit;

    /** 累计收入 */
    private BigDecimal totalIncome;
    /** 累计成本 */
    private BigDecimal totalCost;
    /** 累计净利 */
    private BigDecimal totalProfit;

    /** 记录总数 */
    private Long totalRecords;
}