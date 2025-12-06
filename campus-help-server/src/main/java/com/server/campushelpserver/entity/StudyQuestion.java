package com.server.campushelpserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学习问题实体类
 */
@Data
@TableName("study_question")
@Schema(description = "学习问题实体")
public class StudyQuestion implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "问题ID")
    private Long id;
    
    @TableField("user_id")
    @Schema(description = "发布者ID")
    private Long userId;
    
    @TableField("title")
    @Schema(description = "问题标题")
    private String title;
    
    @TableField("category")
    @Schema(description = "问题分类：MATH-数学，PHYSICS-物理，CHEMISTRY-化学，BIOLOGY-生物，COMPUTER-计算机，ENGLISH-英语，LITERATURE-文学，HISTORY-历史，PHILOSOPHY-哲学，ECONOMICS-经济，MANAGEMENT-管理，LAW-法律，EDUCATION-教育，ART-艺术，ENGINEERING-工程，MEDICINE-医学，AGRICULTURE-农学，OTHER-其他")
    private String category;
    
    @TableField("description")
    @Schema(description = "问题描述")
    private String description;
    
    @TableField("images")
    @Schema(description = "问题图片（JSON数组，最多3张）")
    private String images;
    
    @TableField("reward")
    @Schema(description = "悬赏金额（可选，0表示无酬劳）")
    private BigDecimal reward;
    
    @TableField("status")
    @Schema(description = "状态：PENDING_REVIEW-待审核，PENDING_ANSWER-待解答，ANSWERED-已回答，SOLVED-已解决，CANCELLED-已取消，REJECTED-已拒绝，ADMIN_OFFSHELF-已下架")
    private String status;
    
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
    
    @TableField("offshelf_type")
    @Schema(description = "下架类型：USER-用户自行下架，ADMIN-管理员下架")
    private String offshelfType;
    
    @TableField("offshelf_reason")
    @Schema(description = "下架原因")
    private String offshelfReason;
    
    @TableField("offshelf_time")
    @Schema(description = "下架时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime offshelfTime;
    
    @TableField("offshelf_admin_id")
    @Schema(description = "下架管理员ID")
    private Long offshelfAdminId;
    
    @TableField("accepted_answer_id")
    @Schema(description = "已采纳的回答ID")
    private Long acceptedAnswerId;
    
    @TableField("accept_time")
    @Schema(description = "采纳时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;
    
    @TableField("view_count")
    @Schema(description = "浏览次数")
    private Integer viewCount;
    
    @TableField("answer_count")
    @Schema(description = "回答数量")
    private Integer answerCount;
    
    @TableField("last_answer_time")
    @Schema(description = "最后回答时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAnswerTime;
    
    @TableField("solve_time")
    @Schema(description = "解决时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime solveTime;
    
    @TableField("delete_flag")
    @Schema(description = "逻辑删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    @TableField("create_time")
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "发布者用户信息（非数据库字段）")
    private User user;
}

