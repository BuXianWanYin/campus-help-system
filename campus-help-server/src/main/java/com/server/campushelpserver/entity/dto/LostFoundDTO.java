package com.server.campushelpserver.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 失物招领发布DTO
 */
@Data
@Schema(description = "失物招领发布DTO")
public class LostFoundDTO {
    
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 200, message = "物品名称不能超过200字")
    @Schema(description = "物品名称", required = true)
    private String title;
    
    @NotBlank(message = "物品分类不能为空")
    @Schema(description = "物品分类：证件类、电子产品、生活用品、书籍、其他", required = true)
    private String category;
    
    @NotBlank(message = "物品描述不能为空")
    @Size(min = 10, max = 500, message = "物品描述必须在10-500字之间")
    @Schema(description = "物品描述", required = true)
    private String description;
    
    @NotBlank(message = "类型不能为空")
    @Schema(description = "类型：LOST-失物，FOUND-招领", required = true)
    private String type;
    
    @NotNull(message = "丢失时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "丢失/拾取时间", required = true)
    private LocalDateTime lostTime;
    
    @NotBlank(message = "丢失地点不能为空")
    @Size(max = 200, message = "丢失地点不能超过200字")
    @Schema(description = "丢失/拾取地点", required = true)
    private String lostLocation;
    
    @Schema(description = "联系方式")
    private String contactInfo;
    
    @Schema(description = "物品图片URL列表（最多9张）")
    private List<String> images;
    
    @Schema(description = "悬赏金额（0-10000元）")
    private BigDecimal reward;
}

