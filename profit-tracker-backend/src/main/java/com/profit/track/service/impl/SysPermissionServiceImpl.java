package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.track.dto.MenuTreeNode;
import com.profit.track.dto.PermissionResponse;
import com.profit.track.entity.SysPermission;
import com.profit.track.entity.SysRole;
import com.profit.track.entity.SysRolePermission;
import com.profit.track.mapper.SysPermissionMapper;
import com.profit.track.mapper.SysRoleMapper;
import com.profit.track.mapper.SysRolePermissionMapper;
import com.profit.track.mapper.SysUserRoleMapper;
import com.profit.track.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<SysPermission> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream()
                .map(SysPermission::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return roleIds.stream()
                .map(id -> {
                    SysRole role = sysRoleMapper.selectById(id);
                    return role != null ? role.getRoleCode() : null;
                })
                .filter(roleCode -> roleCode != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuTreeNode> getMenuTree(Long userId) {
        List<SysPermission> allPermissions = permissionMapper.selectByUserId(userId);
        
        // 过滤出菜单类型（type=1）且启用的
        List<SysPermission> menus = allPermissions.stream()
                .filter(p -> p.getType() != null && p.getType() == 1 && p.getStatus() != null && p.getStatus() == 1)
                .sorted(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0))
                .collect(Collectors.toList());

        // 构建树形结构
        return buildMenuTree(menus, null);
    }

    @Override
    public List<MenuTreeNode> getAllMenuTree() {
        // 获取所有启用的菜单类型权限
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getType, 1)
               .eq(SysPermission::getStatus, 1)
               .orderByAsc(SysPermission::getSortOrder);
        List<SysPermission> menus = permissionMapper.selectList(wrapper);
        return buildMenuTree(menus, null);
    }

    /** 构建菜单树 */
    private List<MenuTreeNode> buildMenuTree(List<SysPermission> menus, Long parentId) {
        List<MenuTreeNode> result = new ArrayList<>();
        
        for (SysPermission menu : menus) {
            Long menuParentId = menu.getParentId();
            boolean isChild = (parentId == null && menuParentId != null && menuParentId > 0) ||
                              (parentId != null && parentId.equals(menuParentId));
            
            if (parentId == null && (menuParentId == null || menuParentId == 0)) {
                MenuTreeNode node = new MenuTreeNode();
                node.setId(menu.getId());
                node.setParentId(menu.getParentId());
                node.setName(menu.getName());
                node.setPath(menu.getPath());
                node.setCode(menu.getCode());
                node.setIcon(menu.getIcon());
                node.setSortOrder(menu.getSortOrder());
                
                List<MenuTreeNode> children = buildMenuTree(menus, menu.getId());
                node.setChildren(children);
                
                result.add(node);
            } else if (isChild) {
                MenuTreeNode node = new MenuTreeNode();
                node.setId(menu.getId());
                node.setParentId(menu.getParentId());
                node.setName(menu.getName());
                node.setPath(menu.getPath());
                node.setCode(menu.getCode());
                node.setIcon(menu.getIcon());
                node.setSortOrder(menu.getSortOrder());
                
                List<MenuTreeNode> children = buildMenuTree(menus, menu.getId());
                node.setChildren(children);
                
                result.add(node);
            }
        }
        
        return result;
    }

    @Override
    public List<PermissionResponse> getPermissionsByRoleId(Long roleId) {
        List<SysPermission> permissions = permissionMapper.selectByRoleId(roleId);
        return permissions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 先删除该角色所有权限
        LambdaQueryWrapper<SysRolePermission> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(SysRolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(delWrapper);

        // 批量插入新权限
        for (Long permissionId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId)
               .eq(SysRolePermission::getPermissionId, permissionId);
        rolePermissionMapper.delete(wrapper);
    }

    @Override
    public List<PermissionResponse> listAllPermissions() {
        List<SysPermission> permissions = permissionMapper.selectList(null);
        return permissions.stream()
                .map(this::toResponse)
                .sorted(Comparator.comparingInt(PermissionResponse::getSortOrder))
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse createPermission(PermissionResponse req) {
        SysPermission perm = new SysPermission();
        perm.setParentId(req.getParentId());
        perm.setName(req.getName());
        perm.setCode(req.getCode());
        perm.setType(req.getType());
        perm.setPath(req.getPath());
        perm.setMethod(req.getMethod());
        perm.setSortOrder(req.getSortOrder());
        perm.setIcon(req.getIcon());
        perm.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        
        permissionMapper.insert(perm);
        return toResponse(perm);
    }

    @Override
    public PermissionResponse updatePermission(PermissionResponse req) {
        SysPermission perm = permissionMapper.selectById(req.getId());
        if (perm == null) {
            throw new RuntimeException("权限不存在");
        }
        
        if (req.getName() != null) perm.setName(req.getName());
        if (req.getCode() != null) perm.setCode(req.getCode());
        if (req.getType() != null) perm.setType(req.getType());
        if (req.getPath() != null) perm.setPath(req.getPath());
        if (req.getMethod() != null) perm.setMethod(req.getMethod());
        if (req.getSortOrder() != null) perm.setSortOrder(req.getSortOrder());
        if (req.getIcon() != null) perm.setIcon(req.getIcon());
        if (req.getStatus() != null) perm.setStatus(req.getStatus());
        if (req.getParentId() != null) perm.setParentId(req.getParentId());
        
        permissionMapper.updateById(perm);
        return toResponse(perm);
    }

    @Override
    public void deletePermission(Long id) {
        permissionMapper.deleteById(id);
    }

    private PermissionResponse toResponse(SysPermission perm) {
        PermissionResponse response = new PermissionResponse();
        response.setId(perm.getId());
        response.setParentId(perm.getParentId());
        response.setName(perm.getName());
        response.setCode(perm.getCode());
        response.setType(perm.getType());
        response.setPath(perm.getPath());
        response.setMethod(perm.getMethod());
        response.setSortOrder(perm.getSortOrder());
        response.setIcon(perm.getIcon());
        response.setStatus(perm.getStatus());
        response.setCreatedAt(perm.getCreatedAt());
        response.setUpdatedAt(perm.getUpdatedAt());
        return response;
    }
}
