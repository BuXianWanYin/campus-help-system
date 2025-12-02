import request from '@/utils/request'

/**
 * 系统消息相关 API
 */
export const messageApi = {
  /**
   * 分页查询系统消息
   * @param {Object} params 查询参数（pageNum, pageSize等）
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
   * @param {Number} messageId 消息ID
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
   * 删除消息
   * @param {Number} messageId 消息ID
   * @returns {Promise} 删除结果
   */
  deleteMessage(messageId) {
    return request.delete(`/message/${messageId}`)
  }
}

