package com.server.campushelpserver.entity.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题搜索DTO
 */
@Data
@Schema(description = "问题搜索DTO")
public class QuestionSearchDTO {
    
    @Schema(description = "问题分类")
    private String category;
    
    @Schema(description = "问题状态：PENDING_ANSWER-待解答，ANSWERED-已回答，SOLVED-已解决")
    private String status;
    
    @Schema(description = "审核状态：ALL-全部，PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝")
    private String auditStatus;
    
    @Schema(description = "是否有酬劳：true-有酬劳，false-无酬劳")
    private Boolean hasReward;
    
    @Schema(description = "关键词（搜索标题和描述）")
    private String keyword;
    
    @Schema(description = "排序方式：latest-最新发布，reward-酬劳最高，popular-回答最多，recent_answer-最新回答")
    private String sortBy;
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}

