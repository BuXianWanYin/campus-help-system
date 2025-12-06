package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 修改密码请求DTO
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequestDTO {
    
    @NotBlank(message = "当前密码不能为空")
    @Schema(description = "当前密码", required = true)
    private String currentPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", 
             message = "密码至少8位，且包含字母和数字")
    @Schema(description = "新密码（至少8位，且包含字母和数字）", required = true)
    private String newPassword;
}

