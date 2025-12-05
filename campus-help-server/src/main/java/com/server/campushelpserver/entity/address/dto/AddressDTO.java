package com.server.campushelpserver.entity.address.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 收货地址DTO
 */
@Data
@Schema(description = "收货地址DTO")
public class AddressDTO {
    
    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名不能超过50字")
    @Schema(description = "收货人姓名", required = true)
    private String receiverName;
    
    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "收货人电话", required = true)
    private String receiverPhone;
    
    @NotBlank(message = "省份不能为空")
    @Schema(description = "省份", required = true)
    private String province;
    
    @NotBlank(message = "城市不能为空")
    @Schema(description = "城市", required = true)
    private String city;
    
    @NotBlank(message = "区县不能为空")
    @Schema(description = "区县", required = true)
    private String district;
    
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址不能超过200字")
    @Schema(description = "详细地址", required = true)
    private String detailAddress;
    
    @Schema(description = "邮政编码（6位数字，选填）")
    @Pattern(regexp = "^$|^\\d{6}$", message = "邮政编码必须是6位数字或为空")
    private String postalCode;
    
    @Schema(description = "是否默认地址：0-否，1-是")
    private Integer isDefault;
}

