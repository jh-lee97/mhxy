package com.profit.track.controller;

import com.profit.track.dto.*;
import com.profit.track.service.SysPermissionService;
import com.profit.track.service.SysRoleService;
import com.profit.track.service.SysUserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证", description = "用户登录、注册、忘记密码")
public class PermissionController {

    private final SysPermissionService permissionService;
    private final SysUserRoleService sysUserRoleService;

    /** 获取当前用户的权限和菜单 */
    @GetMapping("/permissions")
    @Operation(summary = "获取当前用户权限", description = "返回权限标识列表、角色列表和菜单树")
    public Result<UserPermissionResponse> getUserPermissions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }

        List<String> permissions = permissionService.getUserPermissions(userId);
        List<String> roles = permissionService.getUserRoleCodes(userId);
        List<MenuTreeNode> menus = permissionService.getMenuTree(userId);

        UserPermissionResponse response = new UserPermissionResponse();
        response.setPermissions(permissions);
        response.setRoles(roles);
        response.setMenus(menus);
        return Result.ok(response);
    }

    /** 获取所有权限列表（管理员） */
    @GetMapping("/permissions/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有权限", description = "仅管理员可访问")
    public Result<List<PermissionResponse>> listAllPermissions() {
        return Result.ok(permissionService.listAllPermissions());
    }

    /** 获取角色的权限列表（管理员） */
    @GetMapping("/permissions/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取角色权限", description = "仅管理员可访问")
    public Result<List<PermissionResponse>> getPermissionsByRole(@PathVariable Long roleId) {
        return Result.ok(permissionService.getPermissionsByRoleId(roleId));
    }

    /** 为角色分配权限（管理员） */
    @PutMapping("/permissions/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分配角色权限", description = "仅管理员可访问")
    public Result<Void> assignPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        permissionService.assignPermissionsToRole(roleId, permissionIds);
        return Result.ok();
    }
}
