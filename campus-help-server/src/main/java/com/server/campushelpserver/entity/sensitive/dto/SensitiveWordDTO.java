package com.server.campushelpserver.entity.sensitive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 敏感词DTO
 */
@Data
@Schema(description = "敏感词DTO")
public class SensitiveWordDTO {
    
    @Schema(description = "敏感词ID（更新时必填）")
    private Long id;
    
    @NotBlank(message = "敏感词不能为空")
    @Schema(description = "敏感词", required = true)
    private String word;
    
    @NotBlank(message = "严重程度不能为空")
    @Schema(description = "严重程度：NORMAL-普通，SEVERE-严重", required = true)
    private String level;
}

