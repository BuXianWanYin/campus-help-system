package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 发送消息DTO
 */
@Data
@Schema(description = "发送消息DTO")
public class SendMessageDTO {
    
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", required = true)
    private Long sessionId;
    
    @NotNull(message = "消息类型不能为空")
    @Schema(description = "消息类型：TEXT-文字，IMAGE-图片，GOODS_CARD-商品卡片，ORDER_CARD-订单卡片", required = true)
    private String messageType;
    
    @Schema(description = "消息内容（文字消息必填）")
    private String content;
    
    @Schema(description = "图片URL列表（图片消息必填）")
    private List<String> images;
}

