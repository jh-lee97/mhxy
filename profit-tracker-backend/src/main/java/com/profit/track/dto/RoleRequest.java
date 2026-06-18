package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "角色创建/更新请求")
public class RoleRequest {

    @Schema(description = "角色ID，创建时留空")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", example = "ADMIN")
    private String roleCode;

    @Schema(description = "描述", example = "系统管理员")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
