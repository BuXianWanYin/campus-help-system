/**
 * HTTP请求封装
 * 基于axios封装，包含请求拦截器、响应拦截器、错误处理等
 */

import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from './auth'

// 创建axios实例
const baseURL = import.meta.env.VITE_APP_BASE_API || '/api'
const timeout = parseInt(import.meta.env.VITE_APP_TIMEOUT) || 60000

const service = axios.create({
  baseURL: baseURL,
  timeout: timeout,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动添加Token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应和错误
service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的状态码不是200，则视为错误
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
      
      // 401未授权：清除token并跳转到登录页（认证接口除外）
      if (res.code === 401) {
        const isAuthApi = response.config?.url?.includes('/auth/')
        if (!isAuthApi) {
          ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            removeToken()
            location.reload()
          })
        }
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    console.error('响应错误:', error)
    
    let message = '请求失败'
    if (error.response) {
      // 根据HTTP状态码显示不同错误信息
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          const isAuthApi = error.config?.url?.includes('/auth/')
          if (isAuthApi) {
            message = error.response?.data?.message || '认证失败'
          } else {
            message = '未授权，请重新登录'
            removeToken()
            location.reload()
          }
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = `连接错误${error.response.status}`
      }
    } else if (error.request) {
      // 网络错误或超时
      if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        message = '请求超时，请检查网络连接或稍后重试'
      } else {
        message = '网络连接失败，请检查网络'
      }
    } else {
      message = error.message
    }
    
    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    
    return Promise.reject(error)
  }
)

export default service

