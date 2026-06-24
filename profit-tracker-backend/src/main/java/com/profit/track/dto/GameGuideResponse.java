package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "攻略响应")
public class GameGuideResponse {

    @Schema(description = "攻略ID")
    private Long id;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "攻略标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
