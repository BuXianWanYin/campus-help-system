import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    roles: (state) => state.userInfo?.roles || []
  },

  actions: {
    // 设置 Token
    setToken(token) {
      this.token = token
      setToken(token)
    },

    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    },

    // 登录
    async login(loginForm) {
      try {
        const response = await request.post('/auth/login', loginForm)
        if (response.code === 200) {
          this.setToken(response.data.token)
          this.setUserInfo(response.data.userInfo)
          return response
        }
        return Promise.reject(response)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // 登出
    async logout() {
      try {
        await request.post('/auth/logout')
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        removeToken()
        removeUserInfo()
      }
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await request.get('/auth/user/info')
        if (response.code === 200) {
          this.setUserInfo(response.data)
          return response.data
        }
        return Promise.reject(response)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // 刷新 Token
    async refreshToken() {
      try {
        const response = await request.post('/auth/refresh')
        if (response.code === 200) {
          this.setToken(response.data.token)
          return response.data.token
        }
        return Promise.reject(response)
      } catch (error) {
        this.logout()
        return Promise.reject(error)
      }
    }
  }
})

