/**
 * 认证相关API
 * 提供用户注册、登录、验证码、密码重置等功能
 */

import request from '@/utils/request'

export const authApi = {
  /**
   * 用户注册
   * @param {Object} user - 用户信息
   * @param {string} code - 验证码
   * @returns {Promise} 注册结果
   */
  register(user, code) {
    return request.post('/auth/register', user, {
      params: { code }
    })
  },
  
  /**
   * 用户登录（支持密码登录和验证码登录）
   * @param {string} email - 邮箱
   * @param {string} password - 密码（可选）
   * @param {string} code - 验证码（可选）
   * @returns {Promise} 登录结果
   */
  login(email, password, code) {
    const params = { email }
    if (password) {
      params.password = password
    } else if (code) {
      params.code = code
    }
    return request.post('/auth/login', null, { params })
  },
  
  /**
   * 发送验证码
   * @param {string} type - 验证码类型（REGISTER-注册，LOGIN-登录，RESET_PASSWORD-重置密码）
   * @param {string} email - 邮箱
   * @returns {Promise} 发送结果
   */
  sendCode(type, email) {
    return request.post('/auth/send-code', null, {
      params: { type, email }
    })
  },
  
  /**
   * 检查邮箱是否已注册
   * @param {string} email - 邮箱
   * @returns {Promise} 检查结果
   */
  checkEmail(email) {
    return request.get('/auth/check-email', {
      params: { email }
    })
  },
  
  /**
   * 重置密码
   * @param {string} email - 邮箱
   * @param {string} code - 验证码
   * @param {string} newPassword - 新密码
   * @returns {Promise} 重置结果
   */
  resetPassword(email, code, newPassword) {
    return request.post('/auth/reset-password', null, {
      params: { email, code, newPassword }
    })
  }
}

