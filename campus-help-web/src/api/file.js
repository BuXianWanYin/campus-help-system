import request from '@/utils/request'

/**
 * 文件相关 API
 */
export const fileApi = {
  /**
   * 上传文件
   * @param {File} file 文件对象
   * @param {String} module 模块名称（common-通用，lost-found-失物招领，goods-闲置交易，task-跑腿服务等）
   * @returns {Promise} 上传结果
   */
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

