package com.server.campushelpserver.entity.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 问题审核DTO
 */
@Data
@Schema(description = "问题审核DTO")
public class QuestionAuditDTO {
    
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：true-通过，false-拒绝", required = true)
    private Boolean approved;
    
    @Schema(description = "拒绝原因（拒绝时必填）")
    private String reason;
}

