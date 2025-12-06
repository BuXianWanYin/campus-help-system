package com.server.campushelpserver.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 认领申请DTO
 */
@Data
@Schema(description = "认领申请DTO")
public class ClaimDTO {
    
    @NotNull(message = "失物ID不能为空")
    @Schema(description = "失物ID", required = true)
    private Long lostFoundId;
    
    @Schema(description = "物品特征描述")
    private String description;
    
    @Schema(description = "丢失时间")
    private LocalDateTime lostTime;
    
    @Schema(description = "其他信息")
    private String otherInfo;
    
    @Schema(description = "证明文件URL列表（最多3张）")
    private List<String> proofImages;
}

