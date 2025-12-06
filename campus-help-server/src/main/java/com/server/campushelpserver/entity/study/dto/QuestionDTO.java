package com.server.campushelpserver.entity.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 问题发布DTO
 */
@Data
@Schema(description = "问题发布DTO")
public class QuestionDTO {
    
    @NotBlank(message = "问题分类不能为空")
    @Schema(description = "问题分类：MATH-数学，PHYSICS-物理，CHEMISTRY-化学，BIOLOGY-生物，COMPUTER-计算机，ENGLISH-英语，LITERATURE-文学，HISTORY-历史，PHILOSOPHY-哲学，ECONOMICS-经济，MANAGEMENT-管理，LAW-法律，EDUCATION-教育，ART-艺术，ENGINEERING-工程，MEDICINE-医学，AGRICULTURE-农学，OTHER-其他", required = true)
    private String category;
    
    @NotBlank(message = "问题标题不能为空")
    @Size(min = 1, max = 200, message = "问题标题长度必须在1-200字符之间")
    @Schema(description = "问题标题", required = true)
    private String title;
    
    @NotBlank(message = "问题描述不能为空")
    @Size(min = 10, max = 2000, message = "问题描述长度必须在10-2000字符之间")
    @Schema(description = "问题描述", required = true)
    private String description;
    
    @Schema(description = "问题图片URL列表（最多3张）")
    private List<String> images;
    
    @Schema(description = "悬赏金额（可选，0.01-9999.99元，0表示无酬劳）")
    private BigDecimal reward;
}

