package com.profit.track.controller;

import com.profit.track.dto.RoleRequest;
import com.profit.track.dto.RoleResponse;
import com.profit.track.dto.Result;
import com.profit.track.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色的增删改查")
public class RoleController {

    private final SysRoleService sysRoleService;

    /** 获取所有角色 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有角色", description = "返回系统中所有角色列表")
    public Result<List<RoleResponse>> list() {
        List<RoleResponse> roles = sysRoleService.listRoles();
        return Result.ok(roles);
    }

    /** 创建角色 */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建角色", description = "创建一个新的角色")
    public Result<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = sysRoleService.createRole(request);
        return Result.ok(role);
    }

    /** 更新角色 */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新角色", description = "更新一个已存在的角色")
    public Result<RoleResponse> update(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = sysRoleService.updateRole(request);
        return Result.ok(role);
    }

    /** 删除角色 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除角色", description = "根据 ID 删除角色")
    public Result<Void> delete(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.ok();
    }

    /** 获取角色列表（含等级信息） */
    @GetMapping("/levels")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取角色等级列表", description = "返回所有角色及其等级，用于前端角色分配")
    public Result<List<RoleResponse>> listRolesWithLevels() {
        List<RoleResponse> roles = sysRoleService.listRoles();
        return Result.ok(roles);
    }
}
