package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布DTO
 */
@Data
@Schema(description = "商品发布DTO")
public class GoodsDTO {
    
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 200, message = "商品标题不能超过200字")
    @Schema(description = "商品标题", required = true)
    private String title;
    
    @NotBlank(message = "商品分类不能为空")
    @Schema(description = "商品分类：数码产品、图书教材、服装鞋包、生活用品、运动健身、乐器、文创用品、其他", required = true)
    private String category;
    
    @NotBlank(message = "商品描述不能为空")
    @Size(min = 10, max = 500, message = "商品描述必须在10-500字之间")
    @Schema(description = "商品描述", required = true)
    private String description;
    
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    @Schema(description = "售价", required = true)
    private BigDecimal price;
    
    @NotBlank(message = "商品成色不能为空")
    @Schema(description = "商品成色：全新、几乎全新、轻微使用痕迹、明显使用痕迹", required = true)
    private String condition;
    
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量至少1件")
    @Schema(description = "库存数量（最少1件）", required = true)
    private Integer stock;
    
    @NotEmpty(message = "商品图片不能为空，至少上传1张")
    @Size(min = 1, max = 9, message = "商品图片数量必须在1-9张之间")
    @Schema(description = "商品图片URL列表（1-9张）", required = true)
    private List<String> images;
    
    @NotBlank(message = "交易方式不能为空")
    @Schema(description = "交易方式：FACE_TO_FACE-自提，MAIL-邮寄", required = true)
    private String tradeMethod;
    
    @Schema(description = "邮寄费用（邮寄时可选，默认0）")
    private BigDecimal shippingFee;
    
    @Schema(description = "自提地点（自提时必填，最多200字）")
    @Size(max = 200, message = "自提地点不能超过200字")
    private String tradeLocation;
}

