/**
 * 用户状态管理
 * 管理用户登录状态、用户信息等
 */

import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || null
  }),

  getters: {
    /**
     * 是否已登录
     */
    isLoggedIn: (state) => !!state.token,
    
    /**
     * 用户邮箱
     */
    email: (state) => state.userInfo?.email || '',
    
    /**
     * 用户昵称
     */
    nickname: (state) => state.userInfo?.nickname || '',
    
    /**
     * 用户角色
     */
    role: (state) => state.userInfo?.role || 'USER',
    
    /**
     * 是否已实名认证
     */
    isVerified: (state) => state.userInfo?.isVerified === 1,
    
    /**
     * 是否可以接任务
     */
    canAcceptTask: (state) => state.userInfo?.canAcceptTask === 1,
    
    /**
     * 是否是管理员
     */
    isAdmin: (state) => state.userInfo?.role === 'ADMIN'
  },

  actions: {
    /**
     * 设置Token
     * @param {string} token - JWT Token
     */
    setToken(token) {
      this.token = token
      setToken(token)
    },

    /**
     * 设置用户信息
     * @param {Object} userInfo - 用户信息对象
     */
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    },

    /**
     * 用户注册
     * @param {Object} user - 用户信息
     * @param {string} code - 验证码
     * @returns {Promise} 注册结果
     */
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

    /**
     * 用户登录（支持密码登录和验证码登录）
     * @param {string} email - 邮箱
     * @param {string} password - 密码（可选）
     * @param {string} code - 验证码（可选）
     * @returns {Promise} 登录结果
     */
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

    /**
     * 登出
     */
    logout() {
      this.token = ''
      this.userInfo = null
      removeToken()
      removeUserInfo()
    },
    
    /**
     * 获取当前用户信息
     * @returns {Promise} 用户信息
     */
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

