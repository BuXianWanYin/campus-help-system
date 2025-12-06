/**
 * API状态管理
 * 提供API测试等功能
 */

import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useApiStore = defineStore('api', {
  state: () => ({}),
  actions: {
    /**
     * 测试API连接
     * @returns {Promise} 测试结果
     */
    async testConnection() {
      return await request.get('/test')
    }
  }
})

