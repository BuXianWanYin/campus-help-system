package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 实名认证请求DTO
 */
@Data
@Schema(description = "实名认证请求")
public class VerificationRequestDTO {
    
    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 10, message = "真实姓名长度必须在2-10个中文字符之间")
    @Schema(description = "真实姓名", required = true)
    private String realName;
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", 
             message = "身份证号格式不正确，请输入18位身份证号")
    @Schema(description = "身份证号（18位）", required = true)
    private String idCard;
    
    @NotBlank(message = "学号/工号不能为空")
    @Size(min = 6, max = 20, message = "学号/工号长度必须在6-20个字符之间")
    @Schema(description = "学号/工号", required = true)
    private String studentId;
    
    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "^(学生|教师)$", message = "用户类型必须是'学生'或'教师'")
    @Schema(description = "用户类型：学生、教师", required = true, example = "学生")
    private String userType;
    
    @NotEmpty(message = "证明文件不能为空，至少上传1张")
    @Size(min = 1, max = 3, message = "证明文件数量必须在1-3张之间")
    @Schema(description = "证明文件URL列表（至少1张，最多3张）", required = true)
    private List<String> proofImages;
}

