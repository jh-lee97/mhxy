package com.profit.track.controller;

import com.profit.track.dto.*;
import com.profit.track.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
        try {
            LoginResponse response = sysUserService.login(request);
            return Result.ok(response);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 用户注册 */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，返回 JWT Token")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = sysUserService.register(request);
            return Result.ok(response);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 获取当前用户信息 */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据 Token 中的 userId 获取用户详细信息")
    public Result<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.fail(401, "未登录");
            }
            UserInfoResponse userInfo = sysUserService.getUserInfo(userId);
            return Result.ok(userInfo);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 修改密码 */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "使用旧密码验证后修改为新密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return Result.fail(401, "未登录");
            }
            sysUserService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 重置密码（忘记密码） */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过手机号+验证码重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            sysUserService.resetPassword(request.getPhone(), request.getCode(), request.getNewPassword());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 发送验证码 */
    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "验证手机号后发送6位数字验证码到控制台")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            sysUserService.sendVerificationCode(request.getUsername(), request.getPhone());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 注册专用：发送验证码（不校验用户是否存在） */
    @PostMapping("/send-register-code")
    @Operation(summary = "注册专用发送验证码", description = "仅校验手机号格式，发送验证码到控制台")
    public Result<Void> sendRegisterCode(@Valid @RequestBody SendRegisterCodeRequest request) {
        try {
            sysUserService.sendRegisterCode(request.getPhone());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 获取所有用户列表（仅管理员） */
    @GetMapping("/users")
    @Operation(summary = "获取所有用户列表", description = "仅超级管理员可访问")
    public Result<List<UserInfoResponse>> listUsers(HttpServletRequest request) {
        try {
            Integer roleLevel = (Integer) request.getAttribute("roleLevel");
            List<UserInfoResponse> users = sysUserService.listUsers(roleLevel);
            return Result.ok(users);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 禁用/启用用户（仅管理员） */
    @PutMapping("/users/{userId}/status")
    @Operation(summary = "更新用户状态", description = "仅超级管理员可访问")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status,
            HttpServletRequest request) {
        try {
            Integer roleLevel = (Integer) request.getAttribute("roleLevel");
            sysUserService.updateUserStatus(userId, status, roleLevel);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 分配角色给用户（仅管理员） */
    @PutMapping("/users/{userId}/role")
    @Operation(summary = "分配角色", description = "仅超级管理员可访问")
    public Result<Void> assignRole(
            @PathVariable Long userId,
            @RequestParam Long roleId,
            HttpServletRequest request) {
        try {
            Integer roleLevel = (Integer) request.getAttribute("roleLevel");
            sysUserService.assignRole(userId, roleId, roleLevel);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
