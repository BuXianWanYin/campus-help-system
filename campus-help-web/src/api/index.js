/**
 * API统一导出入口
 * 按业务模块划分，每个模块创建独立的API文件
 */

// 认证相关 API
export { authApi } from './auth'

// 用户相关 API
export { userApi } from './user'

// 文件相关 API
export { fileApi } from './file'

// 管理员相关 API
export { adminApi } from './admin'

// 系统消息相关 API
export { messageApi, chatApi } from './message'

// 失物招领相关 API
export { lostFoundApi } from './lostFound'

// 学习互助相关 API
export { questionApi, adminQuestionApi } from './question'

// 搜索历史相关 API
export { searchHistoryApi } from './searchHistory'

// 商品相关 API
export { goodsApi } from './goods'

// 订单相关 API
export { orderApi } from './order'

// 收货地址相关 API
export { addressApi } from './address'

// 可以继续添加其他模块的 API
// export { taskApi } from './task'
