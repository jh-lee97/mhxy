package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收益记录新增/编辑请求 DTO
 */
@Data
@Schema(description = "收益记录新增/编辑请求")
public class ProfitRecordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID（编辑时必填）")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "日期，格式 yyyy-MM-dd")
    private String date;

    @Schema(description = "玩法模式：副本/捉鬼/刷宝图/日常任务/活动/跑商/其他")
    private String mode;

    @Schema(description = "活动名称")
    private String activity;

    @Schema(description = "常规收入(梦幻币，单位：万)")
    private BigDecimal income;

    @Schema(description = "藏宝阁变现(单位：万)")
    private BigDecimal cbgIncome;

    @Schema(description = "道具/装备价值(单位：万)")
    private BigDecimal propIncome;

    @Schema(description = "总成本(单位：万)")
    private BigDecimal cost;

    @Schema(description = "备注")
    private String remark;
}