import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuard } from './guard'
import BasicLayout from '../layouts/BasicLayout.vue'

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
    path: '/',
    component: BasicLayout,
    redirect: '/home',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: {
          title: '首页',
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

