package com.profit.track.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 图表数据（近7日趋势）
 */
@Data
public class ChartResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期 */
    private String date;
    /** 收入 */
    private BigDecimal income;
    /** 成本 */
    private BigDecimal cost;
    /** 净利润 */
    private BigDecimal profit;
}