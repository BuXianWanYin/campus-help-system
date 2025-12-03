package com.server.campushelpserver.controller.chat;

import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.chat.ChatMessage;
import com.server.campushelpserver.entity.chat.ChatSession;
import com.server.campushelpserver.entity.chat.dto.CreateSessionDTO;
import com.server.campushelpserver.entity.chat.dto.SendMessageDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.service.chat.ChatSessionService;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信控制器
 */
@RestController
@RequestMapping("/api/chat")
@Tag(name = "私信管理", description = "私信会话和消息相关接口")
public class ChatController {
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private UserService userService;
    
    @Operation(summary = "创建或获取会话", description = "如果会话已存在则返回现有会话ID，否则创建新会话")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/session/create")
    public Result<Map<String, Object>> createOrGetSession(
            @Parameter(description = "创建会话信息") @Validated @RequestBody CreateSessionDTO dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        Long userId = currentUser.getId();
        Long sessionId = chatSessionService.createOrGetSession(dto, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        return Result.success("会话创建成功", result);
    }
    
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话列表")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/session/list")
    public Result<List<ChatSession>> getSessionList() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        Long userId = currentUser.getId();
        List<ChatSession> sessions = chatSessionService.getSessionList(userId);
        return Result.success(sessions);
    }
    
    @Operation(summary = "获取会话详情", description = "获取指定会话的详情信息")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/session/{sessionId}")
    public Result<ChatSession> getSessionDetail(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        Long userId = currentUser.getId();
        ChatSession session = chatSessionService.getSessionDetail(sessionId, userId);
        return Result.success(session);
    }
    
    @Operation(summary = "发送消息", description = "在指定会话中发送消息")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/message/send")
    public Result<Map<String, Object>> sendMessage(
            @Parameter(description = "发送消息信息") @Validated @RequestBody SendMessageDTO dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        Long userId = currentUser.getId();
        Long messageId = chatSessionService.sendMessage(dto, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        return Result.success("消息发送成功", result);
    }
    
    @Operation(summary = "获取消息列表", description = "获取指定会话的所有消息列表")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/message/list/{sessionId}")
    public Result<List<ChatMessage>> getMessageList(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        Long userId = currentUser.getId();
        List<ChatMessage> messages = chatSessionService.getMessageList(sessionId, userId);
        return Result.success(messages);
    }
}

