/**
 * 收货地址相关API
 * 提供收货地址的增删改查、设置默认地址等功能
 */

import request from '@/utils/request'

export const addressApi = {
  /**
   * 获取收货地址列表
   * @returns {Promise} 地址列表
   */
  getList() {
    return request.get('/address/list')
  },
  
  /**
   * 添加收货地址
   * @param {Object} data - 地址信息
   * @returns {Promise} 添加结果
   */
  add(data) {
    return request.post('/address/add', data)
  },
  
  /**
   * 更新收货地址
   * @param {number} id - 地址ID
   * @param {Object} data - 地址信息
   * @returns {Promise} 更新结果
   */
  update(id, data) {
    return request.put(`/address/${id}`, data)
  },
  
  /**
   * 删除收货地址
   * @param {number} id - 地址ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return request.delete(`/address/${id}`)
  },
  
  /**
   * 设置默认地址
   * @param {number} id - 地址ID
   * @returns {Promise} 设置结果
   */
  setDefault(id) {
    return request.put(`/address/${id}/default`)
  }
}

