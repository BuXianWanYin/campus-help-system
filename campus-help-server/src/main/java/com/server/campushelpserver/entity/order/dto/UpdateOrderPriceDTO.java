package com.server.campushelpserver.entity.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 修改订单价格DTO
 */
@Data
@Schema(description = "修改订单价格DTO")
public class UpdateOrderPriceDTO {
    
    @NotNull(message = "新价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Schema(description = "新价格", required = true)
    private BigDecimal newPrice;
}

