/**
 * 失物招领相关API
 * 提供失物发布、查询、认领、管理等功能
 */

import request from '@/utils/request'

export const lostFoundApi = {
  /**
   * 发布失物
   * @param {Object} data - 失物信息
   * @returns {Promise} 发布结果
   */
  publish(data) {
    return request.post('/lost-found/publish', data)
  },
  
  /**
   * 搜索失物列表
   * @param {Object} params - 查询参数（pageNum, pageSize, type, category, status, location, sortBy等）
   * @returns {Promise} 失物列表
   */
  getList(params) {
    return request.get('/lost-found/list', { params })
  },
  
  /**
   * 获取失物详情
   * @param {number} id - 失物ID
   * @returns {Promise} 失物详情
   */
  getDetail(id) {
    return request.get(`/lost-found/${id}`)
  },
  
  /**
   * 申请认领
   * @param {Object} data - 认领信息（lostFoundId, description, lostTime, otherInfo, proofImages等）
   * @returns {Promise} 认领结果
   */
  applyClaim(data) {
    return request.post('/lost-found/claim', data)
  },
  
  /**
   * 确认认领
   * @param {number} claimRecordId - 认领记录ID
   * @returns {Promise} 确认结果
   */
  confirmClaim(claimRecordId) {
    return request.post(`/lost-found/claim/${claimRecordId}/confirm`)
  },
  
  /**
   * 拒绝认领
   * @param {number} claimRecordId - 认领记录ID
   * @param {string} reason - 拒绝原因
   * @returns {Promise} 拒绝结果
   */
  rejectClaim(claimRecordId, reason) {
    return request.post(`/lost-found/claim/${claimRecordId}/reject`, null, {
      params: { reason }
    })
  },

  /**
   * 获取当前用户发布的失物列表
   * @param {Object} params - 查询参数（pageNum, pageSize, type, category, status, location, sortBy等）
   * @returns {Promise} 失物列表
   */
  getMyPosts(params) {
    return request.get('/lost-found/my-posts', { params })
  },

  /**
   * 获取认领记录列表
   * @param {number} id - 失物ID
   * @returns {Promise} 认领记录列表
   */
  getClaimRecords(id) {
    return request.get(`/lost-found/${id}/claims`)
  },

  /**
   * 编辑失物
   * @param {number} id - 失物ID
   * @param {Object} data - 失物信息
   * @returns {Promise} 编辑结果
   */
  update(id, data) {
    return request.put(`/lost-found/${id}`, data)
  },

  /**
   * 删除失物
   * @param {number} id - 失物ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return request.delete(`/lost-found/${id}`)
  },

  /**
   * 关闭失物
   * @param {number} id - 失物ID
   * @returns {Promise} 关闭结果
   */
  close(id) {
    return request.post(`/lost-found/${id}/close`)
  },

  /**
   * 获取我的申请
   * @param {number} id - 失物ID
   * @returns {Promise} 认领记录
   */
  getMyClaim(id) {
    return request.get(`/lost-found/${id}/my-claim`)
  },

  /**
   * 更新认领申请
   * @param {number} claimRecordId - 认领记录ID
   * @param {Object} data - 认领信息
   * @returns {Promise} 更新结果
   */
  updateClaim(claimRecordId, data) {
    return request.put(`/lost-found/claim/${claimRecordId}`, data)
  },

  /**
   * 删除认领申请
   * @param {number} claimRecordId - 认领记录ID
   * @returns {Promise} 删除结果
   */
  deleteClaim(claimRecordId) {
    return request.delete(`/lost-found/claim/${claimRecordId}`)
  }
}

