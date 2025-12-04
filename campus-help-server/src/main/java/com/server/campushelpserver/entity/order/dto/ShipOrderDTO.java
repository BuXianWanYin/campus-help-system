package com.server.campushelpserver.entity.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发货DTO
 */
@Data
@Schema(description = "发货DTO")
public class ShipOrderDTO {
    
    @NotBlank(message = "快递单号不能为空")
    @Schema(description = "快递单号", required = true)
    private String trackingNumber;
    
    @Schema(description = "物流公司")
    private String logisticsCompany;
}

