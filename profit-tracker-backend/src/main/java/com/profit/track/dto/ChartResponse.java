package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 图表数据（近7日趋势）
 */
@Data
@Schema(description = "图表数据响应")
public class ChartResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日期")
    private String date;
    @Schema(description = "收入")
    private BigDecimal income;
    @Schema(description = "成本")
    private BigDecimal cost;
    @Schema(description = "净利润")
    private BigDecimal profit;
}