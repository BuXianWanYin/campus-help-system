package com.server.campushelpserver.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.study.StudyQuestion;
import com.server.campushelpserver.entity.study.dto.QuestionAuditDTO;
import com.server.campushelpserver.entity.study.dto.QuestionSearchDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.service.study.QuestionService;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import com.server.campushelpserver.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员学习问题控制器
 */
@RestController
@RequestMapping("/api/admin/question")
@Tag(name = "管理员-学习互助", description = "管理员学习互助管理相关接口")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminQuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前管理员ID
     */
    private Long getCurrentAdminId() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new BusinessException("未登录");
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException("无权限");
        }
        return user.getId();
    }
    
    @Operation(summary = "获取待审核问题列表", description = "分页查询待审核的学习问题")
    @GetMapping("/pending")
    public Result<Page<StudyQuestion>> getPendingQuestionList(
            @Parameter(description = "搜索条件") QuestionSearchDTO searchDTO) {
        Page<StudyQuestion> result = questionService.getPendingAuditList(searchDTO);
        return Result.success("查询成功", result);
    }
    
    @Operation(summary = "审核问题", description = "审核学习问题（通过或拒绝）")
    @PostMapping("/{questionId}/audit")
    public Result<Void> auditQuestion(
            @Parameter(description = "问题ID") @PathVariable Long questionId,
            @Parameter(description = "审核信息") @Validated @RequestBody QuestionAuditDTO dto) {
        Long adminId = getCurrentAdminId();
        
        // 验证拒绝原因
        if (!dto.getApproved() && (dto.getReason() == null || dto.getReason().trim().isEmpty())) {
            throw new BusinessException("拒绝时必须填写拒绝原因");
        }
        
        questionService.auditQuestion(questionId, dto.getApproved(), dto.getReason(), adminId);
        
        String message = dto.getApproved() ? "审核通过" : "审核拒绝";
        return Result.success(message, null);
    }
    
    @Operation(summary = "下架问题", description = "管理员下架问题")
    @PostMapping("/{questionId}/offshelf")
    public Result<Void> offshelfQuestion(
            @Parameter(description = "问题ID") @PathVariable Long questionId,
            @Parameter(description = "下架原因") @RequestParam String reason) {
        Long adminId = getCurrentAdminId();
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("下架原因不能为空");
        }
        
        questionService.offshelfQuestion(questionId, reason.trim(), adminId);
        return Result.success("下架成功", null);
    }
    
    @Operation(summary = "获取问题下的所有回答", description = "获取指定问题下的所有回答列表（用于回答管理）")
    @GetMapping("/{questionId}/answers")
    public Result<java.util.List<com.server.campushelpserver.entity.study.StudyAnswer>> getAnswersByQuestionId(
            @Parameter(description = "问题ID") @PathVariable Long questionId) {
        java.util.List<com.server.campushelpserver.entity.study.StudyAnswer> answers = questionService.getAnswersByQuestionId(questionId);
        return Result.success("查询成功", answers);
    }
    
    @Operation(summary = "审核回答", description = "审核学习问题的回答（通过或拒绝）")
    @PostMapping("/answer/{answerId}/audit")
    public Result<Void> auditAnswer(
            @Parameter(description = "回答ID") @PathVariable Long answerId,
            @Parameter(description = "审核信息") @Validated @RequestBody QuestionAuditDTO dto) {
        Long adminId = getCurrentAdminId();
        
        // 验证拒绝原因
        if (!dto.getApproved() && (dto.getReason() == null || dto.getReason().trim().isEmpty())) {
            throw new BusinessException("拒绝时必须填写拒绝原因");
        }
        
        questionService.auditAnswer(answerId, dto.getApproved(), dto.getReason(), adminId);
        
        String message = dto.getApproved() ? "审核通过" : "审核拒绝";
        return Result.success(message, null);
    }
}

