package com.server.campushelpserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@TableName("goods")
@Schema(description = "商品实体")
public class Goods implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "商品ID")
    private Long id;
    
    @TableField("user_id")
    @Schema(description = "卖家ID")
    private Long userId;
    
    @TableField("title")
    @Schema(description = "商品标题")
    private String title;
    
    @TableField("category")
    @Schema(description = "商品分类：数码产品、图书教材、服装鞋包、生活用品、运动健身、乐器、文创用品、其他")
    private String category;
    
    @TableField("description")
    @Schema(description = "商品描述")
    private String description;
    
    @TableField("current_price")
    @Schema(description = "售价")
    private BigDecimal currentPrice;
    
    @TableField("`condition`")
    @Schema(description = "商品成色：全新、几乎全新、轻微使用痕迹、明显使用痕迹")
    private String condition;
    
    @TableField("images")
    @Schema(description = "商品图片（JSON数组，1-9张）")
    private String images;
    
    @TableField("trade_method")
    @Schema(description = "交易方式：FACE_TO_FACE-自提，MAIL-邮寄")
    private String tradeMethod;
    
    @TableField("trade_location")
    @Schema(description = "自提地点（自提时必填）")
    private String tradeLocation;
    
    @TableField("shipping_fee")
    @Schema(description = "邮寄费用（邮寄时可选）")
    private BigDecimal shippingFee;
    
    @TableField("stock")
    @Schema(description = "库存数量（最少1件）")
    private Integer stock;
    
    @TableField("status")
    @Schema(description = "状态：PENDING_REVIEW-待审核，ON_SALE-在售，SOLD_OUT-已售完，CLOSED-已关闭，REJECTED-已拒绝，ADMIN_OFFSHELF-已下架")
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
    
    @TableField("version")
    @Schema(description = "乐观锁版本号（用于防止超卖）")
    private Integer version;
    
    @TableLogic(value = "0", delval = "1")
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
    @Schema(description = "卖家用户信息（非数据库字段）")
    private User user;
}

