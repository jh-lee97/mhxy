package com.profit.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("profit_record")
public class ProfitRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 日期 */
    private String date;

    /** 玩法模式：副本/捉鬼/刷宝图/日常任务/活动/跑商/其他 */
    private String mode;

    /** 活动名称 */
    private String activity;

    /** 常规收入(梦幻币) */
    private BigDecimal income;

    /** 藏宝阁变现 */
    private BigDecimal cbgIncome;

    /** 道具/装备价值 */
    @TableField("prop_income")
    private BigDecimal propIncome;

    /** 总成本 */
    private BigDecimal cost;

    /** 备注 */
    private String remark;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private String createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedAt;
}
