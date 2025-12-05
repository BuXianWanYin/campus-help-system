package com.server.campushelpserver.service.impl.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.campushelpserver.entity.chat.ChatMessage;
import com.server.campushelpserver.entity.chat.ChatSession;
import com.server.campushelpserver.entity.chat.dto.CreateSessionDTO;
import com.server.campushelpserver.entity.chat.dto.SendMessageDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.chat.ChatMessageMapper;
import com.server.campushelpserver.mapper.chat.ChatSessionMapper;
import com.server.campushelpserver.mapper.user.UserMapper;
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
    
    @Autowired
    private UserMapper userMapper;
    
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
        
        // 4. 查询是否已存在会话（同一对用户只应该有一个会话，不考虑 related_id）
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUser1Id, user1Id)
               .eq(ChatSession::getUser2Id, user2Id)
               .eq(ChatSession::getDeleteFlag, 0);
        
        ChatSession existingSession = chatSessionMapper.selectOne(wrapper);
        
        if (existingSession != null) {
            // 如果新请求有关联信息，则更新关联信息（即使现有会话已有，也更新，确保是最新的关联信息）
            if (StringUtils.hasText(dto.getRelatedType()) && dto.getRelatedId() != null) {
                // 检查是否需要更新：如果现有会话没有关联信息，或者关联信息不同，则更新
                boolean needUpdate = false;
                if (!StringUtils.hasText(existingSession.getRelatedType()) || existingSession.getRelatedId() == null) {
                    // 现有会话没有关联信息，需要更新
                    needUpdate = true;
                } else if (!dto.getRelatedType().equals(existingSession.getRelatedType()) 
                    || !dto.getRelatedId().equals(existingSession.getRelatedId())) {
                    // 关联信息不同，需要更新
                    needUpdate = true;
                }
                
                if (needUpdate) {
                    LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ChatSession::getId, existingSession.getId())
                                .set(ChatSession::getRelatedType, dto.getRelatedType())
                                .set(ChatSession::getRelatedId, dto.getRelatedId())
                                .set(ChatSession::getUpdateTime, LocalDateTime.now());
                    chatSessionMapper.update(null, updateWrapper);
                    // 更新本地对象，确保返回的数据是最新的
                    existingSession.setRelatedType(dto.getRelatedType());
                    existingSession.setRelatedId(dto.getRelatedId());
                }
            }
            return existingSession.getId();
        }
        
        // 5. 创建新会话
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
               .orderByDesc(ChatSession::getLastMessageTime)
               .orderByDesc(ChatSession::getCreateTime);
        
        List<ChatSession> sessions = chatSessionMapper.selectList(wrapper);
        
        // 填充对方用户信息和未读数量
        for (ChatSession session : sessions) {
            fillOtherUserInfo(session, userId);
            fillUnreadCount(session, userId);
        }
        
        return sessions;
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
        
        // 填充对方用户信息和未读数量
        fillOtherUserInfo(session, userId);
        fillUnreadCount(session, userId);
        
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
        } else if ("GOODS_CARD".equals(dto.getMessageType())) {
            // 商品卡片消息：content字段存储商品ID（JSON格式：{"goodsId": 123}）
            if (!StringUtils.hasText(dto.getContent())) {
                throw new BusinessException("商品卡片消息内容不能为空");
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
        try {
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/chat",
                message
            );
        } catch (Exception e) {
            // WebSocket推送失败不影响消息保存，只记录日志
            System.err.println("WebSocket推送聊天消息失败: " + e.getMessage());
        }
        
        // 9. 同时推送给发送者（用于实时确认消息已发送）
        try {
            messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/chat",
                message
            );
        } catch (Exception e) {
            // WebSocket推送失败不影响消息保存，只记录日志
            System.err.println("WebSocket推送聊天消息给发送者失败: " + e.getMessage());
        }
        
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
    
    /**
     * 填充对方用户信息
     */
    private void fillOtherUserInfo(ChatSession session, Long currentUserId) {
        // 确定对方用户ID
        Long otherUserId = session.getUser1Id().equals(currentUserId) 
            ? session.getUser2Id() 
            : session.getUser1Id();
        
        if (otherUserId != null) {
            User user = userMapper.selectById(otherUserId);
            if (user != null) {
                // 创建简化的用户对象（只包含必要字段，避免返回敏感信息）
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                session.setOtherUser(simpleUser);
            }
        }
    }
    
    /**
     * 填充未读消息数量
     */
    private void fillUnreadCount(ChatSession session, Long currentUserId) {
        // 查询当前用户在该会话中的未读消息数量
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, session.getId())
               .eq(ChatMessage::getReceiverId, currentUserId)
               .eq(ChatMessage::getIsRead, 0)
               .eq(ChatMessage::getDeleteFlag, 0);
        
        long count = chatMessageMapper.selectCount(wrapper);
        session.setUnreadCount((int) count);
    }
}

