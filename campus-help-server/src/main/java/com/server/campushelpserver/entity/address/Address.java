package com.server.campushelpserver.entity.address;

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
 * 收货地址实体类
 */
@Data
@TableName("address")
@Schema(description = "收货地址实体")
public class Address implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "地址ID")
    private Long id;
    
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    
    @TableField("receiver_name")
    @Schema(description = "收货人姓名")
    private String receiverName;
    
    @TableField("receiver_phone")
    @Schema(description = "收货人电话")
    private String receiverPhone;
    
    @TableField("province")
    @Schema(description = "省份")
    private String province;
    
    @TableField("city")
    @Schema(description = "城市")
    private String city;
    
    @TableField("district")
    @Schema(description = "区县")
    private String district;
    
    @TableField("detail_address")
    @Schema(description = "详细地址")
    private String detailAddress;
    
    @TableField("full_address")
    @Schema(description = "完整地址（省+市+区+详细地址）")
    private String fullAddress;
    
    @TableField("postal_code")
    @Schema(description = "邮政编码")
    private String postalCode;
    
    @TableField("is_default")
    @Schema(description = "是否默认地址：0-否，1-是")
    private Integer isDefault;
    
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
}

