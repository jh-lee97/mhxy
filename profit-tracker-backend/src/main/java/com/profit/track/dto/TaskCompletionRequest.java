package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 任务完成/更新请求
 */
@Data
@Schema(description = "任务完成请求")
public class TaskCompletionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务清单ID")
    private Long checklistId;

    @Schema(description = "完成日期 yyyy-MM-dd")
    private String completeDate;

    @Schema(description = "实际收入(万)")
    private BigDecimal actualIncome;

    @Schema(description = "实际耗时(小时)")
    private BigDecimal actualTime;

    @Schema(description = "完成状态：0-未完成，1-已完成，2-跳过")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
