package com.server.campushelpserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.SystemMessage;

/**
 * 系统消息Service接口
 */
public interface SystemMessageService extends IService<SystemMessage> {
    
    /**
     * 发送系统消息
     * @param userId 接收用户ID
     * @param type 消息类型
     * @param title 消息标题
     * @param content 消息内容
     * @param relatedType 关联类型
     * @param relatedId 关联ID
     * @return 系统消息
     */
    SystemMessage sendMessage(Long userId, String type, String title, String content, String relatedType, Long relatedId);
    
    /**
     * 分页查询系统消息
     * @param page 分页参数
     * @param userId 用户ID
     * @param unreadOnly 是否只查询未读消息
     * @return 消息分页列表
     */
    Page<SystemMessage> getMessagePage(Page<SystemMessage> page, Long userId, Boolean unreadOnly);
    
    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Long getUnreadCount(Long userId);
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markAsRead(Long messageId, Long userId);
    
    /**
     * 全部标记为已读
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);
    
    /**
     * 标记指定关联类型和关联ID的消息为已读（用于标记聊天相关消息）
     * @param userId 用户ID
     * @param relatedType 关联类型
     * @param relatedId 关联ID
     */
    void markRelatedMessagesAsRead(Long userId, String relatedType, Long relatedId);
    
    /**
     * 删除消息（逻辑删除）
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void deleteMessage(Long messageId, Long userId);
    
    /**
     * 发送消息给所有管理员
     * @param type 消息类型
     * @param title 消息标题
     * @param content 消息内容
     * @param relatedType 关联类型
     * @param relatedId 关联ID
     */
    void sendMessageToAllAdmins(String type, String title, String content, String relatedType, Long relatedId);
}

