package com.server.campushelpserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.StudyQuestion;
import com.server.campushelpserver.entity.dto.*;
import com.server.campushelpserver.entity.User;
import com.server.campushelpserver.service.QuestionService;
import com.server.campushelpserver.service.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学习问题控制器（用户端）
 */
@RestController
@RequestMapping("/api/question")
@Tag(name = "学习互助", description = "学习互助相关接口")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return null;
        }
        return user.getId();
    }
    
    @Operation(summary = "发布问题", description = "发布学习问题")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/publish")
    public Result<Long> publishQuestion(@Parameter(description = "问题信息") @Validated @RequestBody QuestionDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Long id = questionService.publishQuestion(dto, userId);
        return Result.success("发布成功", id);
    }
    
    @Operation(summary = "搜索问题列表", description = "根据条件搜索问题列表")
    @GetMapping("/list")
    public Result<Page<StudyQuestion>> getQuestionList(@Parameter(description = "搜索条件") QuestionSearchDTO searchDTO) {
        // 保存搜索历史（如果有关键词）
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty()) {
            try {
                Long userId = getCurrentUserId();
                if (userId != null) {
                    searchHistoryService.saveSearchHistory(userId, searchDTO.getKeyword().trim(), "STUDY_QUESTION");
                }
            } catch (Exception e) {
                // 保存搜索历史失败不影响搜索功能
            }
        }
        
        Page<StudyQuestion> page = questionService.getQuestionList(searchDTO);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "获取问题详情", description = "根据ID获取问题详细信息（包含回答列表）")
    @GetMapping("/{questionId}")
    public Result<QuestionDetailVO> getQuestionDetail(@Parameter(description = "问题ID") @PathVariable Long questionId) {
        Long userId = getCurrentUserId();
        QuestionDetailVO detail = questionService.getQuestionDetail(questionId, userId);
        return Result.success("查询成功", detail);
    }
    
    @Operation(summary = "回答问题", description = "回答学习问题")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{questionId}/answer")
    public Result<Long> answerQuestion(
            @Parameter(description = "问题ID") @PathVariable Long questionId,
            @Parameter(description = "回答信息") @Validated @RequestBody AnswerDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Long answerId = questionService.answerQuestion(questionId, dto, userId);
        return Result.success("回答成功", answerId);
    }
    
    @Operation(summary = "采纳答案", description = "采纳某个回答作为最佳答案")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{questionId}/accept")
    public Result<Void> acceptAnswer(
            @Parameter(description = "问题ID") @PathVariable Long questionId,
            @Parameter(description = "采纳信息") @Validated @RequestBody AcceptAnswerDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        questionService.acceptAnswer(questionId, dto.getAnswerId(), userId);
        return Result.success("采纳成功", null);
    }
    
    @Operation(summary = "取消问题", description = "取消自己发布的问题")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{questionId}/cancel")
    public Result<Void> cancelQuestion(@Parameter(description = "问题ID") @PathVariable Long questionId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        questionService.cancelQuestion(questionId, userId);
        return Result.success("取消成功", null);
    }
    
    @Operation(summary = "更新问题", description = "编辑已发布的问题")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{questionId}")
    public Result<Void> updateQuestion(
            @Parameter(description = "问题ID") @PathVariable Long questionId,
            @Parameter(description = "问题信息") @Validated @RequestBody QuestionDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        questionService.updateQuestion(questionId, dto, userId);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "获取我发布的问题列表", description = "获取当前登录用户发布的所有问题")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/my-published")
    public Result<Page<StudyQuestion>> getMyPublishedQuestions(
            @Parameter(description = "问题状态（可选）") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Page<StudyQuestion> page = questionService.getMyPublishedQuestions(userId, status, pageNum, pageSize);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "获取我回答的问题列表", description = "获取当前登录用户回答过的所有问题")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/my-answered")
    public Result<Page<StudyQuestion>> getMyAnsweredQuestions(
            @Parameter(description = "问题状态（可选）") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Page<StudyQuestion> page = questionService.getMyAnsweredQuestions(userId, status, pageNum, pageSize);
        return Result.success("查询成功", page);
    }
}

