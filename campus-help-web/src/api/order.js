import request from '@/utils/request'

/**
 * 订单相关 API
 */
export const orderApi = {
  /**
   * 创建订单
   * @param {Object} data 订单信息（goodsId, quantity, tradeMethod, addressId等）
   * @returns {Promise} 创建结果
   */
  create(data) {
    return request.post('/order/create', data)
  },
  
  /**
   * 获取我的订单列表
   * @param {Object} params 查询参数（pageNum, pageSize, role, status, tradeMethod, startTime, endTime, keyword等）
   * @returns {Promise} 订单列表
   */
  getList(params) {
    return request.get('/order/list', { params })
  },
  
  /**
   * 获取订单详情
   * @param {Number} id 订单ID
   * @returns {Promise} 订单详情
   */
  getDetail(id) {
    return request.get(`/order/${id}`)
  },
  
  /**
   * 卖家改价
   * @param {Number} id 订单ID
   * @param {Object} data 价格信息（newPrice）
   * @returns {Promise} 改价结果
   */
  updatePrice(id, data) {
    return request.put(`/order/${id}/price`, data)
  },
  
  /**
   * 买家付款
   * @param {Number} id 订单ID
   * @returns {Promise} 付款结果
   */
  pay(id) {
    return request.post(`/order/${id}/pay`)
  },
  
  /**
   * 卖家发货
   * @param {Number} id 订单ID
   * @param {Object} data 发货信息（trackingNumber, logisticsCompany）
   * @returns {Promise} 发货结果
   */
  ship(id, data) {
    return request.post(`/order/${id}/ship`, data)
  },
  
  /**
   * 买家确认收货
   * @param {Number} id 订单ID
   * @returns {Promise} 确认结果
   */
  confirmReceipt(id) {
    return request.post(`/order/${id}/confirm`)
  },
  
  /**
   * 取消订单
   * @param {Number} id 订单ID
   * @param {String} reason 取消原因
   * @returns {Promise} 取消结果
   */
  cancel(id, reason) {
    return request.post(`/order/${id}/cancel`, null, {
      params: { reason }
    })
  },
  
  /**
   * 根据会话ID获取订单
   * @param {Number} sessionId 会话ID
   * @returns {Promise} 订单信息
   */
  getBySessionId(sessionId) {
    return request.get(`/order/by-session/${sessionId}`)
  }
}

