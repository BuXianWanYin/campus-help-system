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
 * 认领记录实体类
 */
@Data
@TableName("claim_record")
@Schema(description = "认领记录实体")
public class ClaimRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "认领记录ID")
    private Long id;
    
    @TableField("lost_found_id")
    @Schema(description = "失物ID")
    private Long lostFoundId;
    
    @TableField("claimer_id")
    @Schema(description = "认领者ID")
    private Long claimerId;
    
    @TableField("description")
    @Schema(description = "物品特征描述")
    private String description;
    
    @TableField("lost_time")
    @Schema(description = "丢失时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lostTime;
    
    @TableField("other_info")
    @Schema(description = "其他信息")
    private String otherInfo;
    
    @TableField("proof_images")
    @Schema(description = "证明文件（JSON数组）")
    private String proofImages;
    
    @TableField("status")
    @Schema(description = "状态：PENDING-待确认，CONFIRMED-已确认，REJECTED-已拒绝")
    private String status;
    
    @TableField("confirm_time")
    @Schema(description = "确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;
    
    @TableField("reject_reason")
    @Schema(description = "拒绝原因")
    private String rejectReason;
    
    @TableField("reject_time")
    @Schema(description = "拒绝时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectTime;
    
    @TableField("delete_flag")
    @Schema(description = "逻辑删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    @TableField("create_time")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "认领者用户信息（非数据库字段）")
    private User user;
}

