/**
 * 路由配置
 * 定义系统的所有路由规则
 */

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
        component: () => import('../views/Home.vue'),
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
        path: 'lost-found/detail/:id',
        name: 'LostFoundDetail',
        component: () => import('../views/lost-found/Detail.vue'),
        meta: {
          title: '失物详情',
          requiresAuth: true
        }
      },
      {
        path: 'lost-found/publish',
        name: 'LostFoundPublish',
        component: () => import('../views/lost-found/Publish.vue'),
        meta: {
          title: '发布失物',
          requiresAuth: true
        }
      },
      {
        path: 'lost-found/edit/:id',
        name: 'LostFoundEdit',
        component: () => import('../views/lost-found/Edit.vue'),
        meta: {
          title: '编辑失物',
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
        path: 'goods/detail/:id',
        name: 'GoodsDetail',
        component: () => import('../views/goods/Detail.vue'),
        meta: {
          title: '商品详情',
          requiresAuth: true
        }
      },
      {
        path: 'goods/publish',
        name: 'GoodsPublish',
        component: () => import('../views/goods/Publish.vue'),
        meta: {
          title: '发布商品',
          requiresAuth: true
        }
      },
      {
        path: 'goods/edit/:id',
        name: 'GoodsEdit',
        component: () => import('../views/goods/Edit.vue'),
        meta: {
          title: '编辑商品',
          requiresAuth: true
        }
      },
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('../views/order/List.vue'),
        meta: {
          title: '我的交易',
          requiresAuth: true
        }
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('../views/order/Detail.vue'),
        meta: {
          title: '订单详情',
          requiresAuth: true
        }
      },
      {
        path: 'order/pay/:id',
        name: 'OrderPay',
        component: () => import('../views/order/Pay.vue'),
        meta: {
          title: '订单支付',
          requiresAuth: true
        }
      },
      {
        path: 'address/list',
        name: 'AddressList',
        component: () => import('../views/address/List.vue'),
        meta: {
          title: '收货地址',
          requiresAuth: true
        }
      },
      {
        path: 'study/list',
        name: 'StudyList',
        component: () => import('../views/study/List.vue'),
        meta: {
          title: '学习互助',
          requiresAuth: true
        }
      },
      {
        path: 'study/publish',
        name: 'StudyPublish',
        component: () => import('../views/study/Publish.vue'),
        meta: {
          title: '发布问题',
          requiresAuth: true
        }
      },
      {
        path: 'study/detail/:id',
        name: 'StudyDetail',
        component: () => import('../views/study/Detail.vue'),
        meta: {
          title: '问题详情',
          requiresAuth: true
        }
      },
      {
        path: 'study/edit/:id',
        name: 'StudyEdit',
        component: () => import('../views/study/Edit.vue'),
        meta: {
          title: '编辑问题',
          requiresAuth: true
        }
      },
      {
        path: 'study/my-questions',
        name: 'MyQuestions',
        component: () => import('../views/study/MyQuestions.vue'),
        meta: {
          title: '我的问题',
          requiresAuth: true
        }
      },
      {
        path: 'study/my-answers',
        name: 'MyAnswers',
        component: () => import('../views/study/MyAnswers.vue'),
        meta: {
          title: '我的回答',
          requiresAuth: true
        }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/profile/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'user/verification',
        name: 'UserVerification',
        component: () => import('../views/verification/Verification.vue'),
        meta: {
          title: '实名认证',
          requiresAuth: true
        }
      },
      {
        path: 'user/messages',
        name: 'UserMessages',
        component: () => import('../views/message/Messages.vue'),
        meta: {
          title: '消息通知',
          requiresAuth: true
        }
      },
      {
        path: 'user/posts',
        name: 'UserPosts',
        component: () => import('../views/post/Posts.vue'),
        meta: {
          title: '我的发布',
          requiresAuth: true
        }
      },
      {
        path: 'user/chat',
        name: 'UserChat',
        component: () => import('../views/chat/Chat.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: {
      title: '管理后台',
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: {
          title: '数据概览',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'verification',
        name: 'AdminVerification',
        component: () => import('../views/admin/Verification.vue'),
        meta: {
          title: '实名认证审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'lost-found-audit',
        name: 'AdminLostFoundAudit',
        component: () => import('../views/admin/LostFoundAudit.vue'),
        meta: {
          title: '失物招领审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'goods-audit',
        name: 'AdminGoodsAudit',
        component: () => import('../views/admin/GoodsAudit.vue'),
        meta: {
          title: '闲置交易审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'question-audit',
        name: 'AdminQuestionAudit',
        component: () => import('../views/admin/QuestionAudit.vue'),
        meta: {
          title: '学习问题审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('../views/message/Messages.vue'),
        meta: {
          title: '消息通知',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/profile/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'content/lost-found',
        name: 'AdminContentLostFound',
        component: () => import('../views/admin/content/LostFoundManagement.vue'),
        meta: {
          title: '失物招领管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'content/goods',
        name: 'AdminContentGoods',
        component: () => import('../views/admin/content/GoodsManagement.vue'),
        meta: {
          title: '闲置交易管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'content/study',
        name: 'AdminContentStudy',
        component: () => import('../views/admin/content/StudyManagement.vue'),
        meta: {
          title: '学习互助管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'sensitive-word',
        name: 'AdminSensitiveWord',
        component: () => import('../views/admin/SensitiveWord.vue'),
        meta: {
          title: '敏感词配置',
          requiresAuth: true,
          requiresAdmin: true
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

