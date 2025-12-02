package com.server.campushelpserver.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.campushelpserver.entity.message.SystemMessage;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.message.SystemMessageMapper;
import com.server.campushelpserver.service.message.SystemMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统消息Service实现类
 */
@Service
public class SystemMessageServiceImpl extends ServiceImpl<SystemMessageMapper, SystemMessage> implements SystemMessageService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemMessage sendMessage(Long userId, String type, String title, String content, String relatedType, Long relatedId) {
        SystemMessage message = new SystemMessage();
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setRelatedType(relatedType);
        message.setRelatedId(relatedId);
        message.setIsRead(0);
        message.setDeleteFlag(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        
        this.save(message);
        
        // 通过WebSocket实时推送消息
        try {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/system",
                message
            );
        } catch (Exception e) {
            // WebSocket推送失败不影响消息保存，只记录日志
            System.err.println("WebSocket推送消息失败: " + e.getMessage());
        }
        
        return message;
    }
    
    @Override
    public Page<SystemMessage> getMessagePage(Page<SystemMessage> page, Long userId, Boolean unreadOnly) {
        LambdaQueryWrapper<SystemMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemMessage::getUserId, userId);
        wrapper.eq(SystemMessage::getDeleteFlag, 0);
        
        if (unreadOnly != null && unreadOnly) {
            wrapper.eq(SystemMessage::getIsRead, 0);
        }
        
        wrapper.orderByDesc(SystemMessage::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SystemMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemMessage::getUserId, userId);
        wrapper.eq(SystemMessage::getIsRead, 0);
        wrapper.eq(SystemMessage::getDeleteFlag, 0);
        
        return this.count(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId, Long userId) {
        SystemMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (message.getIsRead() == 1) {
            return; // 已经已读，无需重复操作
        }
        
        message.setIsRead(1);
        message.setUpdateTime(LocalDateTime.now());
        this.updateById(message);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<SystemMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemMessage::getUserId, userId);
        wrapper.eq(SystemMessage::getIsRead, 0);
        wrapper.eq(SystemMessage::getDeleteFlag, 0);
        wrapper.set(SystemMessage::getIsRead, 1);
        wrapper.set(SystemMessage::getUpdateTime, LocalDateTime.now());
        
        this.update(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId, Long userId) {
        SystemMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        // 使用MyBatis Plus逻辑删除
        this.removeById(messageId);
    }
}

