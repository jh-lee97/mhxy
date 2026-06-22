package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Schema(description = "角色权限分配请求")
public class RolePermissionRequest {

    @Schema(description = "权限ID列表", required = true)
    @NotEmpty(message = "权限列表不能为空")
    private List<Long> permissionIds;
}
