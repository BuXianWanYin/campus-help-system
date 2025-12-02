package com.server.campushelpserver.entity.user;

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
 * 验证码实体类
 */
@Data
@TableName("verification_code")
@Schema(description = "验证码实体")
public class VerificationCode implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "验证码ID")
    private Long id;
    
    @TableField("type")
    @Schema(description = "类型：REGISTER-注册，LOGIN-登录，RESET_PASSWORD-重置密码", required = true)
    private String type;
    
    @TableField("target")
    @Schema(description = "目标（邮箱）", required = true)
    private String target;
    
    @TableField("code")
    @Schema(description = "验证码", required = true)
    private String code;
    
    @TableField("expire_time")
    @Schema(description = "过期时间", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    @TableField("is_used")
    @Schema(description = "是否已使用：0-未使用，1-已使用")
    private Integer isUsed;
    
    @TableField("create_time")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

