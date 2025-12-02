import request from '@/utils/request'

/**
 * 失物招领相关 API
 */
export const lostFoundApi = {
  /**
   * 发布失物
   * @param {Object} data 失物信息
   * @returns {Promise} 发布结果
   */
  publish(data) {
    return request.post('/lost-found/publish', data)
  },
  
  /**
   * 搜索失物列表
   * @param {Object} params 查询参数（pageNum, pageSize, type, category, status, location, sortBy等）
   * @returns {Promise} 失物列表
   */
  getList(params) {
    return request.get('/lost-found/list', { params })
  },
  
  /**
   * 获取失物详情
   * @param {Number} id 失物ID
   * @returns {Promise} 失物详情
   */
  getDetail(id) {
    return request.get(`/lost-found/${id}`)
  },
  
  /**
   * 申请认领
   * @param {Object} data 认领信息（lostFoundId, description, lostTime, otherInfo, proofImages等）
   * @returns {Promise} 认领结果
   */
  applyClaim(data) {
    return request.post('/lost-found/claim', data)
  },
  
  /**
   * 确认认领
   * @param {Number} claimRecordId 认领记录ID
   * @returns {Promise} 确认结果
   */
  confirmClaim(claimRecordId) {
    return request.post(`/lost-found/claim/${claimRecordId}/confirm`)
  },
  
  /**
   * 拒绝认领
   * @param {Number} claimRecordId 认领记录ID
   * @param {String} reason 拒绝原因
   * @returns {Promise} 拒绝结果
   */
  rejectClaim(claimRecordId, reason) {
    return request.post(`/lost-found/claim/${claimRecordId}/reject`, null, {
      params: { reason }
    })
  }
}

