package com.server.campushelpserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.Goods;
import com.server.campushelpserver.entity.LostFound;
import com.server.campushelpserver.entity.StudyQuestion;
import com.server.campushelpserver.mapper.GoodsMapper;
import com.server.campushelpserver.mapper.LostFoundMapper;
import com.server.campushelpserver.mapper.StudyQuestionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器
 * 提供公共统计接口
 */
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计", description = "公共统计相关接口")
public class StatisticsController {
    
    @Autowired
    private LostFoundMapper lostFoundMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private StudyQuestionMapper studyQuestionMapper;
    
    /**
     * 获取首页统计数据
     * 统计所有已审核通过的历史数据（包括所有状态）
     */
    @Operation(summary = "获取首页统计数据", description = "统计所有已审核通过的历史数据，包括失物招领、闲置交易、学习互助")
    @GetMapping("/home")
    public Result<Map<String, Long>> getHomeStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        // 统计失物招领：所有已审核通过的（包括所有状态）
        LambdaQueryWrapper<LostFound> lostFoundWrapper = new LambdaQueryWrapper<>();
        lostFoundWrapper.eq(LostFound::getAuditStatus, "APPROVED")
                       .eq(LostFound::getDeleteFlag, 0);
        Long lostFoundCount = lostFoundMapper.selectCount(lostFoundWrapper);
        stats.put("lostFoundCount", lostFoundCount);
        
        // 统计商品：所有已审核通过的（包括所有状态）
        LambdaQueryWrapper<Goods> goodsWrapper = new LambdaQueryWrapper<>();
        goodsWrapper.eq(Goods::getAuditStatus, "APPROVED")
                   .eq(Goods::getDeleteFlag, 0);
        Long goodsCount = goodsMapper.selectCount(goodsWrapper);
        stats.put("goodsCount", goodsCount);
        
        // 统计学习问题：所有已审核通过的（包括所有状态）
        LambdaQueryWrapper<StudyQuestion> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(StudyQuestion::getAuditStatus, "APPROVED")
                      .eq(StudyQuestion::getDeleteFlag, 0);
        Long studyQuestionCount = studyQuestionMapper.selectCount(questionWrapper);
        stats.put("studyQuestionCount", studyQuestionCount);
        
        return Result.success("查询成功", stats);
    }
}

