package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 任务计划创建/更新请求
 */
@Data
@Schema(description = "任务计划请求")
public class TaskPlanRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "计划ID（更新时必填）")
    private Long id;

    @Schema(description = "计划名称", example = "2025年第26周")
    private String planName;

    @Schema(description = "所属年份")
    private Integer year;

    @Schema(description = "所属周数")
    private Integer weekNumber;

    @Schema(description = "周一日期", example = "2025-06-23")
    private String startDate;

    @Schema(description = "周日日期", example = "2025-06-29")
    private String endDate;

    @Schema(description = "状态：0-停用，1-进行中，2-已完成")
    private Integer status;

    @Schema(description = "每日任务列表")
    private List<DayTask> dayTasks;

    @Data
    public static class DayTask implements Serializable {
        @Schema(description = "星期几：1-7")
        private Integer dayOfWeek;

        @Schema(description = "任务名称")
        private String taskName;

        @Schema(description = "分类：日常/副本/活动/其他")
        private String category;

        @Schema(description = "预估收入(万)")
        private BigDecimal estimatedIncome;

        @Schema(description = "预估耗时(小时)")
        private BigDecimal estimatedTime;

        @Schema(description = "描述")
        private String description;

        @Schema(description = "排序")
        private Integer sortOrder;

        @Schema(description = "是否必做：0-选做，1-必做")
        private Integer mandatory;
    }
}
