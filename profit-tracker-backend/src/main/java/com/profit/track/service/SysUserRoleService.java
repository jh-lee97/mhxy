package com.profit.track.service;

import com.profit.track.dto.LoginResponse;
import com.profit.track.dto.UserInfoResponse;

import java.util.List;

public interface SysUserRoleService {

    /** 为用户分配角色 */
    void assignRoleToUser(Long userId, Long roleId);

    /** 移除用户的角色 */
    void removeRoleFromUser(Long userId, Long roleId);

    /** 获取用户的所有角色ID */
    List<Long> getUserRoleIds(Long userId);

    /** 登录时获取用户信息和权限 */
    LoginResponse enrichLoginResponse(Long userId);

    /** 更新用户信息（含角色列表） */
    UserInfoResponse getUserInfoWithRoles(Long userId);
}
