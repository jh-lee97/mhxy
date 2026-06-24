package com.profit.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 每周任务计划
 */
@Data
@TableName("task_plan")
public class TaskPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 计划名称，如"2025年第25周" */
    private String planName;

    /** 所属年份 */
    private Integer year;

    /** 所属周数 (1-53) */
    private Integer weekNumber;

    /** 周一日期 */
    private String startDate;

    /** 周日日期 */
    private String endDate;

    /** 状态：0-停用，1-进行中，2-已完成 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
