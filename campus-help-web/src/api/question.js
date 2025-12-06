/**
 * 学习互助相关API
 * 提供问题发布、查询、回答、采纳等功能
 */

import request from '@/utils/request'

export const questionApi = {
  /**
   * 发布问题
   * @param {Object} data - 问题信息
   * @returns {Promise} 发布结果
   */
  publish(data) {
    return request.post('/question/publish', data)
  },
  
  /**
   * 搜索问题列表
   * @param {Object} params - 查询参数
   * @returns {Promise} 问题列表
   */
  getList(params) {
    return request.get('/question/list', { params })
  },
  
  /**
   * 获取问题详情
   * @param {number} questionId - 问题ID
   * @returns {Promise} 问题详情
   */
  getDetail(questionId) {
    return request.get(`/question/${questionId}`)
  },
  
  /**
   * 回答问题
   * @param {number} questionId - 问题ID
   * @param {Object} data - 回答内容
   * @returns {Promise} 回答结果
   */
  answer(questionId, data) {
    return request.post(`/question/${questionId}/answer`, data)
  },
  
  /**
   * 采纳答案
   * @param {number} questionId - 问题ID
   * @param {Object} data - 答案信息
   * @returns {Promise} 采纳结果
   */
  acceptAnswer(questionId, data) {
    return request.post(`/question/${questionId}/accept`, data)
  },
  
  /**
   * 取消问题
   * @param {number} questionId - 问题ID
   * @returns {Promise} 取消结果
   */
  cancel(questionId) {
    return request.post(`/question/${questionId}/cancel`)
  },
  
  /**
   * 更新问题
   * @param {number} questionId - 问题ID
   * @param {Object} data - 问题信息
   * @returns {Promise} 更新结果
   */
  update(questionId, data) {
    return request.put(`/question/${questionId}`, data)
  },
  
  /**
   * 获取我发布的问题列表
   * @param {Object} params - 查询参数
   * @returns {Promise} 问题列表
   */
  getMyPublished(params) {
    return request.get('/question/my-published', { params })
  },
  
  /**
   * 获取我回答的问题列表
   * @param {Object} params - 查询参数
   * @returns {Promise} 问题列表
   */
  getMyAnswered(params) {
    return request.get('/question/my-answered', { params })
  }
}

/**
 * 管理员学习互助相关API
 * 提供问题审核、下架、回答管理等功能
 */
export const adminQuestionApi = {
  /**
   * 获取待审核问题列表
   * @param {Object} params - 查询参数
   * @returns {Promise} 问题列表
   */
  getPendingList(params) {
    return request.get('/admin/question/pending', { params })
  },
  
  /**
   * 审核问题
   * @param {number} questionId - 问题ID
   * @param {Object} data - 审核信息
   * @returns {Promise} 审核结果
   */
  audit(questionId, data) {
    return request.post(`/admin/question/${questionId}/audit`, data)
  },
  
  /**
   * 下架问题
   * @param {number} questionId - 问题ID
   * @param {string} reason - 下架原因
   * @returns {Promise} 下架结果
   */
  offshelf(questionId, reason) {
    return request.post(`/admin/question/${questionId}/offshelf`, null, {
      params: { reason }
    })
  },
  
  /**
   * 获取问题下的所有回答
   * @param {number} questionId - 问题ID
   * @returns {Promise} 回答列表
   */
  getAnswersByQuestionId(questionId) {
    return request.get(`/admin/question/${questionId}/answers`)
  },
  
  /**
   * 审核回答
   * @param {number} answerId - 回答ID
   * @param {Object} data - 审核信息
   * @returns {Promise} 审核结果
   */
  auditAnswer(answerId, data) {
    return request.post(`/admin/question/answer/${answerId}/audit`, data)
  },
  
  /**
   * 删除回答
   * @param {number} answerId - 回答ID
   * @param {string} reason - 删除原因
   * @returns {Promise} 删除结果
   */
  deleteAnswer(answerId, reason) {
    return request.delete(`/admin/question/answer/${answerId}`, null, {
      params: { reason }
    })
  }
}

