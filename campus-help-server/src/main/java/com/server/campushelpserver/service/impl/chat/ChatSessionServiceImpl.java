package com.server.campushelpserver.service.impl.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.campushelpserver.entity.chat.ChatMessage;
import com.server.campushelpserver.entity.chat.ChatSession;
import com.server.campushelpserver.entity.chat.dto.CreateSessionDTO;
import com.server.campushelpserver.entity.chat.dto.SendMessageDTO;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.chat.ChatMessageMapper;
import com.server.campushelpserver.mapper.chat.ChatSessionMapper;
import com.server.campushelpserver.service.chat.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 私信会话服务实现类
 */
@Service
public class ChatSessionServiceImpl implements ChatSessionService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrGetSession(CreateSessionDTO dto, Long userId) {
        // 1. 验证参数
        if (dto == null || dto.getTargetUserId() == null) {
            throw new BusinessException("目标用户ID不能为空");
        }
        
        // 2. 验证不能与自己创建会话
        if (dto.getTargetUserId().equals(userId)) {
            throw new BusinessException("不能与自己创建会话");
        }
        
        // 3. 确定user1和user2（较小的ID作为user1，保证唯一性）
        Long user1Id = userId < dto.getTargetUserId() ? userId : dto.getTargetUserId();
        Long user2Id = userId < dto.getTargetUserId() ? dto.getTargetUserId() : userId;
        
        // 4. 查询是否已存在会话
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUser1Id, user1Id)
               .eq(ChatSession::getUser2Id, user2Id);
        
        // 如果有关联类型和ID，也作为查询条件
        if (StringUtils.hasText(dto.getRelatedType()) && dto.getRelatedId() != null) {
            wrapper.eq(ChatSession::getRelatedType, dto.getRelatedType())
                   .eq(ChatSession::getRelatedId, dto.getRelatedId());
        }
        
        wrapper.eq(ChatSession::getDeleteFlag, 0);
        
        ChatSession existingSession = chatSessionMapper.selectOne(wrapper);
        
        if (existingSession != null) {
            return existingSession.getId();
        }
        
        // 4. 创建新会话
        ChatSession session = new ChatSession();
        session.setUser1Id(user1Id);
        session.setUser2Id(user2Id);
        session.setRelatedType(dto.getRelatedType());
        session.setRelatedId(dto.getRelatedId());
        session.setDeleteFlag(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        
        chatSessionMapper.insert(session);
        
        return session.getId();
    }
    
    @Override
    public List<ChatSession> getSessionList(Long userId) {
        // 查询包含该用户的所有会话
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ChatSession::getUser1Id, userId)
                         .or()
                         .eq(ChatSession::getUser2Id, userId))
               .eq(ChatSession::getDeleteFlag, 0)
               .orderByDesc(ChatSession::getLastMessageTime, ChatSession::getCreateTime);
        
        return chatSessionMapper.selectList(wrapper);
    }
    
    @Override
    public ChatSession getSessionDetail(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        
        if (session == null || session.getDeleteFlag() == 1) {
            throw new BusinessException("会话不存在");
        }
        
        // 验证用户是否有权限查看该会话
        if (!session.getUser1Id().equals(userId) && !session.getUser2Id().equals(userId)) {
            throw new BusinessException("无权查看该会话");
        }
        
        return session;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(SendMessageDTO dto, Long senderId) {
        // 1. 验证会话是否存在
        ChatSession session = chatSessionMapper.selectById(dto.getSessionId());
        if (session == null || session.getDeleteFlag() == 1) {
            throw new BusinessException("会话不存在");
        }
        
        // 2. 验证发送者是会话参与者
        if (!session.getUser1Id().equals(senderId) && !session.getUser2Id().equals(senderId)) {
            throw new BusinessException("无权发送消息");
        }
        
        // 3. 确定接收者
        Long receiverId = session.getUser1Id().equals(senderId) ? session.getUser2Id() : session.getUser1Id();
        
        // 4. 验证消息内容
        if ("TEXT".equals(dto.getMessageType())) {
            if (!StringUtils.hasText(dto.getContent())) {
                throw new BusinessException("文字消息内容不能为空");
            }
        } else if ("IMAGE".equals(dto.getMessageType())) {
            if (dto.getImages() == null || dto.getImages().isEmpty()) {
                throw new BusinessException("图片消息必须包含至少一张图片");
            }
        } else {
            throw new BusinessException("不支持的消息类型");
        }
        
        // 5. 创建消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(dto.getSessionId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageType(dto.getMessageType());
        message.setContent(dto.getContent());
        
        // 转换图片列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                message.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        }
        
        message.setIsRead(0);
        message.setDeleteFlag(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        
        // 6. 更新会话的最后消息时间和内容
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getId, dto.getSessionId())
                    .set(ChatSession::getLastMessageTime, LocalDateTime.now())
                    .set(ChatSession::getLastMessageContent, dto.getContent())
                    .set(ChatSession::getUpdateTime, LocalDateTime.now());
        chatSessionMapper.update(null, updateWrapper);
        
        // 7. 通过WebSocket实时推送给接收者
        messagingTemplate.convertAndSendToUser(
            receiverId.toString(),
            "/queue/chat",
            message
        );
        
        return message.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChatMessage> getMessageList(Long sessionId, Long userId) {
        // 1. 验证会话权限
        getSessionDetail(sessionId, userId);
        
        // 2. 查询消息列表
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getDeleteFlag, 0)
               .orderByAsc(ChatMessage::getCreateTime);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        
        // 3. 标记消息为已读
        chatMessageMapper.markAsRead(sessionId, userId);
        
        return messages;
    }
}

