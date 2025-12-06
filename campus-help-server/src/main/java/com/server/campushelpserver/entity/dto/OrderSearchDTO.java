package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单搜索DTO
 */
@Data
@Schema(description = "订单搜索DTO")
public class OrderSearchDTO {
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "角色：BUYER-我买到的，SELLER-我卖出的，ALL-全部")
    private String role = "ALL";
    
    @Schema(description = "订单状态筛选")
    private String status;
    
    @Schema(description = "交易方式筛选")
    private String tradeMethod;
    
    @Schema(description = "开始时间")
    private String startTime;
    
    @Schema(description = "结束时间")
    private String endTime;
    
    @Schema(description = "关键词（订单号、商品标题、对方昵称）")
    private String keyword;
}

