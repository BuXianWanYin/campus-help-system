package com.server.campushelpserver.entity.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 每日趋势DTO
 */
@Data
@Schema(description = "每日互助趋势")
public class DailyTrendDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "日期（格式：yyyy-MM-dd）")
    private String date;
    
    @Schema(description = "失物招领数量")
    private Long lostFoundCount;
    
    @Schema(description = "闲置交易数量")
    private Long goodsCount;
    
    @Schema(description = "学习互助数量")
    private Long studyQuestionCount;
    
    @Schema(description = "总计")
    private Long total;
}

