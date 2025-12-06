package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 实名认证审核请求DTO
 */
@Data
@Schema(description = "实名认证审核请求")
public class VerificationAuditRequestDTO {
    
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private Long userId;
    
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：1-通过，0-拒绝", required = true)
    private Integer auditResult;
    
    @Schema(description = "审核原因（拒绝时必填）")
    private String auditReason;
}

