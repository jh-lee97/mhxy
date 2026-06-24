package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.profit.track.dto.*;
import com.profit.track.entity.SysRole;
import com.profit.track.entity.SysUser;
import com.profit.track.entity.SysUserRole;
import com.profit.track.mapper.SysRoleMapper;
import com.profit.track.mapper.SysUserMapper;
import com.profit.track.mapper.SysUserRoleMapper;
import com.profit.track.service.SysPermissionService;
import com.profit.track.service.SysUserRoleService;
import com.profit.track.service.SysUserService;
import com.profit.track.util.JwtUtil;
import com.profit.track.util.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final VerificationCodeUtil verificationCodeUtil;
    private final SysPermissionService sysPermissionService;
    private final SysUserRoleService sysUserRoleService;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        SysUser user = getByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 通过 RBAC 服务获取完整的用户信息和权限
        return sysUserRoleService.enrichLoginResponse(user.getId());
    }

    @Override
    public LoginResponse phoneLogin(PhoneLoginRequest request) {
        // 查找用户
        SysUser user = getByPhone(request.getPhone());
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }

        // 验证状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证验证码
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new RuntimeException("请输入验证码");
        }
        if (!verificationCodeUtil.verifyCode("login", request.getPhone(), request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 通过 RBAC 服务获取完整的用户信息和权限
        return sysUserRoleService.enrichLoginResponse(user.getId());
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 验证手机号格式
        if (request.getPhone() == null || !request.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 验证验证码
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new RuntimeException("请输入验证码");
        }
        if (!verificationCodeUtil.verifyCode("register", request.getPhone(), request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查用户名是否存在
        SysUser existing = getByUsername(request.getUsername());
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已被注册
        LambdaQueryWrapper<SysUser> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(SysUser::getPhone, request.getPhone());
        if (count(phoneWrapper) > 0) {
            throw new RuntimeException("该手机号已被注册");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        save(user);

        // 默认分配 USER 角色
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleCode, "USER");
        SysRole userRole = sysRoleMapper.selectOne(roleWrapper);
        if (userRole != null) {
            sysUserRoleService.assignRoleToUser(user.getId(), userRole.getId());
        }

        // 通过 RBAC 服务获取完整的用户信息和权限
        return sysUserRoleService.enrichLoginResponse(user.getId());
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        return sysUserRoleService.getUserInfoWithRoles(userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void resetPassword(String phone, String code, String newPassword) {
        // 根据手机号查找用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        SysUser user = getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }

        // 验证验证码
        if (!verificationCodeUtil.verifyCode("reset", phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void sendVerificationCode(String username, String phone) {
        // 根据用户名查找用户
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 比对手机号是否匹配
        if (!phone.equals(user.getPhone())) {
            throw new RuntimeException("手机号与用户不匹配");
        }

        // 发送验证码（存入 Redis + 打印到控制台）
        verificationCodeUtil.sendCode("login", phone);
    }

    @Override
    public void sendLoginCode(String phone) {
        // 根据手机号查找用户
        SysUser user = getByPhone(phone);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }
        // 检查状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("该账号已被禁用");
        }
        verificationCodeUtil.sendCode("login", phone);
    }

    @Override
    public void sendRegisterCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        // 校验手机号是否已被注册，已注册的不能再发送注册验证码
        SysUser existing = getByPhone(phone);
        if (existing != null) {
            throw new RuntimeException("该手机号已被注册，请直接登录");
        }
        verificationCodeUtil.sendCode("register", phone);
    }

    @Override
    public void sendResetCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        // 校验手机号是否存在
        SysUser user = getByPhone(phone);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }
        verificationCodeUtil.sendCode("reset", phone);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public SysUser getByPhone(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return getOne(wrapper);
    }

    @Override
    public List<UserInfoResponse> listUsers() {
        List<SysUser> users = list();
        return users.stream().map(this::toUserInfoResponse).collect(Collectors.toList());
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 分配角色（支持多角色）
        sysUserRoleService.assignRoleToUser(userId, roleId);
    }

    @Override
    public void deleteUser(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否拥有 ADMIN 角色
        List<Long> roleIds = sysUserRoleService.getUserRoleIds(userId);
        for (Long roleId : roleIds) {
            SysRole role = sysRoleMapper.selectById(roleId);
            if (role != null && "ADMIN".equals(role.getRoleCode())) {
                throw new RuntimeException("不允许删除超级管理员用户");
            }
        }

        // 删除用户
        removeById(userId);
    }

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
        return response;
    }
}
