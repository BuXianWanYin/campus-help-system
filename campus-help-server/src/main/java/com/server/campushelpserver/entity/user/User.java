package com.server.campushelpserver.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
@Schema(description = "用户实体")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;
    
    @TableField("password")
    @Schema(description = "密码（BCrypt加密）", required = true, hidden = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "密码不能为空", groups = {RegisterGroup.class})
    @Size(min = 8, message = "密码至少8位，且包含字母和数字", groups = {RegisterGroup.class})
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "密码至少8位，且包含字母和数字", groups = {RegisterGroup.class})
    private String password;
    
    @TableField("nickname")
    @Schema(description = "昵称")
    private String nickname;
    
    @TableField("avatar")
    @Schema(description = "头像URL")
    private String avatar;
    
    @TableField("email")
    @Schema(description = "邮箱（唯一）", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @TableField("gender")
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;
    
    @TableField("grade")
    @Schema(description = "年级")
    private String grade;
    
    @TableField("major")
    @Schema(description = "专业")
    private String major;
    
    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;
    
    @TableField("id_card")
    @Schema(description = "身份证号")
    private String idCard;
    
    @TableField("student_id")
    @Schema(description = "学号")
    private String studentId;
    
    @TableField("is_verified")
    @Schema(description = "是否实名认证：0-未认证，1-已认证")
    private Integer isVerified;
    
    @TableField("can_accept_task")
    @Schema(description = "是否可以接单跑腿：0-不可接单，1-可接单")
    private Integer canAcceptTask;
    
    @TableField("credit_score")
    @Schema(description = "信用积分")
    private Integer creditScore;
    
    @TableField("credit_level")
    @Schema(description = "信用等级")
    private String creditLevel;
    
    @TableField("runner_level")
    @Schema(description = "跑腿员等级：新手、普通、优秀、金牌")
    private String runnerLevel;
    
    @TableField("role")
    @Schema(description = "角色：USER-普通用户，ADMIN-管理员")
    private String role;
    
    @TableField("status")
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
    
    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    
    @TableField("create_time")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @TableField("delete_flag")
    @Schema(description = "逻辑删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    /**
     * 注册验证组
     */
    public interface RegisterGroup {
    }
}

