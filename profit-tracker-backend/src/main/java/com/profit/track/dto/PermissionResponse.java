package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "权限信息响应")
public class PermissionResponse {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "父级权限ID")
    private Long parentId;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "权限标识")
    private String code;

    @Schema(description = "类型：1-菜单 2-按钮 3-接口")
    private Integer type;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "HTTP方法")
    private String method;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "子权限列表")
    private List<PermissionResponse> children;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
