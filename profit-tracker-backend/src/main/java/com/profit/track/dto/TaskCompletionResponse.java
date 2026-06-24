package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务完成记录响应
 */
@Data
@Schema(description = "任务完成记录响应")
public class TaskCompletionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "任务清单ID")
    private Long checklistId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "星期几：1-7")
    private Integer dayOfWeek;

    @Schema(description = "完成日期")
    private String completeDate;

    @Schema(description = "实际收入(万)")
    private BigDecimal actualIncome;

    @Schema(description = "实际耗时(小时)")
    private BigDecimal actualTime;

    @Schema(description = "完成状态：0-未完成，1-已完成，2-跳过")
    private Integer status;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
