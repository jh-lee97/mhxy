package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Schema(description = "攻略创建/更新请求")
public class GameGuideRequest {

    @Schema(description = "分类")
    @NotBlank(message = "分类不能为空")
    private String category;

    @Schema(description = "攻略标题")
    @NotBlank(message = "标题不能为空")
    @Size(max = 200)
    private String title;

    @Schema(description = "摘要")
    @Size(max = 500)
    private String summary;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
