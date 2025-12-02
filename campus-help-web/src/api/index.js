import request from '@/utils/request'

/**
 * 认证相关 API
 */
export const authApi = {
  // 登录
  login(data) {
    return request.post('/auth/login', data)
  },
  
  // 登出
  logout() {
    return request.post('/auth/logout')
  },
  
  // 获取用户信息
  getUserInfo() {
    return request.get('/auth/user/info')
  },
  
  // 刷新 Token
  refreshToken() {
    return request.post('/auth/refresh')
  },
  
  // 注册
  register(data) {
    return request.post('/auth/register', data)
  }
}

/**
 * 用户相关 API
 */
export const userApi = {
  // 获取用户列表
  getUserList(params) {
    return request.get('/user/list', { params })
  },
  
  // 获取用户详情
  getUserById(id) {
    return request.get(`/user/${id}`)
  },
  
  // 更新用户信息
  updateUser(id, data) {
    return request.put(`/user/${id}`, data)
  },
  
  // 删除用户
  deleteUser(id) {
    return request.delete(`/user/${id}`)
  }
}

// 可以继续添加其他模块的 API

