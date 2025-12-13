package com.server.campushelpserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息实体类
 */
@Data
@TableName("system_message")
@Schema(description = "系统消息实体")
public class SystemMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long id;
    
    @TableField("user_id")
    @Schema(description = "接收用户ID", required = true)
    private Long userId;
    
    @TableField("type")
    @Schema(description = "消息类型：VERIFICATION_APPROVED-认证通过，VERIFICATION_REJECTED-认证拒绝，ORDER_STATUS-订单状态，TASK_STATUS-任务状态，ANNOUNCEMENT-系统公告", required = true)
    private String type;
    
    @TableField("title")
    @Schema(description = "消息标题", required = true)
    private String title;
    
    @TableField("content")
    @Schema(description = "消息内容", required = true)
    private String content;
    
    @TableField("is_read")
    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;
    
    @TableField("related_type")
    @Schema(description = "关联类型：VERIFICATION-实名认证，ORDER-订单，TASK-任务，ANNOUNCEMENT-公告")
    private String relatedType;
    
    @TableField("related_id")
    @Schema(description = "关联ID")
    private Long relatedId;
    
    @TableLogic(value = "0", delval = "1")
    @TableField("delete_flag")
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

