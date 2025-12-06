/**
 * 系统消息相关API
 * 提供系统消息查询、标记已读、删除等功能
 */

import request from '@/utils/request'

export const messageApi = {
  /**
   * 分页查询系统消息
   * @param {Object} params - 查询参数（pageNum, pageSize等）
   * @returns {Promise} 消息列表
   */
  getMessagePage(params) {
    return request.get('/message/page', { params })
  },
  
  /**
   * 获取未读消息数量
   * @returns {Promise} 未读消息数量
   */
  getUnreadCount() {
    return request.get('/message/unread-count')
  },
  
  /**
   * 标记消息为已读
   * @param {number} messageId - 消息ID
   * @returns {Promise} 标记结果
   */
  markAsRead(messageId) {
    return request.put(`/message/read/${messageId}`)
  },
  
  /**
   * 全部标记为已读
   * @returns {Promise} 标记结果
   */
  markAllAsRead() {
    return request.put('/message/read-all')
  },
  
  /**
   * 标记聊天相关消息为已读
   * @param {number} sessionId - 会话ID
   * @returns {Promise} 标记结果
   */
  markChatMessagesAsRead(sessionId) {
    return request.put(`/message/read-chat/${sessionId}`)
  },
  
  /**
   * 删除消息
   * @param {number} messageId - 消息ID
   * @returns {Promise} 删除结果
   */
  deleteMessage(messageId) {
    return request.delete(`/message/${messageId}`)
  }
}

/**
 * 私信相关API
 * 提供会话创建、消息发送、消息查询等功能
 */
export const chatApi = {
  /**
   * 创建或获取会话
   * @param {Object} data - 创建会话信息（targetUserId, relatedType, relatedId）
   * @returns {Promise} 会话ID
   */
  createOrGetSession(data) {
    return request.post('/chat/session/create', data)
  },
  
  /**
   * 获取会话列表
   * @returns {Promise} 会话列表
   */
  getSessionList() {
    return request.get('/chat/session/list')
  },
  
  /**
   * 获取会话详情
   * @param {number} sessionId - 会话ID
   * @returns {Promise} 会话详情
   */
  getSessionDetail(sessionId) {
    return request.get(`/chat/session/${sessionId}`)
  },
  
  /**
   * 发送消息
   * @param {Object} data - 发送消息信息（sessionId, messageType, content, images）
   * @returns {Promise} 消息ID
   */
  sendMessage(data) {
    return request.post('/chat/message/send', data)
  },
  
  /**
   * 获取消息列表
   * @param {number} sessionId - 会话ID
   * @returns {Promise} 消息列表
   */
  getMessageList(sessionId) {
    return request.get(`/chat/message/list/${sessionId}`)
  }
}

