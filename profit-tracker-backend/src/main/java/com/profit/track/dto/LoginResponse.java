package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色等级")
    private Integer roleLevel;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "权限标识列表")
    private List<String> permissions;

    @Schema(description = "角色编码列表")
    private List<String> roles;
}
