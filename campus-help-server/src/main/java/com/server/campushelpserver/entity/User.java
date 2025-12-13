package com.server.campushelpserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    
    @TableField("phone")
    @Schema(description = "手机号")
    private String phone;
    
    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;
    
    @TableField("id_card")
    @Schema(description = "身份证号")
    private String idCard;
    
    @TableField("student_id")
    @Schema(description = "学号")
    private String studentId;
    
    @TableField("user_type")
    @Schema(description = "用户类型：学生、教师")
    private String userType;
    
    @TableField("is_verified")
    @Schema(description = "是否实名认证：0-未认证，1-已认证")
    private Integer isVerified;
    
    @TableField("verification_status")
    @Schema(description = "认证状态：NOT_VERIFIED、PENDING、VERIFIED、REJECTED")
    private String verificationStatus;
    
    @TableField("verification_proof")
    @Schema(description = "认证证明文件（JSON数组）")
    private String verificationProof;
    
    @TableField("verification_submit_time")
    @Schema(description = "认证提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verificationSubmitTime;
    
    @TableField("verification_audit_time")
    @Schema(description = "认证审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verificationAuditTime;
    
    @TableField("verification_audit_reason")
    @Schema(description = "认证拒绝原因")
    private String verificationAuditReason;
    
    @TableField("verification_audit_admin_id")
    @Schema(description = "审核管理员ID")
    private Long verificationAuditAdminId;
    
    @TableField("can_accept_task")
    @Schema(description = "是否可以接单跑腿：0-不可接单，1-可接单")
    private Integer canAcceptTask;
    
    @TableField("role")
    @Schema(description = "角色：USER-普通用户，ADMIN-管理员")
    private String role;
    
    @TableField("status")
    @Schema(description = "账号状态：1-正常，2-已封禁")
    private Integer status;
    
    @TableField("ban_type")
    @Schema(description = "封禁类型：TEMPORARY-临时，PERMANENT-永久")
    private String banType;
    
    @TableField("ban_reason")
    @Schema(description = "封禁原因")
    private String banReason;
    
    @TableField("ban_days")
    @Schema(description = "封禁天数")
    private Integer banDays;
    
    @TableField("ban_time")
    @Schema(description = "封禁时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime banTime;
    
    @TableField("unban_time")
    @Schema(description = "解封时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unbanTime;
    
    @TableField("ban_admin_id")
    @Schema(description = "封禁操作管理员ID")
    private Long banAdminId;
    
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
    
    @TableLogic(value = "0", delval = "1")
    @TableField("delete_flag")
    @Schema(description = "逻辑删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    /**
     * 注册验证组
     */
    public interface RegisterGroup {
    }
}

