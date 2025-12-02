/**
 * 图片URL处理工具
 */

/**
 * 获取头像完整URL
 * @param {string} avatar - 头像路径
 * @returns {string} 完整的头像URL
 */
export function getAvatarUrl(avatar) {
  if (!avatar) return ''
  
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  
  // 确保路径以 / 开头
  if (!avatar.startsWith('/')) {
    avatar = '/' + avatar
  }
  
  // 返回相对路径，Vite 代理会转发到后端
  return avatar
}

/**
 * 获取图片完整URL（通用）
 * @param {string} imagePath - 图片路径
 * @returns {string} 完整的图片URL
 */
export function getImageUrl(imagePath) {
  return getAvatarUrl(imagePath)
}

