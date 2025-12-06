package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 回答DTO
 */
@Data
@Schema(description = "回答DTO")
public class AnswerDTO {
    
    @NotBlank(message = "回答内容不能为空")
    @Size(min = 1, max = 2000, message = "回答内容长度必须在1-2000字符之间")
    @Schema(description = "回答内容", required = true)
    private String content;
    
    @Schema(description = "回答图片URL列表（最多3张）")
    private List<String> images;
}

