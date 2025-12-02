const TOKEN_KEY = 'campus_help_token'
const REFRESH_TOKEN_KEY = 'campus_help_refresh_token'
const USER_INFO_KEY = 'campus_help_user_info'

/**
 * 获取 Token（使用 sessionStorage，每个标签页独立）
 */
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token（使用 sessionStorage，每个标签页独立）
 */
export function setToken(token) {
  return sessionStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token（使用 sessionStorage，每个标签页独立）
 */
export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取刷新 Token（使用 sessionStorage，每个标签页独立）
 */
export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新 Token（使用 sessionStorage，每个标签页独立）
 */
export function setRefreshToken(token) {
  return sessionStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/**
 * 获取用户信息（使用 sessionStorage，每个标签页独立）
 */
export function getUserInfo() {
  const userInfo = sessionStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息（使用 sessionStorage，每个标签页独立）
 */
export function setUserInfo(userInfo) {
  return sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息（使用 sessionStorage，每个标签页独立）
 */
export function removeUserInfo() {
  sessionStorage.removeItem(USER_INFO_KEY)
}

