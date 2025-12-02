package com.server.campushelpserver.entity.lostfound;

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

import com.server.campushelpserver.entity.user.User;

/**
 * 失物招领实体类
 */
@Data
@TableName("lost_found")
@Schema(description = "失物招领实体")
public class LostFound implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "失物ID")
    private Long id;
    
    @TableField("user_id")
    @Schema(description = "发布者ID")
    private Long userId;
    
    @TableField("title")
    @Schema(description = "物品名称")
    private String title;
    
    @TableField("category")
    @Schema(description = "物品分类：证件类、电子产品、生活用品、书籍、其他")
    private String category;
    
    @TableField("description")
    @Schema(description = "物品描述")
    private String description;
    
    @TableField("type")
    @Schema(description = "类型：LOST-失物，FOUND-招领")
    private String type;
    
    @TableField("lost_time")
    @Schema(description = "丢失/拾取时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lostTime;
    
    @TableField("lost_location")
    @Schema(description = "丢失/拾取地点")
    private String lostLocation;
    
    @TableField("contact_info")
    @Schema(description = "联系方式")
    private String contactInfo;
    
    @TableField("images")
    @Schema(description = "物品图片（JSON数组）")
    private String images;
    
    @TableField("reward")
    @Schema(description = "悬赏金额")
    private BigDecimal reward;
    
    @TableField("status")
    @Schema(description = "状态：PENDING_REVIEW-待审核，PENDING_CLAIM-待认领，CLAIMING-认领中，CLAIMED-已认领，CLOSED-已关闭，REJECTED-已拒绝，ADMIN_OFFSHELF-已下架")
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
    
    @TableField("view_count")
    @Schema(description = "浏览次数")
    private Integer viewCount;
    
    @TableField("favorite_count")
    @Schema(description = "收藏次数")
    private Integer favoriteCount;
    
    @TableField("comment_count")
    @Schema(description = "评论次数")
    private Integer commentCount;
    
    @TableField("version")
    @Schema(description = "乐观锁版本号")
    private Integer version;
    
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

