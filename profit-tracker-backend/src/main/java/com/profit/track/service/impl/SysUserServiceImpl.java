package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.profit.track.dto.*;
import com.profit.track.entity.SysRole;
import com.profit.track.entity.SysUser;
import com.profit.track.mapper.SysRoleMapper;
import com.profit.track.mapper.SysUserMapper;
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
    private final VerificationCodeUtil verificationCodeUtil;

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

        // 获取角色信息
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String roleName = role != null ? role.getRoleName() : "";
        Integer roleLevel = role != null ? role.getRoleLevel() : 1;

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleName);

        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getRoleId(), roleLevel, roleName);
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
        if (!verificationCodeUtil.verifyCode(request.getPhone(), request.getCode())) {
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
        user.setRoleId(request.getRoleId() != null ? request.getRoleId() : 1L);
        user.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        save(user);

        // 获取角色信息
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String roleName = role != null ? role.getRoleName() : "";
        Integer roleLevel = role != null ? role.getRoleLevel() : 1;

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleName);

        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getRoleId(), roleLevel, roleName);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        SysRole role = sysRoleMapper.selectById(user.getRoleId());

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setRoleId(user.getRoleId());
        response.setRoleLevel(role != null ? role.getRoleLevel() : 1);
        response.setRoleName(role != null ? role.getRoleName() : "");
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());

        return response;
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
        if (!verificationCodeUtil.verifyCode(phone, code)) {
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
        verificationCodeUtil.sendCode(phone);
    }

    @Override
    public void sendRegisterCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        verificationCodeUtil.sendCode(phone);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public List<UserInfoResponse> listUsers(Integer roleLevel) {
        // 只有管理员可以查看用户列表
        if (!isSuperAdmin(roleLevel)) {
            throw new RuntimeException("无权访问用户列表");
        }
        List<SysUser> users = list();
        return users.stream().map(this::toUserInfoResponse).collect(Collectors.toList());
    }

    @Override
    public void updateUserStatus(Long userId, Integer status, Integer roleLevel) {
        // 只有管理员可以修改用户状态
        if (!isSuperAdmin(roleLevel)) {
            throw new RuntimeException("无权管理用户状态");
        }
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void assignRole(Long userId, Long roleId, Integer roleLevel) {
        // 只有管理员可以分配角色
        if (!isSuperAdmin(roleLevel)) {
            throw new RuntimeException("无权分配角色");
        }
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setRoleId(roleId);
        // 同时更新角色等级
        SysRole role = sysRoleMapper.selectById(roleId);
        user.setRoleLevel(role != null ? role.getRoleLevel() : 1);
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    private boolean isSuperAdmin(Integer roleLevel) {
        return roleLevel != null && roleLevel >= 100;
    }

    private UserInfoResponse toUserInfoResponse(SysUser user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setRoleId(user.getRoleId());
        response.setRoleLevel(user.getRoleLevel());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
