package com.profit.track.controller;

import com.profit.track.dto.*;
import com.profit.track.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证", description = "用户登录、注册、忘记密码")
public class AuthController {

    private final SysUserService sysUserService;

    /** 用户登录 */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT Token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);
        return Result.ok(response);
    }

    /** 手机号+验证码登录 */
    @PostMapping("/phone-login")
    @Operation(summary = "手机号验证码登录", description = "通过手机号+验证码登录，返回 JWT Token")
    public Result<LoginResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        LoginResponse response = sysUserService.phoneLogin(request);
        return Result.ok(response);
    }

    /** 用户注册 */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，返回 JWT Token")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = sysUserService.register(request);
        return Result.ok(response);
    }

    /** 获取当前用户信息 */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据 Token 中的 userId 获取用户详细信息")
    public Result<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        UserInfoResponse userInfo = sysUserService.getUserInfo(userId);
        return Result.ok(userInfo);
    }

    /** 修改密码 */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "使用旧密码验证后修改为新密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        sysUserService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }

    /** 重置密码（忘记密码） */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过手机号+验证码重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(request.getPhone(), request.getCode(), request.getNewPassword());
        return Result.ok();
    }

    /** 发送验证码 */
    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "验证手机号后发送6位数字验证码到控制台")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        sysUserService.sendVerificationCode(request.getUsername(), request.getPhone());
        return Result.ok();
    }

    /** 手机号登录专用：发送验证码 */
    @PostMapping("/send-login-code")
    @Operation(summary = "发送登录验证码", description = "通过手机号发送登录验证码到控制台")
    public Result<Void> sendLoginCode(@Valid @RequestBody SendLoginCodeRequest request) {
        sysUserService.sendLoginCode(request.getPhone());
        return Result.ok();
    }

    /** 注册专用：发送验证码（不校验用户是否存在） */
    @PostMapping("/send-register-code")
    @Operation(summary = "注册专用发送验证码", description = "仅校验手机号格式，发送验证码到控制台")
    public Result<Void> sendRegisterCode(@Valid @RequestBody SendRegisterCodeRequest request) {
        sysUserService.sendRegisterCode(request.getPhone());
        return Result.ok();
    }

    /** 重置密码专用：发送验证码（仅校验手机号是否存在） */
    @PostMapping("/send-reset-code")
    @Operation(summary = "重置密码专用发送验证码", description = "校验手机号是否存在，发送验证码到控制台")
    public Result<Void> sendResetCode(@Valid @RequestBody SendResetCodeRequest request) {
        sysUserService.sendResetCode(request.getPhone());
        return Result.ok();
    }

    /** 获取所有用户列表（仅管理员） */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有用户列表", description = "仅超级管理员可访问")
    public Result<List<UserInfoResponse>> listUsers(HttpServletRequest request) {
        List<UserInfoResponse> users = sysUserService.listUsers();
        return Result.ok(users);
    }

    /** 禁用/启用用户（仅管理员） */
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户状态", description = "仅超级管理员可访问")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        sysUserService.updateUserStatus(userId, status);
        return Result.ok();
    }

    /** 分配角色给用户（仅管理员） */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分配角色", description = "仅超级管理员可访问")
    public Result<Void> assignRole(
            @PathVariable Long userId,
            @RequestParam Long roleId) {
        sysUserService.assignRole(userId, roleId);
        return Result.ok();
    }
}
