package com.profit.track.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收益记录查询响应 DTO
 */
@Data
public class ProfitRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String date;
    private String mode;
    private String activity;
    private BigDecimal income;
    private BigDecimal cbgIncome;
    private BigDecimal propIncome;
    private BigDecimal cost;
    private String remark;
    private String createdAt;
    private String updatedAt;
}