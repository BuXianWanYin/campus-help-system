package com.server.campushelpserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息实体类
 */
@Data
@TableName("chat_message")
@Schema(description = "私信消息实体")
public class ChatMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long id;
    
    @TableField("session_id")
    @Schema(description = "会话ID")
    private Long sessionId;
    
    @TableField("sender_id")
    @Schema(description = "发送者ID")
    private Long senderId;
    
    @TableField("receiver_id")
    @Schema(description = "接收者ID")
    private Long receiverId;
    
    @TableField("message_type")
    @Schema(description = "消息类型：TEXT-文字，IMAGE-图片，GOODS_CARD-商品卡片，ORDER_CARD-订单卡片")
    private String messageType;
    
    @TableField("content")
    @Schema(description = "消息内容")
    private String content;
    
    @TableField("images")
    @Schema(description = "图片URL列表（JSON数组）")
    private String images;
    
    @TableField("is_read")
    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;
    
    @TableField("delete_flag")
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

