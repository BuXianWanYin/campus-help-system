import request from '@/utils/request'

/**
 * 管理员相关 API
 */
export const adminApi = {
  /**
   * 获取实名认证列表（支持按状态筛选）
   * @param {Object} params 查询参数（current, size, status）
   * @returns {Promise} 认证列表
   */
  getVerificationList(params) {
    return request.get('/admin/verification/list', { params })
  },
  
  /**
   * 获取待审核的实名认证列表（兼容旧接口）
   * @param {Object} params 查询参数
   * @returns {Promise} 认证列表
   */
  getPendingVerifications(params) {
    return request.get('/admin/verification/pending', { params })
  },
  
  /**
   * 审核实名认证
   * @param {Object} data 审核信息
   * @returns {Promise} 审核结果
   */
  auditVerification(data) {
    return request.post('/user/verification/audit', data)
  },
  
  /**
   * 封禁用户
   * @param {Object} data 封禁信息
   * @returns {Promise} 封禁结果
   */
  banUser(data) {
    return request.post('/admin/user/ban', data)
  },
  
  /**
   * 解封用户
   * @param {Number} userId 用户ID
   * @returns {Promise} 解封结果
   */
  unbanUser(userId) {
    return request.post(`/admin/user/unban/${userId}`)
  },
  
  /**
   * 创建用户
   * @param {Object} data 用户信息
   * @returns {Promise} 创建结果
   */
  createUser(data) {
    return request.post('/admin/user/create', data)
  },
  
  /**
   * 删除用户
   * @param {Number} userId 用户ID
   * @returns {Promise} 删除结果
   */
  deleteUser(userId) {
    return request.delete(`/admin/user/${userId}`)
  },
  
  /**
   * 获取待审核的失物招领列表
   * @param {Object} params 查询参数
   * @returns {Promise} 失物招领列表
   */
  getPendingLostFoundList(params) {
    return request.get('/admin/lost-found/pending', { params })
  },
  
  /**
   * 审核失物招领
   * @param {Number} id 失物ID
   * @param {Object} data 审核信息
   * @returns {Promise} 审核结果
   */
  auditLostFound(id, data) {
    return request.post(`/admin/lost-found/${id}/audit`, data)
  },
  
  /**
   * 获取待审核的商品列表
   * @param {Object} params 查询参数
   * @returns {Promise} 商品列表
   */
  getPendingGoodsList(params) {
    return request.get('/admin/goods/pending', { params })
  },
  
  /**
   * 审核商品
   * @param {Number} id 商品ID
   * @param {Object} data 审核信息
   * @returns {Promise} 审核结果
   */
  auditGoods(id, data) {
    return request.post(`/admin/goods/${id}/audit`, data)
  },
  
  /**
   * 获取数据概览统计信息
   * @param {String} period 统计周期：7days-最近7天，30days-最近30天，semester-本学期，year-本学年
   * @returns {Promise} 统计数据
   */
  getDashboardStats(period = '7days') {
    return request.get('/admin/dashboard/stats', { params: { period } })
  },
  
  /**
   * 下架失物招领
   * @param {Number} id 失物ID
   * @param {Object} data 下架信息（reason）
   * @returns {Promise} 下架结果
   */
  offshelfLostFound(id, data) {
    return request.post(`/admin/lost-found/${id}/offshelf`, null, { params: { reason: data.reason } })
  },
  
  /**
   * 下架商品
   * @param {Number} id 商品ID
   * @param {Object} data 下架信息（reason）
   * @returns {Promise} 下架结果
   */
  offshelfGoods(id, data) {
    return request.post(`/admin/goods/${id}/offshelf`, null, { params: { reason: data.reason } })
  },
  
  /**
   * 获取敏感词列表
   * @param {Object} params 查询参数（current, size, keyword）
   * @returns {Promise} 敏感词列表
   */
  getSensitiveWordList(params) {
    return request.get('/admin/sensitive-word/list', { params })
  },
  
  /**
   * 获取所有敏感词
   * @returns {Promise} 敏感词列表
   */
  getAllSensitiveWords() {
    return request.get('/admin/sensitive-word/all')
  },
  
  /**
   * 添加敏感词
   * @param {Object} data 敏感词信息
   * @returns {Promise} 添加结果
   */
  addSensitiveWord(data) {
    return request.post('/admin/sensitive-word/add', data)
  },
  
  /**
   * 更新敏感词
   * @param {Number} id 敏感词ID
   * @param {Object} data 敏感词信息
   * @returns {Promise} 更新结果
   */
  updateSensitiveWord(id, data) {
    return request.put(`/admin/sensitive-word/${id}`, data)
  },
  
  /**
   * 删除敏感词
   * @param {Number} id 敏感词ID
   * @returns {Promise} 删除结果
   */
  deleteSensitiveWord(id) {
    return request.delete(`/admin/sensitive-word/${id}`)
  },
  
  /**
   * 批量删除敏感词
   * @param {Array} ids 敏感词ID列表
   * @returns {Promise} 删除结果
   */
  batchDeleteSensitiveWords(ids) {
    return request.delete('/admin/sensitive-word/batch', { data: ids })
  },
  
  /**
   * 重新加载敏感词库
   * @returns {Promise} 重新加载结果
   */
  reloadSensitiveWords() {
    return request.post('/admin/sensitive-word/reload')
  }
}

