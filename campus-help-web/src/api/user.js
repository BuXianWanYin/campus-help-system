import request from '@/utils/request'

/**
 * 用户相关 API
 */
export const userApi = {
  /**
   * 获取当前用户信息
   * @returns {Promise} 用户信息
   */
  getCurrentUser() {
    return request.get('/user/current')
  },
  
  /**
   * 更新当前用户信息
   * @param {Object} data 用户信息
   * @returns {Promise} 更新结果
   */
  updateCurrentUser(data) {
    return request.put('/user/current', data)
  },
  
  /**
   * 分页查询用户列表（管理员）
   * @param {Object} params 查询参数
   * @returns {Promise} 用户列表
   */
  getUserPage(params) {
    return request.get('/user/page', { params })
  },
  
  /**
   * 根据ID获取用户信息（管理员）
   * @param {Number} id 用户ID
   * @returns {Promise} 用户信息
   */
  getUserById(id) {
    return request.get(`/user/${id}`)
  },
  
  /**
   * 更新用户信息（管理员）
   * @param {Number} id 用户ID
   * @param {Object} data 用户信息
   * @returns {Promise} 更新结果
   */
  updateUser(id, data) {
    return request.put(`/admin/user/${id}`, data)
  },
  
  /**
   * 提交实名认证
   * @param {Object} data 认证信息
   * @returns {Promise} 提交结果
   */
  submitVerification(data) {
    return request.post('/user/verification/submit', data)
  },
  
  /**
   * 审核实名认证（管理员）
   * @param {Object} data 审核信息
   * @returns {Promise} 审核结果
   */
  auditVerification(data) {
    return request.post('/user/verification/audit', data)
  },
  
  /**
   * 修改密码
   * @param {Object} data 密码信息
   * @returns {Promise} 修改结果
   */
  changePassword(data) {
    return request.post('/user/change-password', data)
  }
}

