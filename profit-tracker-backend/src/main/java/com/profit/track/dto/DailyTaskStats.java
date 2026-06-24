package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 每日任务统计
 */
@Data
@Schema(description = "每日任务统计")
public class DailyTaskStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "星期几：1-7")
    private Integer dayOfWeek;

    @Schema(description = "任务总数")
    private Integer totalTasks;

    @Schema(description = "已完成数")
    private Integer completedTasks;

    @Schema(description = "跳过数")
    private Integer skippedTasks;

    @Schema(description = "完成率(%)")
    private Double completionRate;

    @Schema(description = "预估收入(万)")
    private BigDecimal estimatedIncome;

    @Schema(description = "实际收入(万)")
    private BigDecimal actualIncome;

    @Schema(description = "预估耗时(小时)")
    private BigDecimal estimatedTime;

    @Schema(description = "实际耗时(小时)")
    private BigDecimal actualTime;
}
