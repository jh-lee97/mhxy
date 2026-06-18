package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 统计卡片数据
 */
@Data
@Schema(description = "统计数据响应")
public class StatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "今日收入")
    private BigDecimal todayIncome;
    @Schema(description = "今日成本")
    private BigDecimal todayCost;
    @Schema(description = "今日净利")
    private BigDecimal todayProfit;

    @Schema(description = "本周收入")
    private BigDecimal weekIncome;
    @Schema(description = "本周成本")
    private BigDecimal weekCost;
    @Schema(description = "本周净利")
    private BigDecimal weekProfit;

    @Schema(description = "累计收入")
    private BigDecimal totalIncome;
    @Schema(description = "累计成本")
    private BigDecimal totalCost;
    @Schema(description = "累计净利")
    private BigDecimal totalProfit;

    @Schema(description = "记录总数")
    private Long totalRecords;
}