package com.server.campushelpserver.entity.study;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.server.campushelpserver.entity.user.User;

/**
 * 学习回答实体类
 */
@Data
@TableName("study_answer")
@Schema(description = "学习回答实体")
public class StudyAnswer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "回答ID")
    private Long id;
    
    @TableField("question_id")
    @Schema(description = "问题ID")
    private Long questionId;
    
    @TableField("user_id")
    @Schema(description = "回答者ID")
    private Long userId;
    
    @TableField("content")
    @Schema(description = "回答内容")
    private String content;
    
    @TableField("images")
    @Schema(description = "回答图片（JSON数组，最多3张）")
    private String images;
    
    @TableField("is_accepted")
    @Schema(description = "是否被采纳：0-未采纳，1-已采纳")
    private Integer isAccepted;
    
    @TableField("accept_time")
    @Schema(description = "采纳时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;
    
    @TableField("audit_status")
    @Schema(description = "审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝")
    private String auditStatus;
    
    @TableField("audit_reason")
    @Schema(description = "审核拒绝原因")
    private String auditReason;
    
    @TableField("audit_time")
    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @TableField("audit_admin_id")
    @Schema(description = "审核管理员ID")
    private Long auditAdminId;
    
    @TableField("audit_trigger_reason")
    @Schema(description = "触发人工审核原因（敏感词、发布频繁等）")
    private String auditTriggerReason;
    
    @TableField("like_count")
    @Schema(description = "点赞数量（可选功能）")
    private Integer likeCount;
    
    @TableField("delete_flag")
    @Schema(description = "逻辑删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    @TableField("create_time")
    @Schema(description = "回答时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "回答者用户信息（非数据库字段）")
    private User user;
}

