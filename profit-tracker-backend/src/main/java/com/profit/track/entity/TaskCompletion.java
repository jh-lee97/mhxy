package com.profit.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 每日任务完成记录
 */
@Data
@TableName("task_completion")
public class TaskCompletion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务清单ID */
    private Long checklistId;

    /** 所属计划ID */
    private Long planId;

    /** 用户ID */
    private Long userId;

    /** 完成日期 yyyy-MM-dd */
    private String completeDate;

    /** 实际收入(万) */
    private BigDecimal actualIncome;

    /** 实际耗时(小时) */
    private BigDecimal actualTime;

    /** 完成状态：0-未完成，1-已完成，2-跳过 */
    private Integer status;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
