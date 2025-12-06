/**
 * API统一导出入口
 * 按业务模块划分，每个模块创建独立的API文件
 */

// 认证相关API
export { authApi } from './auth'

// 用户相关API
export { userApi } from './user'

// 文件相关API
export { fileApi } from './file'

// 管理员相关API
export { adminApi } from './admin'

// 系统消息相关API
export { messageApi, chatApi } from './message'

// 失物招领相关API
export { lostFoundApi } from './lostFound'

// 学习互助相关API
export { questionApi, adminQuestionApi } from './question'

// 搜索历史相关API
export { searchHistoryApi } from './searchHistory'

// 商品相关API
export { goodsApi } from './goods'

// 订单相关API
export { orderApi } from './order'

// 收货地址相关API
export { addressApi } from './address'
