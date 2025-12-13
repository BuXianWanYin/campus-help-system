/**
 * 统计相关API
 */
import request from '@/utils/request'

export const statisticsApi = {
  /**
   * 获取首页统计数据
   * @returns {Promise} 统计数据
   */
  getHomeStatistics() {
    return request.get('/statistics/home')
  }
}

