package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.profit.track.dto.RoleRequest;
import com.profit.track.dto.RoleResponse;
import com.profit.track.entity.SysRole;
import com.profit.track.mapper.SysRoleMapper;
import com.profit.track.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<RoleResponse> listRoles() {
        List<SysRole> roles = list();
        return roles.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public RoleResponse createRole(RoleRequest request) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, request.getRoleCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(request, role);
        LocalDateTime now = LocalDateTime.now();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        save(role);
        return toResponse(role);
    }

    @Override
    public RoleResponse updateRole(RoleRequest request) {
        SysRole role = getById(request.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        BeanUtils.copyProperties(request, role, "id", "createdAt");
        role.setUpdatedAt(LocalDateTime.now());
        updateById(role);
        return toResponse(role);
    }

    @Override
    public void deleteRole(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        if ("ADMIN".equals(role.getRoleCode())) {
            throw new RuntimeException("不允许删除超级管理员角色");
        }
        removeById(id);
    }

    private RoleResponse toResponse(SysRole role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setRoleCode(role.getRoleCode());
        response.setRoleLevel(role.getRoleLevel());
        response.setDescription(role.getDescription());
        response.setStatus(role.getStatus());
        response.setCreatedAt(role.getCreatedAt());
        return response;
    }
}
