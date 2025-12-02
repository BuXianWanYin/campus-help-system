import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    email: (state) => state.userInfo?.email || '',
    nickname: (state) => state.userInfo?.nickname || '',
    role: (state) => state.userInfo?.role || 'USER',
    isVerified: (state) => state.userInfo?.isVerified === 1,
    canAcceptTask: (state) => state.userInfo?.canAcceptTask === 1,
    isAdmin: (state) => state.userInfo?.role === 'ADMIN'
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

    // 用户注册
    async register(user, code) {
      try {
        const response = await authApi.register(user, code)
        if (response.code === 200) {
          this.setToken(response.data.token)
          this.setUserInfo(response.data.user)
          return response
        }
        return Promise.reject(response)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // 用户登录（支持密码登录和验证码登录）
    async login(email, password, code) {
      try {
        const response = await authApi.login(email, password, code)
        if (response.code === 200) {
          this.setToken(response.data.token)
          this.setUserInfo(response.data.user)
          return response
        }
        return Promise.reject(response)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // 登出
    logout() {
      this.token = ''
      this.userInfo = null
      removeToken()
      removeUserInfo()
    },
    
    // 获取当前用户信息
    async fetchCurrentUser() {
      try {
        const { userApi } = await import('@/api')
        const response = await userApi.getCurrentUser()
        if (response.code === 200) {
          this.setUserInfo(response.data)
          return response.data
        }
        return Promise.reject(response)
      } catch (error) {
        return Promise.reject(error)
      }
    }
  }
})

