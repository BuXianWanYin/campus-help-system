package com.server.campushelpserver.entity.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建会话DTO
 */
@Data
@Schema(description = "创建会话DTO")
public class CreateSessionDTO {
    
    @NotNull(message = "对方用户ID不能为空")
    @Schema(description = "对方用户ID", required = true)
    private Long targetUserId;
    
    @Schema(description = "关联类型：LOST_FOUND-失物招领，GOODS-商品，TASK-跑腿任务")
    private String relatedType;
    
    @Schema(description = "关联ID（失物ID、商品ID、任务ID等）")
    private Long relatedId;
}

