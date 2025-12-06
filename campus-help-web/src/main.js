/**
 * 应用入口文件
 * 初始化Vue应用，配置路由、状态管理、UI组件库等
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
import '@/assets/styles/global.css'
import { clickOutside } from '@/utils/directives'

const app = createApp(App)
const pinia = createPinia()

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册自定义指令
app.directive('click-outside', clickOutside)

// 使用Pinia状态管理
app.use(pinia)
// 使用Vue Router路由
app.use(router)
// 使用Element Plus UI组件库
app.use(ElementPlus, {
  locale: zhCn
})

// 挂载应用到DOM
app.mount('#app')

