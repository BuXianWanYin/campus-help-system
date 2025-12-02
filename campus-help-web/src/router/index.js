import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuard } from './guard'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue'),
    meta: {
      title: '忘记密码',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/home',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'home',
        name: 'Home',
        // 不指定 component，MainLayout 会直接显示首页内容
        meta: {
          title: '首页',
          requiresAuth: true
        }
      },
      {
        path: 'lost-found/list',
        name: 'LostFoundList',
        component: () => import('../views/lost-found/List.vue'),
        meta: {
          title: '失物招领',
          requiresAuth: true
        }
      },
      {
        path: 'goods/list',
        name: 'GoodsList',
        component: () => import('../views/goods/List.vue'),
        meta: {
          title: '闲置交易',
          requiresAuth: true
        }
      },
      {
        path: 'task/list',
        name: 'TaskList',
        component: () => import('../views/task/List.vue'),
        meta: {
          title: '跑腿服务',
          requiresAuth: true
        }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/user/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'user/verification',
        name: 'UserVerification',
        component: () => import('../views/user/Verification.vue'),
        meta: {
          title: '实名认证',
          requiresAuth: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 设置路由守卫
setupRouterGuard(router)

export default router

