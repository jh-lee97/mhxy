package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户权限响应")
public class UserPermissionResponse {

    @Schema(description = "权限标识列表")
    private List<String> permissions;

    @Schema(description = "角色编码列表")
    private List<String> roles;

    @Schema(description = "菜单树")
    private List<MenuTreeNode> menus;
}
