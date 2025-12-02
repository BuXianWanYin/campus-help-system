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

// 可以继续添加其他模块的 API

