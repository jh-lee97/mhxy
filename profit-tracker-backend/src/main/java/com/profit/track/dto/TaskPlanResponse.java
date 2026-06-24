package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务计划响应
 */
@Data
@Schema(description = "任务计划响应")
public class TaskPlanResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "计划ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "所属年份")
    private Integer year;

    @Schema(description = "所属周数")
    private Integer weekNumber;

    @Schema(description = "周一日期")
    private String startDate;

    @Schema(description = "周日日期")
    private String endDate;

    @Schema(description = "状态：0-停用，1-进行中，2-已完成")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "每日任务列表")
    private List<DayTaskResponse> dayTasks;

    @Data
    public static class DayTaskResponse implements Serializable {
        @Schema(description = "任务ID")
        private Long id;

        @Schema(description = "星期几：1-7")
        private Integer dayOfWeek;

        @Schema(description = "任务名称")
        private String taskName;

        @Schema(description = "分类")
        private String category;

        @Schema(description = "预估收入(万)")
        private BigDecimal estimatedIncome;

        @Schema(description = "预估耗时(小时)")
        private BigDecimal estimatedTime;

        @Schema(description = "描述")
        private String description;

        @Schema(description = "排序")
        private Integer sortOrder;

        @Schema(description = "是否必做")
        private Integer mandatory;

        @Schema(description = "当日已完成任务数")
        private Integer completedCount;

        @Schema(description = "当日任务总数")
        private Integer totalCount;

        @Schema(description = "当日完成率")
        private Double completionRate;
    }

    @Schema(description = "计划统计")
    @Data
    public static class PlanStats implements Serializable {
        @Schema(description = "总任务数")
        private Integer totalTasks;

        @Schema(description = "已完成任务数")
        private Integer completedTasks;

        @Schema(description = "完成率")
        private Double completionRate;

        @Schema(description = "预估总收入(万)")
        private BigDecimal estimatedIncome;

        @Schema(description = "预估总收入(万)")
        private BigDecimal actualIncome;

        @Schema(description = "预估总耗时(小时)")
        private BigDecimal estimatedTime;

        @Schema(description = "预估总耗时(小时)")
        private BigDecimal actualTime;
    }
}
