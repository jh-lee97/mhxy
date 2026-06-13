package com.profit.track.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收益记录新增/编辑请求 DTO
 */
@Data
public class ProfitRecordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID（编辑时必填） */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 日期 */
    private String date;

    /** 玩法模式 */
    private String mode;

    /** 活动名称 */
    private String activity;

    /** 常规收入(梦幻币) */
    private BigDecimal income;

    /** 藏宝阁变现 */
    private BigDecimal cbgIncome;

    /** 道具/装备价值 */
    private BigDecimal propIncome;

    /** 总成本 */
    private BigDecimal cost;

    /** 备注 */
    private String remark;
}