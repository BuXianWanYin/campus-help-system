package com.server.campushelpserver.controller.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.message.SystemMessage;
import com.server.campushelpserver.service.message.SystemMessageService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统消息控制器
 */
@RestController
@RequestMapping("/api/message")
@Tag(name = "系统消息", description = "系统消息相关接口")
public class SystemMessageController {
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Autowired
    private com.server.campushelpserver.service.user.UserService userService;
    
    @Operation(summary = "分页查询系统消息", description = "获取当前用户的系统消息列表")
    @GetMapping("/page")
    public Result<Page<SystemMessage>> getMessagePage(
            @Parameter(description = "分页参数") Page<SystemMessage> page,
            @Parameter(description = "是否只查询未读消息") @RequestParam(required = false) Boolean unreadOnly) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        com.server.campushelpserver.entity.user.User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Long userId = user.getId();
        Page<SystemMessage> result = systemMessageService.getMessagePage(page, userId, unreadOnly);
        return Result.success("查询成功", result);
    }
    
    @Operation(summary = "获取未读消息数量", description = "获取当前用户的未读消息数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        com.server.campushelpserver.entity.user.User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Long userId = user.getId();
        Long count = systemMessageService.getUnreadCount(userId);
        return Result.success("查询成功", count);
    }
    
    @Operation(summary = "标记消息为已读", description = "标记单条消息为已读")
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@Parameter(description = "消息ID") @PathVariable Long id) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        com.server.campushelpserver.entity.user.User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Long userId = user.getId();
        systemMessageService.markAsRead(id, userId);
        return Result.success("标记成功", null);
    }
    
    @Operation(summary = "全部标记为已读", description = "将当前用户的所有未读消息标记为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        com.server.campushelpserver.entity.user.User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Long userId = user.getId();
        systemMessageService.markAllAsRead(userId);
        return Result.success("全部标记为已读", null);
    }
    
    @Operation(summary = "标记聊天相关消息为已读", description = "标记指定会话的聊天相关系统消息为已读")
    @PutMapping("/read-chat/{sessionId}")
    public Result<Void> markChatMessagesAsRead(@Parameter(description = "会话ID") @PathVariable Long sessionId) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        com.server.campushelpserver.entity.user.User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Long userId = user.getId();
        systemMessageService.markRelatedMessagesAsRead(userId, "CHAT", sessionId);
        return Result.success("标记成功", null);
    }
    
    @Operation(summary = "删除消息", description = "删除单条消息（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@Parameter(description = "消息ID") @PathVariable Long id) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        com.server.campushelpserver.entity.user.User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Long userId = user.getId();
        systemMessageService.deleteMessage(id, userId);
        return Result.success("删除成功", null);
    }
}

