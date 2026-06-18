package com.profit.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.profit.track.dto.LoginRequest;
import com.profit.track.dto.LoginResponse;
import com.profit.track.dto.RegisterRequest;
import com.profit.track.dto.UserInfoResponse;
import com.profit.track.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    /** 用户登录 */
    LoginResponse login(LoginRequest request);

    /** 用户注册 */
    LoginResponse register(RegisterRequest request);

    /** 根据用户ID获取用户信息 */
    UserInfoResponse getUserInfo(Long userId);

    /** 修改密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /** 重置密码（忘记密码场景） */
    void resetPassword(String phone, String code, String newPassword);

    /** 发送验证码 */
    void sendVerificationCode(String username, String phone);

    /** 注册专用：发送验证码（不校验用户是否存在） */
    void sendRegisterCode(String phone);

    /** 根据用户名查找用户 */
    SysUser getByUsername(String username);

    /** 获取所有用户列表（管理员） */
    List<UserInfoResponse> listUsers(Integer roleLevel);

    /** 禁用/启用用户（管理员） */
    void updateUserStatus(Long userId, Integer status, Integer roleLevel);

    /** 分配角色给用户（管理员） */
    void assignRole(Long userId, Long roleId, Integer roleLevel);
}
