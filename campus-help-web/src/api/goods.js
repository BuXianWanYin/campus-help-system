/**
 * 商品相关API
 * 提供商品发布、查询、管理等功能
 */

import request from '@/utils/request'

export const goodsApi = {
  /**
   * 发布商品
   * @param {Object} data - 商品信息
   * @returns {Promise} 发布结果
   */
  publish(data) {
    return request.post('/goods/publish', data)
  },
  
  /**
   * 搜索商品列表
   * @param {Object} params - 查询参数（pageNum, pageSize, keyword, category, condition, status, minPrice, maxPrice, sortBy等）
   * @returns {Promise} 商品列表
   */
  getList(params) {
    return request.get('/goods/list', { params })
  },
  
  /**
   * 获取商品详情
   * @param {number} id - 商品ID
   * @returns {Promise} 商品详情
   */
  getDetail(id) {
    return request.get(`/goods/${id}`)
  },
  
  /**
   * 编辑商品
   * @param {number} id - 商品ID
   * @param {Object} data - 商品信息
   * @returns {Promise} 编辑结果
   */
  update(id, data) {
    return request.put(`/goods/${id}`, data)
  },
  
  /**
   * 删除商品
   * @param {number} id - 商品ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return request.delete(`/goods/${id}`)
  },
  
  /**
   * 下架商品
   * @param {number} id - 商品ID
   * @returns {Promise} 下架结果
   */
  offshelf(id) {
    return request.post(`/goods/${id}/offshelf`)
  },
  
  /**
   * 重新上架商品
   * @param {number} id - 商品ID
   * @returns {Promise} 上架结果
   */
  reshelf(id) {
    return request.post(`/goods/${id}/reshelf`)
  },
  
  /**
   * 获取当前用户发布的商品列表
   * @param {Object} params - 查询参数（pageNum, pageSize, status, keyword等）
   * @returns {Promise} 商品列表
   */
  getMyPosts(params) {
    return request.get('/goods/my-posts', { params })
  }
}

