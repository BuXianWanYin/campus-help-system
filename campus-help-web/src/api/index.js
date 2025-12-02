import request from '@/utils/request'

/**
 * 认证相关 API
 */
export const authApi = {
  // 用户注册
  register(user, code) {
    return request.post('/auth/register', user, {
      params: { code }
    })
  },
  
  // 用户登录（支持密码登录和验证码登录）
  login(email, password, code) {
    const params = { email }
    if (password) {
      params.password = password
    } else if (code) {
      params.code = code
    }
    return request.post('/auth/login', null, { params })
  },
  
  // 发送验证码
  sendCode(type, email) {
    return request.post('/auth/send-code', null, {
      params: { type, email }
    })
  },
  
  // 检查邮箱是否已注册
  checkEmail(email) {
    return request.get('/auth/check-email', {
      params: { email }
    })
  },
  
  // 重置密码
  resetPassword(email, code, newPassword) {
    return request.post('/auth/reset-password', null, {
      params: { email, code, newPassword }
    })
  }
}

/**
 * 用户相关 API
 */
export const userApi = {
  // 获取当前用户信息
  getCurrentUser() {
    return request.get('/user/current')
  },
  
  // 更新当前用户信息
  updateCurrentUser(data) {
    return request.put('/user/current', data)
  },
  
  // 分页查询用户列表（管理员）
  getUserPage(params) {
    return request.get('/user/page', { params })
  },
  
  // 根据ID获取用户信息（管理员）
  getUserById(id) {
    return request.get(`/user/${id}`)
  },
  
  // 提交实名认证
  submitVerification(data) {
    return request.post('/user/verification/submit', data)
  },
  
  // 审核实名认证（管理员）
  auditVerification(data) {
    return request.post('/user/verification/audit', data)
  }
}

/**
 * 文件相关 API
 */
export const fileApi = {
  // 上传文件
  upload(file, module = 'common') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('module', module)
    return request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

/**
 * 管理员相关 API
 */
export const adminApi = {
  // 获取待审核的实名认证列表
  getPendingVerifications(params) {
    return request.get('/admin/verification/pending', { params })
  },
  
  // 审核实名认证（使用userApi中的接口）
  auditVerification(data) {
    return request.post('/user/verification/audit', data)
  },
  
  // 封禁用户
  banUser(data) {
    return request.post('/admin/user/ban', data)
  },
  
  // 解封用户
  unbanUser(userId) {
    return request.post(`/admin/user/unban/${userId}`)
  }
}

// 可以继续添加其他模块的 API

