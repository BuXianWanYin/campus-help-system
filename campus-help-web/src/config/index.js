/**
 * 全局配置
 */

export default {
  // 应用标题
  title: import.meta.env.VITE_APP_TITLE || '校园帮助系统',
  
  // API 基础路径
  baseURL: import.meta.env.VITE_APP_BASE_API,
  
  // 请求超时时间
  timeout: parseInt(import.meta.env.VITE_APP_TIMEOUT) || 30000,
  
  // 是否显示页面加载动画
  showLoading: true,
  
  // Element Plus 组件大小
  elementSize: 'default',
  
  // 分页配置
  pagination: {
    pageSize: 10,
    pageSizes: [10, 20, 50, 100]
  }
}

