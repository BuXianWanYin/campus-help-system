/**
 * 常量配置
 */

// API 路径
export const API_BASE_URL = import.meta.env.VITE_APP_BASE_API

// 请求超时时间
export const REQUEST_TIMEOUT = parseInt(import.meta.env.VITE_APP_TIMEOUT)

// Token 相关
export const TOKEN_KEY = 'campus_help_token'
export const REFRESH_TOKEN_KEY = 'campus_help_refresh_token'
export const USER_INFO_KEY = 'campus_help_user_info'

// 响应状态码
export const RESPONSE_CODE = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500
}

// 路由名称
export const ROUTE_NAME = {
  LOGIN: 'Login',
  HOME: 'Home',
  DASHBOARD: 'Dashboard'
}

// 存储类型
export const STORAGE_TYPE = {
  LOCAL: 'localStorage',
  SESSION: 'sessionStorage'
}

