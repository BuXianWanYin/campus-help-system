package com.server.campushelpserver.entity.lostfound.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 失物招领搜索DTO
 */
@Data
@Schema(description = "失物招领搜索DTO")
public class LostFoundSearchDTO {
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "关键词（搜索标题或描述）")
    private String keyword;
    
    @Schema(description = "物品分类")
    private String category;
    
    @Schema(description = "类型：LOST-失物，FOUND-招领")
    private String type;
    
    @Schema(description = "状态筛选")
    private String status;
    
    @Schema(description = "地点筛选")
    private String location;
    
    @Schema(description = "是否有悬赏")
    private Boolean hasReward;
    
    @Schema(description = "排序方式：latest-最新，view-浏览最多，reward-悬赏最高", example = "latest")
    private String sortBy = "latest";
    
    @Schema(description = "审核状态筛选：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝，ALL-全部")
    private String auditStatus;
}

