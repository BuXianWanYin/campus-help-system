import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import appConfig from '@/config'

const whiteList = ['/login', '/register', '/forgot-password'] // 白名单，不需要登录就可以访问

/**
 * 路由守卫
 */
export function setupRouterGuard(router) {
  // 前置守卫
  router.beforeEach(async (to, from, next) => {
    const token = getToken()
    const userStore = useUserStore()

    // 如果有 token
    if (token) {
      // 如果已登录，访问登录页、注册页或忘记密码页，重定向到首页
      if (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') {
        next({ path: '/' })
      } else {
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
      }
    } else {
      // 没有 token
      if (whiteList.includes(to.path)) {
        // 在白名单中，直接访问
        next()
      } else {
        // 不在白名单中，跳转到登录页
        next({ path: '/login', query: { redirect: to.fullPath } })
      }
    }
  })

  // 后置守卫
  router.afterEach((to, from) => {
    // 设置页面标题
    const title = to.meta.title ? `${to.meta.title} - ${appConfig.title}` : appConfig.title
    document.title = title || '校园帮助系统'
  })
}

