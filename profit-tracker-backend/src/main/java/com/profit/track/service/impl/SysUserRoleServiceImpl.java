package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.track.dto.*;
import com.profit.track.entity.SysPermission;
import com.profit.track.entity.SysRolePermission;
import com.profit.track.entity.SysRole;
import com.profit.track.entity.SysUser;
import com.profit.track.entity.SysUserRole;
import com.profit.track.mapper.SysPermissionMapper;
import com.profit.track.mapper.SysRoleMapper;
import com.profit.track.mapper.SysRolePermissionMapper;
import com.profit.track.mapper.SysUserMapper;
import com.profit.track.mapper.SysUserRoleMapper;
import com.profit.track.service.SysPermissionService;
import com.profit.track.service.SysUserRoleService;
import com.profit.track.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl implements SysUserRoleService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final JwtUtil jwtUtil;

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        // 检查是否已存在
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId);
        if (sysUserRoleMapper.selectCount(wrapper) > 0) {
            return; // 已存在，不重复添加
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId);
        sysUserRoleMapper.delete(wrapper);
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return sysUserRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public LoginResponse enrichLoginResponse(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取用户角色
        List<Long> roleIds = getUserRoleIds(userId);
        List<String> roleCodes = new ArrayList<>();
        String primaryRoleName = "";
        int maxRoleLevel = 0;

        for (Long roleId : roleIds) {
            SysRole role = sysRoleMapper.selectById(roleId);
            if (role != null) {
                roleCodes.add(role.getRoleCode());
                if (role.getRoleLevel() != null && role.getRoleLevel() > maxRoleLevel) {
                    maxRoleLevel = role.getRoleLevel();
                    primaryRoleName = role.getRoleName();
                }
            }
        }

        if (roleCodes.isEmpty()) {
            roleCodes = Collections.singletonList("USER");
            primaryRoleName = "普通用户";
        }

        // 获取权限标识
        List<String> permissions = sysPermissionMapper.selectByUserId(userId)
                .stream()
                .map(SysPermission::getCode)
                .collect(Collectors.toList());

        // 生成包含权限的新 Token
        String token = jwtUtil.generateToken(userId, user.getUsername(), permissions, roleCodes);

        return new LoginResponse(
                token,
                userId,
                user.getUsername(),
                user.getNickname(),
                roleIds.isEmpty() ? null : roleIds.get(0),
                maxRoleLevel,
                primaryRoleName,
                permissions,
                roleCodes
        );
    }

    @Override
    public UserInfoResponse getUserInfoWithRoles(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

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
        List<Long> roleIds = getUserRoleIds(userId);
        if (!roleIds.isEmpty()) {
            response.setRoleId(roleIds.get(0));
            SysRole primaryRole = sysRoleMapper.selectById(roleIds.get(0));
            if (primaryRole != null) {
                response.setRoleName(primaryRole.getRoleName());
                response.setRoleLevel(primaryRole.getRoleLevel());
            }
        }

        return response;
    }
}
