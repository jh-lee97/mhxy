package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

@Data
@Schema(description = "分页查询请求")
public class PageRequest {

    @Schema(description = "页码，从1开始", example = "1")
    private int page = 1;

    @Schema(description = "每页大小", example = "20")
    private int size = 20;

    @Schema(description = "搜索关键词（用户名/昵称/手机号）")
    private String keyword;
}
