package com.server.campushelpserver.entity.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 采纳答案DTO
 */
@Data
@Schema(description = "采纳答案DTO")
public class AcceptAnswerDTO {
    
    @NotNull(message = "回答ID不能为空")
    @Schema(description = "回答ID", required = true)
    private Long answerId;
}

