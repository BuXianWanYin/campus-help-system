package com.server.campushelpserver.entity.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 数据概览统计DTO
 */
@Data
@Schema(description = "数据概览统计信息")
public class DashboardStatsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "总用户数")
    private Long totalUsers;
    
    @Schema(description = "待审核认证数")
    private Long pendingVerifications;
    
    @Schema(description = "已认证用户数")
    private Long verifiedUsers;
    
    @Schema(description = "已封禁用户数")
    private Long bannedUsers;
    
    @Schema(description = "总互助次数（失物招领+闲置交易+学习互助）")
    private Long totalAssistanceCount;
    
    @Schema(description = "失物招领数量")
    private Long lostFoundCount;
    
    @Schema(description = "闲置交易数量")
    private Long goodsCount;
    
    @Schema(description = "学习互助数量")
    private Long studyQuestionCount;
    
    @Schema(description = "活跃用户数（最近30天有活动的用户）")
    private Long activeUsers;
    
    @Schema(description = "互助类型分布")
    private Map<String, Long> typeDistribution;
    
    @Schema(description = "每日互助趋势（最近7天）")
    private List<DailyTrendDTO> dailyTrends;
    
    @Schema(description = "上周总互助次数（用于计算变化百分比）")
    private Long lastWeekTotalAssistanceCount;
    
    @Schema(description = "上周活跃用户数（用于计算变化百分比）")
    private Long lastWeekActiveUsers;
}

