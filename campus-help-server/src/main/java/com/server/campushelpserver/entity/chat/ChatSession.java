package com.server.campushelpserver.entity.chat;

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
 * 私信会话实体类
 */
@Data
@TableName("chat_session")
@Schema(description = "私信会话实体")
public class ChatSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "会话ID")
    private Long id;
    
    @TableField("user1_id")
    @Schema(description = "用户1 ID")
    private Long user1Id;
    
    @TableField("user2_id")
    @Schema(description = "用户2 ID")
    private Long user2Id;
    
    @TableField("related_type")
    @Schema(description = "关联类型：LOST_FOUND-失物招领，GOODS-商品，TASK-跑腿任务")
    private String relatedType;
    
    @TableField("related_id")
    @Schema(description = "关联ID（失物ID、商品ID、任务ID等）")
    private Long relatedId;
    
    @TableField("last_message_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;
    
    @TableField("last_message_content")
    @Schema(description = "最后消息内容")
    private String lastMessageContent;
    
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

