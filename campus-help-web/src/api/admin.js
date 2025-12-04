import request from '@/utils/request'

/**
 * 管理员相关 API
 */
export const adminApi = {
  /**
   * 获取待审核的实名认证列表
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
  }
}

