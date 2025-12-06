package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 创建订单DTO
 */
@Data
@Schema(description = "创建订单DTO")
public class OrderDTO {
    
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", required = true)
    private Long goodsId;
    
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少1件")
    @Schema(description = "购买数量", required = true)
    private Integer quantity;
    
    @NotNull(message = "交易方式不能为空")
    @Schema(description = "交易方式：FACE_TO_FACE-自提，MAIL-邮寄", required = true)
    private String tradeMethod;
    
    @Schema(description = "收货地址ID（邮寄时必填）")
    private Long addressId;
}

