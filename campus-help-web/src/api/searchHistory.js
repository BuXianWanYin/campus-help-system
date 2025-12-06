/**
 * 搜索历史相关API
 * 提供搜索历史的查询、删除、清空等功能
 */

import request from '@/utils/request'

export const searchHistoryApi = {
  /**
   * 获取搜索历史列表
   * @param {string} moduleType - 模块类型：LOST_FOUND-失物招领，GOODS-闲置交易，STUDY-学习互助
   * @param {number} limit - 限制数量
   * @returns {Promise} 搜索历史列表
   */
  getList(moduleType, limit = 10) {
    return request.get('/search-history/list', {
      params: { moduleType, limit }
    })
  },
  
  /**
   * 删除搜索历史
   * @param {number} id - 搜索历史ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return request.delete(`/search-history/${id}`)
  },
  
  /**
   * 清空搜索历史
   * @param {string} moduleType - 模块类型
   * @returns {Promise} 清空结果
   */
  clear(moduleType) {
    return request.delete('/search-history/clear', {
      params: { moduleType }
    })
  }
}

