/**
 * 路由守卫
 * 处理路由跳转前的权限验证、登录状态检查等
 */

import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import appConfig from '@/config'

// 白名单：不需要登录就可以访问的路径
const whiteList = ['/login', '/register', '/forgot-password']

/**
 * 设置路由守卫
 * @param {Object} router - Vue Router实例
 */
export function setupRouterGuard(router) {
  // 前置守卫：路由跳转前执行
  router.beforeEach(async (to, from, next) => {
    const token = getToken()
    const userStore = useUserStore()

    // 如果有token
    if (token) {
      // 检查是否需要认证
      if (to.meta.requiresAuth !== false) {
        // 如果用户信息不存在，尝试获取用户信息
        if (!userStore.userInfo) {
          try {
            await userStore.fetchCurrentUser()
          } catch (error) {
            // 获取用户信息失败，清除token，跳转到登录页
            userStore.logout()
            ElMessage.error('登录已过期，请重新登录')
            next({ path: '/login', query: { redirect: to.fullPath } })
            return
          }
        }
        
        // 如果用户是管理员，禁止访问前台页面（非admin路径）
        if (userStore.isAdmin && !to.path.startsWith('/admin')) {
          // 如果访问登录页、注册页或忘记密码页，重定向到管理后台
          if (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') {
            next({ path: '/admin/dashboard' })
            return
          }
          // 其他前台页面，也重定向到管理后台
          ElMessage.warning('管理员无需访问前台页面，如果需要访问前台页面，请注册账号。')
          next({ path: '/admin/dashboard' })
          return
        }
        
        // 如果已登录，访问登录页、注册页或忘记密码页，重定向到首页
        if (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') {
          next({ path: '/' })
          return
        }
        
        // 检查是否需要管理员权限
        if (to.meta.requiresAdmin && !userStore.isAdmin) {
          ElMessage.error('您没有权限访问该页面')
          next({ path: '/home' })
          return
        }
        
        next()
      } else {
        next()
      }
    } else {
      // 没有token
      if (whiteList.includes(to.path)) {
        // 在白名单中，直接访问
        next()
      } else {
        // 不在白名单中，跳转到登录页
        next({ path: '/login', query: { redirect: to.fullPath } })
      }
    }
  })

  // 后置守卫：路由跳转后执行
  router.afterEach((to, from) => {
    // 设置页面标题
    const title = to.meta.title ? `${to.meta.title} - ${appConfig.title}` : appConfig.title
    document.title = title || '校园帮助系统'
    
    // 滚动到顶部（避免页面切换时停留在旧页面的滚动位置）
    window.scrollTo(0, 0)
  })
}

