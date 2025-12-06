import request from '@/utils/request'

// 学习互助相关 API
export const questionApi = {
  // 发布问题
  publish(data) {
    return request.post('/question/publish', data)
  },
  
  // 搜索问题列表
  getList(params) {
    return request.get('/question/list', { params })
  },
  
  // 获取问题详情
  getDetail(questionId) {
    return request.get(`/question/${questionId}`)
  },
  
  // 回答问题
  answer(questionId, data) {
    return request.post(`/question/${questionId}/answer`, data)
  },
  
  // 采纳答案
  acceptAnswer(questionId, data) {
    return request.post(`/question/${questionId}/accept`, data)
  },
  
  // 取消问题
  cancel(questionId) {
    return request.post(`/question/${questionId}/cancel`)
  },
  
  // 更新问题
  update(questionId, data) {
    return request.put(`/question/${questionId}`, data)
  },
  
  // 获取我发布的问题列表
  getMyPublished(params) {
    return request.get('/question/my-published', { params })
  },
  
  // 获取我回答的问题列表
  getMyAnswered(params) {
    return request.get('/question/my-answered', { params })
  }
}

// 管理员学习互助相关 API
export const adminQuestionApi = {
  // 获取待审核问题列表
  getPendingList(params) {
    return request.get('/admin/question/pending', { params })
  },
  
  // 审核问题
  audit(questionId, data) {
    return request.post(`/admin/question/${questionId}/audit`, data)
  },
  
  // 下架问题
  offshelf(questionId, reason) {
    return request.post(`/admin/question/${questionId}/offshelf`, null, {
      params: { reason }
    })
  },
  
  // 获取问题下的所有回答
  getAnswersByQuestionId(questionId) {
    return request.get(`/admin/question/${questionId}/answers`)
  },
  
  // 审核回答
  auditAnswer(answerId, data) {
    return request.post(`/admin/question/answer/${answerId}/audit`, data)
  },
  
  // 删除回答
  deleteAnswer(answerId, reason) {
    return request.delete(`/admin/question/answer/${answerId}`, null, {
      params: { reason }
    })
  }
}

