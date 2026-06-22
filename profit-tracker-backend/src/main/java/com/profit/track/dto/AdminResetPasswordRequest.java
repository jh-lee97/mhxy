package com.profit.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "管理员重置密码请求")
public class AdminResetPasswordRequest {

    @Schema(description = "新密码", example = "newpass123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50之间")
    private String newPassword;
}
