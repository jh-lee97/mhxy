package com.profit.track.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.profit.track.dto.*;
import com.profit.track.entity.SysPermission;
import com.profit.track.entity.SysRole;
import com.profit.track.entity.SysUser;
import com.profit.track.mapper.SysRoleMapper;
import com.profit.track.mapper.SysUserMapper;
import com.profit.track.service.SysPermissionService;
import com.profit.track.service.SysRoleService;
import com.profit.track.service.SysUserRoleService;
import com.profit.track.service.SysUserService;
import com.profit.track.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "后台管理", description = "管理台接口，仅管理员可访问")
public class AdminController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;
    private final SysUserRoleService sysUserRoleService;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ==================== 用户管理 ====================

    /** 分页查询用户列表 */
    @GetMapping("/users")
    @Operation(summary = "分页查询用户", description = "支持关键词搜索")
    public Result<PageResponse<UserInfoResponse>> listUsers(PageRequest request) {
        Page<SysUser> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String kw = "%" + request.getKeyword().trim() + "%";
            wrapper.and(w -> w.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getNickname, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> resultPage = sysUserMapper.selectPage(page, wrapper);

        List<UserInfoResponse> records = resultPage.getRecords().stream()
                .map(this::toUserInfoResponse)
                .collect(Collectors.toList());

        PageResponse<UserInfoResponse> pageResponse = new PageResponse<>(
                records, resultPage.getTotal(), request.getPage(), request.getSize(),
                (int) Math.ceil((double) resultPage.getTotal() / request.getSize())
        );
        return Result.ok(pageResponse);
    }

    /** 禁用/启用用户 */
    @PutMapping("/users/{userId}/status")
    @Operation(summary = "启用/禁用用户")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        sysUserService.updateUserStatus(userId, status);
        return Result.ok();
    }

    /** 分配角色给用户 */
    @PutMapping("/users/{userId}/role")
    @Operation(summary = "分配角色")
    public Result<Void> assignRole(
            @PathVariable Long userId,
            @RequestParam Long roleId) {
        sysUserService.assignRole(userId, roleId);
        return Result.ok();
    }

    /** 管理员重置用户密码 */
    @PostMapping("/users/{userId}/reset-password")
    @Operation(summary = "重置用户密码")
    public Result<Void> resetUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
        return Result.ok();
    }

    /** 删除用户 */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        sysUserService.deleteUser(userId);
        return Result.ok();
    }

    // ==================== 角色管理 ====================

    /** 获取所有角色 */
    @GetMapping("/roles")
    @Operation(summary = "获取所有角色")
    public Result<List<RoleResponse>> listRoles() {
        return Result.ok(sysRoleService.listRoles());
    }

    /** 创建角色 */
    @PostMapping("/roles")
    @Operation(summary = "创建角色")
    public Result<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return Result.ok(sysRoleService.createRole(request));
    }

    /** 更新角色 */
    @PutMapping("/roles/{id}")
    @Operation(summary = "更新角色")
    public Result<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        request.setId(id);
        return Result.ok(sysRoleService.updateRole(request));
    }

    /** 删除角色 */
    @DeleteMapping("/roles/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.ok();
    }

    /** 获取角色的权限列表 */
    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "获取角色权限")
    public Result<List<PermissionResponse>> getRolePermissions(@PathVariable Long roleId) {
        return Result.ok(sysPermissionService.getPermissionsByRoleId(roleId));
    }

    /** 为角色分配权限 */
    @PutMapping("/roles/{roleId}/permissions")
    @Operation(summary = "分配角色权限")
    public Result<Void> assignPermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody RolePermissionRequest request) {
        sysPermissionService.assignPermissionsToRole(roleId, request.getPermissionIds());
        return Result.ok();
    }

    // ==================== 菜单管理 ====================

    /** 获取菜单树（按用户权限过滤） */
    @GetMapping("/menus")
    @Operation(summary = "获取菜单树", description = "根据当前用户权限返回可访问的菜单树")
    public Result<List<MenuTreeNode>> getMenuTree(
            javax.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.ok(sysPermissionService.getMenuTree(userId));
    }

    /** 获取所有菜单（管理员，不分权限） */
    @GetMapping("/menus/all")
    @Operation(summary = "获取所有菜单", description = "返回系统中所有启用的菜单（管理员专用）")
    public Result<List<MenuTreeNode>> listAllMenus() {
        return Result.ok(sysPermissionService.getAllMenuTree());
    }

    /** 创建菜单 */
    @PostMapping("/menus")
    @Operation(summary = "创建菜单")
    public Result<PermissionResponse> createMenu(@Valid @RequestBody PermissionResponse request) {
        request.setType(1); // 强制类型为菜单
        return Result.ok(sysPermissionService.createPermission(request));
    }

    /** 更新菜单 */
    @PutMapping("/menus/{id}")
    @Operation(summary = "更新菜单")
    public Result<PermissionResponse> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody PermissionResponse request) {
        request.setId(id);
        request.setType(1); // 强制类型为菜单
        return Result.ok(sysPermissionService.updatePermission(request));
    }

    /** 删除菜单 */
    @DeleteMapping("/menus/{id}")
    @Operation(summary = "删除菜单")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        sysPermissionService.deletePermission(id);
        return Result.ok();
    }

    // ==================== 权限管理 ====================

    /** 获取所有权限（树形结构） */
    @GetMapping("/permissions")
    @Operation(summary = "获取所有权限")
    public Result<List<PermissionResponse>> listPermissions() {
        return Result.ok(sysPermissionService.listAllPermissions());
    }

    /** 创建权限 */
    @PostMapping("/permissions")
    @Operation(summary = "创建权限")
    public Result<PermissionResponse> createPermission(@Valid @RequestBody PermissionResponse request) {
        return Result.ok(sysPermissionService.createPermission(request));
    }

    /** 更新权限 */
    @PutMapping("/permissions/{id}")
    @Operation(summary = "更新权限")
    public Result<PermissionResponse> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionResponse request) {
        request.setId(id);
        return Result.ok(sysPermissionService.updatePermission(request));
    }

    /** 删除权限 */
    @DeleteMapping("/permissions/{id}")
    @Operation(summary = "删除权限")
    public Result<Void> deletePermission(@PathVariable Long id) {
        sysPermissionService.deletePermission(id);
        return Result.ok();
    }

    // ==================== 工具方法 ====================

    private UserInfoResponse toUserInfoResponse(SysUser user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());

        // 获取用户角色
        List<Long> roleIds = sysUserRoleService.getUserRoleIds(user.getId());
        if (!roleIds.isEmpty()) {
            SysRole primaryRole = sysRoleMapper.selectById(roleIds.get(0));
            if (primaryRole != null) {
                response.setRoleId(primaryRole.getId());
                response.setRoleName(primaryRole.getRoleName());
                response.setRoleCode(primaryRole.getRoleCode());
                response.setRoleLevel(primaryRole.getRoleLevel());
            }
        }
        return response;
    }
}
