package com.profit.track.service;

import com.profit.track.dto.MenuTreeNode;
import com.profit.track.dto.PermissionResponse;

import java.util.List;

public interface SysPermissionService {

    /** 根据用户ID获取所有权限标识 */
    List<String> getUserPermissions(Long userId);

    /** 根据用户ID获取所有角色编码 */
    List<String> getUserRoleCodes(Long userId);

    /** 根据用户ID获取菜单树 */
    List<MenuTreeNode> getMenuTree(Long userId);

    /** 获取所有菜单树（管理员专用，不过滤权限） */
    List<MenuTreeNode> getAllMenuTree();

    /** 根据角色ID获取权限列表 */
    List<PermissionResponse> getPermissionsByRoleId(Long roleId);

    /** 为角色分配权限 */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /** 移除角色的某个权限 */
    void removePermissionFromRole(Long roleId, Long permissionId);

    /** 获取所有权限列表 */
    List<PermissionResponse> listAllPermissions();

    /** 创建权限 */
    PermissionResponse createPermission(PermissionResponse permission);

    /** 更新权限 */
    PermissionResponse updatePermission(PermissionResponse permission);

    /** 删除权限 */
    void deletePermission(Long id);
}
