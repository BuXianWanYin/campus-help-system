/**
 * 认证工具函数
 * 管理Token、刷新Token、用户信息的存储和获取
 * 使用sessionStorage存储，每个标签页独立
 */

const TOKEN_KEY = 'campus_help_token'
const REFRESH_TOKEN_KEY = 'campus_help_refresh_token'
const USER_INFO_KEY = 'campus_help_user_info'

/**
 * 获取Token
 */
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 * @param {string} token - JWT Token
 */
export function setToken(token) {
  return sessionStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除Token
 */
export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取刷新Token
 */
export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新Token
 * @param {string} token - 刷新Token
 */
export function setRefreshToken(token) {
  return sessionStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/**
 * 获取用户信息
 * @returns {Object|null} 用户信息对象
 */
export function getUserInfo() {
  const userInfo = sessionStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * @param {Object} userInfo - 用户信息对象
 */
export function setUserInfo(userInfo) {
  return sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  sessionStorage.removeItem(USER_INFO_KEY)
}

