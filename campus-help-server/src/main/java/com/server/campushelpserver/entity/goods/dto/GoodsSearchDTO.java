package com.server.campushelpserver.entity.goods.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索DTO
 */
@Data
@Schema(description = "商品搜索DTO")
public class GoodsSearchDTO {
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "关键词（搜索标题或描述）")
    private String keyword;
    
    @Schema(description = "商品分类")
    private String category;
    
    @Schema(description = "商品成色")
    private String condition;
    
    @Schema(description = "状态筛选")
    private String status;
    
    @Schema(description = "最低价格")
    private BigDecimal minPrice;
    
    @Schema(description = "最高价格")
    private BigDecimal maxPrice;
    
    @Schema(description = "排序方式：latest-最新，price_asc-价格从低到高，price_desc-价格从高到低，view-浏览量", example = "latest")
    private String sortBy = "latest";
}

