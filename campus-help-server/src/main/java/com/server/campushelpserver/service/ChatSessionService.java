package com.server.campushelpserver.service;

import com.server.campushelpserver.entity.ChatMessage;
import com.server.campushelpserver.entity.ChatSession;
import com.server.campushelpserver.entity.dto.CreateSessionDTO;
import com.server.campushelpserver.entity.dto.SendMessageDTO;

import java.util.List;

/**
 * 私信会话服务接口
 */
public interface ChatSessionService {
    
    /**
     * 创建或获取会话
     * @param dto 创建会话DTO
     * @param userId 当前用户ID
     * @return 会话ID
     */
    Long createOrGetSession(CreateSessionDTO dto, Long userId);
    
    /**
     * 获取当前用户的所有会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getSessionList(Long userId);
    
    /**
     * 获取会话详情
     * @param sessionId 会话ID
     * @param userId 当前用户ID（用于权限验证）
     * @return 会话详情
     */
    ChatSession getSessionDetail(Long sessionId, Long userId);
    
    /**
     * 发送消息
     * @param dto 发送消息DTO
     * @param senderId 发送者ID
     * @return 消息ID
     */
    Long sendMessage(SendMessageDTO dto, Long senderId);
    
    /**
     * 获取会话消息列表
     * @param sessionId 会话ID
     * @param userId 当前用户ID（用于权限验证）
     * @return 消息列表
     */
    List<ChatMessage> getMessageList(Long sessionId, Long userId);
}

