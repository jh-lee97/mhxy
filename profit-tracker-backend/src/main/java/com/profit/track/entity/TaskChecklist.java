package com.profit.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 每日任务清单条目
 */
@Data
@TableName("task_checklist")
public class TaskChecklist implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属计划ID */
    private Long planId;

    /** 星期几：1=周一, 7=周日 */
    private Integer dayOfWeek;

    /** 任务名称，如"师门任务" */
    private String taskName;

    /** 任务分类：日常/副本/活动/其他 */
    private String category;

    /** 预估梦幻币收入(万) */
    private BigDecimal estimatedIncome;

    /** 预估耗时(小时) */
    private BigDecimal estimatedTime;

    /** 任务描述/备注 */
    private String description;

    /** 排序权重 */
    private Integer sortOrder;

    /** 是否必做：0-选做，1-必做 */
    private Integer mandatory;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
