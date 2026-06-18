package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收益记录查询响应 DTO
 */
@Data
@Schema(description = "收益记录响应")
public class ProfitRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "日期")
    private String date;
    @Schema(description = "玩法模式")
    private String mode;
    @Schema(description = "活动名称")
    private String activity;
    @Schema(description = "常规收入(梦幻币)")
    private BigDecimal income;
    @Schema(description = "藏宝阁变现")
    private BigDecimal cbgIncome;
    @Schema(description = "道具/装备价值")
    private BigDecimal propIncome;
    @Schema(description = "总成本")
    private BigDecimal cost;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private String createdAt;
    @Schema(description = "更新时间")
    private String updatedAt;
}