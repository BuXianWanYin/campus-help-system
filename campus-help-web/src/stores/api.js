import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useApiStore = defineStore('api', {
  state: () => ({}),
  actions: {
    async testConnection() {
      return await request.get('/test')
    }
  }
})

