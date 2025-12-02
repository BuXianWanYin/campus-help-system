<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="main-header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo-wrapper">
            <div class="logo-icon">
              <el-icon :size="32"><Box /></el-icon>
            </div>
            <h1 class="logo-text">{{ appConfig.title || '校园帮系统' }}</h1>
          </div>
        </div>
        
        <div class="header-center">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            class="main-menu"
            :collapse="false"
            @select="handleMenuSelect"
          >
            <el-menu-item index="home">首页</el-menu-item>
            <el-menu-item index="lost-found">失物招领</el-menu-item>
            <el-menu-item index="goods">闲置交易</el-menu-item>
            <el-menu-item index="task">跑腿服务</el-menu-item>
          </el-menu>
        </div>
        
        <div class="header-right">
          <div class="search-wrapper">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索..."
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <el-button type="primary" class="publish-btn" @click="handlePublish">
            发布信息
          </el-button>
          
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="getAvatarUrl(userStore.userInfo?.avatar)">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="nickname">{{ userStore.nickname || userStore.email }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- 管理员只显示管理后台和退出登录 -->
                <template v-if="userStore.isAdmin">
                  <el-dropdown-item command="admin">
                    <el-icon><Tools /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </template>
                <!-- 普通用户显示完整菜单 -->
                <template v-else>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="my-posts">
                    <el-icon><Document /></el-icon>
                    我的发布
                  </el-dropdown-item>
                  <el-dropdown-item command="messages">
                    <el-icon><Message /></el-icon>
                    消息通知
                    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge" />
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-button class="mobile-menu-btn" @click="showMobileMenu = !showMobileMenu">
            <el-icon><Menu /></el-icon>
          </el-button>
        </div>
      </div>
    </header>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 首页内容（仅在首页显示） -->
      <template v-if="isHomePage">
        <!-- 欢迎横幅 -->
        <section class="welcome-banner">
          <div class="banner-content">
            <div class="banner-left">
              <h2 class="banner-title">欢迎使用{{ appConfig.title || '校园帮系统' }}</h2>
              <p class="banner-desc">一站式校园互助平台，让校园生活更便捷</p>
              <div class="banner-actions">
                <el-button class="banner-btn-primary" size="large" @click="handlePublish">立即发布</el-button>
                <el-button class="banner-btn-secondary" size="large" @click="goToRoute('/about')">了解更多</el-button>
              </div>
            </div>
            <div class="banner-stats">
              <div class="stat-item">
                <div class="stat-value">1200+</div>
                <div class="stat-label">失物招领</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">3500+</div>
                <div class="stat-label">闲置交易</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">800+</div>
                <div class="stat-label">跑腿服务</div>
              </div>
            </div>
          </div>
        </section>

     

      <!-- 失物招领模块 -->
      <section class="lost-found-section">
        <div class="section-header">
          <h2 class="section-title">失物招领</h2>
          <div class="section-actions">
            <el-button type="primary" @click="handlePublish">发布信息</el-button>
            <el-link type="primary" :underline="false" @click="goToRoute('/lost-found/list')">查看更多</el-link>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="类型：">
              <el-select v-model="lostFoundTab" placeholder="全部" style="width: 120px" clearable @change="handleTypeChange">
                <el-option label="全部" value="" />
                <el-option label="失物" value="lost" />
                <el-option label="招领" value="found" />
              </el-select>
            </el-form-item>
            <el-form-item label="分类：">
              <el-select v-model="lostFoundFilters.category" placeholder="全部" style="width: 120px" clearable @change="handleFilter">
                <el-option label="全部" value="" />
                <el-option label="证件类" value="证件类" />
                <el-option label="电子产品" value="电子产品" />
                <el-option label="生活用品" value="生活用品" />
                <el-option label="书籍" value="书籍" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="地点：">
              <el-input 
                v-model="lostFoundFilters.location" 
                placeholder="输入地点" 
                style="width: 150px" 
                clearable 
                class="location-input"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 失物列表 -->
        <div class="card-grid" v-loading="lostFoundLoading">
          <div v-for="item in lostFoundList" :key="item.id" class="lost-found-card" @click="goToDetail('lost-found', item.id)">
            <div class="card-image-wrapper">
              <img :src="getFirstImage(item.images)" :alt="item.title" class="card-image" />
              <span class="card-badge" :class="item.type === 'LOST' ? 'badge-red' : 'badge-green'">
                {{ item.type === 'LOST' ? '失物' : '招领' }}
              </span>
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ item.title }}</h3>
              <p class="card-desc">{{ item.description }}</p>
              <div class="card-meta">
                <span class="meta-item">
                  <el-icon><Location /></el-icon>
                  {{ item.lostLocation }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(item.createTime) }}
                </span>
              </div>
              <div class="card-footer">
                <div class="card-user">
                  <el-avatar :size="24" :src="getAvatarUrl(item.userAvatar)">{{ item.userName?.charAt(0) || 'U' }}</el-avatar>
                  <span>{{ item.userName || '未知用户' }}</span>
                </div>
                <el-button 
                  v-if="item.userId && item.userId !== userStore.userInfo?.id" 
                  type="primary" 
                  size="small" 
                  text 
                  @click.stop="handleContact(item)">
                  联系TA
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-if="!lostFoundLoading && lostFoundList.length === 0" description="暂无失物信息" />

      </section>

      <!-- 闲置交易模块 -->
      <section class="goods-section">
        <div class="section-header">
          <h2 class="section-title">闲置交易</h2>
          <div class="section-actions">
            <el-button type="primary" @click="handlePublish">发布闲置</el-button>
            <el-link type="primary" :underline="false" @click="goToRoute('/goods/list')">查看更多</el-link>
          </div>
        </div>

        <!-- 分类导航 -->
        <div class="category-nav">
          <div v-for="category in goodsCategories" :key="category.id" class="category-item" :class="{ active: goodsActiveCategory === category.id }" @click="goodsActiveCategory = category.id">
            <el-icon :size="20"><component :is="category.icon" /></el-icon>
            <span>{{ category.name }}</span>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="goods-grid">
          <div v-for="item in goodsList" :key="item.id" class="goods-card" @click="goToDetail('goods', item.id)">
            <div class="card-image-wrapper">
              <img :src="item.image" :alt="item.title" class="card-image" />
              <span v-if="item.badge" class="card-badge" :class="item.badgeClass">{{ item.badge }}</span>
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ item.title }}</h3>
              <div class="card-price-row">
                <span class="card-price">¥{{ item.price }}</span>
                <span class="card-views">
                  <el-icon><View /></el-icon>
                  {{ item.views }}
                </span>
              </div>
              <div class="card-footer">
                <div class="card-user">
                  <el-avatar :size="20" :src="getAvatarUrl(item.userAvatar)">{{ item.userName.charAt(0) }}</el-avatar>
                  <span>{{ item.userName }}</span>
                </div>
                <span class="card-time">{{ item.time }}</span>
              </div>
            </div>
          </div>
        </div>

      
      </section>

      <!-- 跑腿服务模块 -->
      <section class="task-section">
        <div class="section-header">
          <h2 class="section-title">跑腿服务</h2>
          <div class="section-actions">
            <el-button type="primary" @click="handlePublish">发布任务</el-button>
            <el-link type="primary" :underline="false" @click="goToRoute('/task/list')">查看更多</el-link>
          </div>
        </div>

        <!-- 任务分类 -->
        <div class="task-categories">
          <el-tag
            v-for="category in taskCategories"
            :key="category.id"
            :type="taskActiveCategory === category.id ? 'primary' : 'info'"
            class="task-category-tag"
            @click="taskActiveCategory = category.id"
          >
            {{ category.name }}
          </el-tag>
        </div>

        <!-- 任务列表 -->
        <div class="task-list">
          <div v-for="task in taskList" :key="task.id" class="task-card">
            <div class="task-icon" :class="task.colorClass">
              <el-icon><component :is="task.icon" /></el-icon>
            </div>
            <div class="task-content">
              <div class="task-header">
                <h3 class="task-title">{{ task.title }}</h3>
                <span class="task-reward">¥{{ task.reward }}</span>
              </div>
              <p class="task-desc">{{ task.description }}</p>
              <div class="task-meta">
                <span class="meta-item">
                  <el-icon><Location /></el-icon>
                  {{ task.route }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ task.deadline }}
                </span>
              </div>
              <div class="task-footer">
                <div class="task-user">
                  <el-avatar :size="24" :src="getAvatarUrl(task.userAvatar)">{{ task.userName.charAt(0) }}</el-avatar>
                  <span>{{ task.userName }}</span>
                </div>
                <div class="task-actions">
                  <el-button type="primary" size="small" text @click.stop="goToDetail('task', task.id)">查看详情</el-button>
                  <el-button type="warning" size="small" @click.stop="handleAcceptTask(task)">立即接取</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>


      </template>
      
      <!-- 路由视图（其他页面） -->
      <router-view v-if="!isHomePage" />
    </main>
    
    <!-- 底部导航栏（移动端） -->
    <nav class="bottom-nav" v-if="isMobile">
      <div class="nav-item" :class="{ active: activeMenu === 'home' }" @click="goToRoute('/home')">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="nav-item" :class="{ active: activeMenu === 'lost-found' }" @click="goToRoute('/lost-found/list')">
        <el-icon><Search /></el-icon>
        <span>失物招领</span>
      </div>
      <div class="nav-item publish-nav-item" @click="handlePublish">
        <el-icon><Plus /></el-icon>
        <span>发布</span>
      </div>
      <div class="nav-item" :class="{ active: activeMenu === 'goods' }" @click="goToRoute('/goods/list')">
        <el-icon><ShoppingBag /></el-icon>
        <span>闲置</span>
      </div>
      <div class="nav-item" :class="{ active: activeMenu === 'task' }" @click="goToRoute('/task/list')">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </nav>
    
    <!-- 消息通知面板 -->
    <div class="notification-panel" v-if="showNotificationPanel">
      <div class="notification-header">
        <h3>消息通知</h3>
        <el-button text @click="markAllAsRead">全部标记为已读</el-button>
      </div>
      <div class="notification-list" v-loading="loadingNotifications">
        <div v-if="notifications.length === 0 && !loadingNotifications" class="notification-empty">
          <p>暂无消息</p>
        </div>
        <!-- 消息列表内容 -->
        <div v-for="item in notifications" :key="item.id" class="notification-item" @click="handleNotificationClick(item)">
          <div class="notification-icon" :class="item.type">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="notification-content">
            <p class="notification-title">{{ item.title }}</p>
            <p class="notification-desc">{{ item.description }}</p>
            <p class="notification-time">{{ item.time }}</p>
          </div>
          <div v-if="!item.read" class="notification-dot"></div>
        </div>
      </div>
      <div class="notification-footer">
        <el-button text type="primary" @click="goToMessages">查看全部消息</el-button>
      </div>
    </div>
    
    <!-- 消息通知按钮 -->
    <div class="notification-btn-wrapper">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-button
          circle
          type="primary"
          class="notification-btn"
          @click="showNotificationPanel = !showNotificationPanel"
        >
          <el-icon><Bell /></el-icon>
        </el-button>
      </el-badge>
    </div>
    
    <!-- 返回顶部按钮 -->
    <el-backtop :right="isMobile ? 20 : 100" :bottom="isMobile ? 80 : 100" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick, markRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { debounce } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, ArrowDown, User, Setting, SwitchButton, Menu, Tools,
  HomeFilled, Plus, ShoppingBag, Document, Message, Bell, ChatDotRound, Check, Star,
  Location, Clock, View, Star as LightbulbIcon, Link, Box as ComputerIcon, Document as NotebookIcon, ShoppingBag as TShirtIcon, Star as BasketballIcon,
  Message as HeadsetIcon, Edit as EditPenIcon, More, ShoppingCart, ShoppingBag as ForkSpoonIcon, Link as ConnectionIcon, User as UsersIcon, View as TrendChartsIcon,
  Close, ArrowUp, ArrowDown as ArrowDownIcon, Box, InfoFilled
} from '@element-plus/icons-vue'
import appConfig from '@/config'
import { getAvatarUrl } from '@/utils/image'
import { messageApi, lostFoundApi } from '@/api'
import wsManager from '@/utils/websocket'
import { getToken } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 判断是否是首页（排除重定向过程中的误判）
const isHomePage = computed(() => {
  const path = route.path
  // 判断是否为首页路径
  return path === '/home' || path === '/'
})

const searchKeyword = ref('')
const activeMenu = ref('') // 初始为空，避免默认激活首页
const showMobileMenu = ref(false)
const showNotificationPanel = ref(false)
const unreadCount = ref(0)
const notifications = ref([])
const isMobile = ref(false)
const loadingNotifications = ref(false)

// 功能模块（使用 markRaw 避免图标组件被响应式化）
const featureModules = ref([
  { id: 1, title: '失物招领', icon: markRaw(Search), path: '/lost-found/list', colorClass: 'icon-blue', description: '发布失物信息，寻找丢失物品' },
  { id: 2, title: '闲置交易', icon: markRaw(ShoppingBag), path: '/goods/list', colorClass: 'icon-green', description: '买卖闲置物品，让资源再利用' },
  { id: 3, title: '跑腿服务', icon: markRaw(TrendChartsIcon), path: '/task/list', colorClass: 'icon-orange', description: '发布或接取任务，互帮互助' }
])

// 失物招领
const lostFoundTab = ref('')
const lostFoundFilters = ref({ category: '', location: '' })
const lostFoundList = ref([])
const lostFoundLoading = ref(false)

// 闲置交易（使用 markRaw 避免图标组件被响应式化）
const goodsActiveCategory = ref(1)
const goodsCategories = ref([
  { id: 1, name: '数码产品', icon: markRaw(ComputerIcon) },
  { id: 2, name: '图书教材', icon: markRaw(NotebookIcon) },
  { id: 3, name: '服装鞋包', icon: markRaw(TShirtIcon) },
  { id: 4, name: '生活用品', icon: markRaw(HomeFilled) },
  { id: 5, name: '运动健身', icon: markRaw(BasketballIcon) },
  { id: 6, name: '乐器', icon: markRaw(HeadsetIcon) },
  { id: 7, name: '文创用品', icon: markRaw(EditPenIcon) },
  { id: 8, name: '更多分类', icon: markRaw(More) }
])
const goodsList = ref([
  { id: 1, title: 'iPhone 13 128GB 午夜色 9成新', price: 3999, views: 128, image: 'https://via.placeholder.com/300x200?text=iPhone', userAvatar: '', userName: '陈同学', time: '1小时前', badge: '新品', badgeClass: 'badge-red' },
  { id: 2, title: 'MacBook Pro 2020款 13英寸 8GB内存', price: 6800, views: 86, image: 'https://via.placeholder.com/300x200?text=MacBook', userAvatar: '', userName: '林同学', time: '3小时前' },
  { id: 3, title: '大学物理教材 上下册 几乎全新', price: 80, views: 42, image: 'https://via.placeholder.com/300x200?text=教材', userAvatar: '', userName: '黄同学', time: '昨天' },
  { id: 4, title: '耐克 Air Jordan 1 高帮运动鞋 42码', price: 450, views: 215, image: 'https://via.placeholder.com/300x200?text=运动鞋', userAvatar: '', userName: '刘同学', time: '2天前', badge: '热门', badgeClass: 'badge-orange' }
])
const negotiationPrice = ref({ current: 3999, offer: 3800, min: 3500 })

// 跑腿服务
const taskActiveCategory = ref('all')
const taskCategories = ref([
  { id: 'all', name: '全部' },
  { id: 'express', name: '取快递' },
  { id: 'food', name: '买饭' },
  { id: 'file', name: '送文件' },
  { id: 'queue', name: '代排队' },
  { id: 'other', name: '其他' }
])
const taskList = ref([
  { id: 1, title: '取快递 - 顺丰快递', description: '快递单号：SF1234567890，放在南门快递点', route: '南门快递点 → 东区宿舍', deadline: '2025-07-20 18:00前', reward: 10, icon: markRaw(ShoppingCart), colorClass: 'icon-orange', userAvatar: '', userName: '周同学' },
  { id: 2, title: '买饭 - 西区食堂', description: '一份红烧肉盖浇饭，不要辣，加一个煎蛋', route: '西区食堂 → 教学楼C301', deadline: '2025-07-20 12:00前', reward: 8, icon: markRaw(ForkSpoonIcon), colorClass: 'icon-orange', userAvatar: '', userName: '吴同学' },
  { id: 3, title: '送文件 - 教务处', description: '将成绩单送到教务处张老师办公室，需要签字带回', route: '行政楼 → 教务处', deadline: '2025-07-20 16:00前', reward: 15, icon: markRaw(Document), colorClass: 'icon-orange', userAvatar: '', userName: '郑同学' }
])
const taskForm = ref({
  type: '',
  description: '',
  reward: 10,
  startLocation: '',
  endLocation: '',
  deadline: null
})


// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
  const routeMap = {
    'home': '/home',
    'lost-found': '/lost-found/list',
    'goods': '/goods/list',
    'task': '/task/list'
  }
  if (routeMap[index]) {
    router.push(routeMap[index])
  }
}

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { keyword: searchKeyword.value }
    })
  }
}

// 处理发布
const handlePublish = () => {
  // 根据当前页面或上下文跳转到对应的发布页面
  // 目前默认跳转到失物发布页面
  router.push('/lost-found/publish')
}

// 处理用户菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'my-posts':
      router.push('/user/posts')
      break
    case 'messages':
      showNotificationPanel.value = true
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'admin':
      // 在新标签页打开管理后台
      window.open('/admin/dashboard', '_blank')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('退出成功')
        router.push('/login')
      }).catch(() => {})
      break
  }
}

// 跳转路由
const goToRoute = (path) => {
  router.push(path)
}

// 标记全部为已读
const markAllAsRead = async () => {
  if (!userStore.isLoggedIn) return
  try {
    await messageApi.markAllAsRead()
    ElMessage.success('已全部标记为已读')
    notifications.value.forEach(item => {
      item.read = true
    })
    unreadCount.value = 0
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 前往消息页面
const goToMessages = () => {
  router.push('/user/messages')
  showNotificationPanel.value = false
}

// 点击通知项
const handleNotificationClick = async (item) => {
  if (!item.read) {
    try {
      await messageApi.markAsRead(item.id)
      item.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  // 跳转到消息详情或相关页面
  goToMessages()
}

// 获取未读消息数量
const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const response = await messageApi.getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 获取最新消息列表（用于通知面板）
const fetchRecentMessages = async () => {
  if (!userStore.isLoggedIn) return
  loadingNotifications.value = true
  try {
    const response = await messageApi.getMessagePage({
      current: 1,
      size: 5
    })
    if (response.code === 200) {
      const records = response.data.records || []
      notifications.value = records.map(msg => ({
        id: msg.id,
        title: msg.title,
        description: msg.content,
        time: formatMessageTime(msg.createTime),
        read: msg.isRead === 1,
        type: getMessageTypeClass(msg.type),
        icon: getMessageIcon(msg.type)
      }))
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
  } finally {
    loadingNotifications.value = false
  }
}

// 格式化消息时间
const formatMessageTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit'
    })
  }
}

// 获取消息类型样式类
const getMessageTypeClass = (type) => {
  const typeMap = {
    'VERIFICATION_APPROVED': 'green',
    'VERIFICATION_REJECTED': 'red',
    'ORDER_STATUS': 'blue',
    'TASK_STATUS': 'orange',
    'ANNOUNCEMENT': 'blue'
  }
  return typeMap[type] || 'blue'
}

// 获取消息图标（使用 markRaw 避免图标组件被响应式化）
const getMessageIcon = (type) => {
  const iconMap = {
    'VERIFICATION_APPROVED': markRaw(Check),
    'VERIFICATION_REJECTED': markRaw(Close),
    'ORDER_STATUS': markRaw(ShoppingBag),
    'TASK_STATUS': markRaw(TrendChartsIcon),
    'ANNOUNCEMENT': markRaw(Bell)
  }
  return iconMap[type] || markRaw(InfoFilled)
}

// WebSocket消息处理
const handleWebSocketMessage = (message) => {
  // STOMP发送的是SystemMessage对象，直接处理
  if (message && message.id) {
    // 收到新的系统消息
    ElMessage.info('您有新的消息')
    // 刷新消息列表和未读数量
    fetchRecentMessages()
    fetchUnreadCount()
  }
}

// 更新当前激活菜单
const updateActiveMenu = () => {
  const path = route.path
  // 等待路由完全加载后再更新，避免重定向过程中的误判
  if (!route.name) {
    activeMenu.value = ''
    return
  }
  
  if (path.startsWith('/lost-found')) {
    activeMenu.value = 'lost-found'
  } else if (path.startsWith('/goods')) {
    activeMenu.value = 'goods'
  } else if (path.startsWith('/task')) {
    activeMenu.value = 'task'
  } else if (path.startsWith('/user')) {
    // 个人中心相关页面不激活任何菜单项
    activeMenu.value = ''
  } else if (path === '/home' || (path === '/' && route.name === 'Home')) {
    activeMenu.value = 'home'
  } else {
    // 其他未匹配的路径，默认不激活任何菜单
    activeMenu.value = ''
  }
}

// 监听路由变化，实时更新激活菜单和加载数据
watch(() => route.path, (newPath) => {
  updateActiveMenu()
  // 如果是首页，加载失物招领列表
  if (newPath === '/home' || newPath === '/') {
    nextTick(() => {
      fetchLostFoundList()
    })
  }
}, { immediate: true })

// 跳转到功能模块
const goToModule = (path) => {
  router.push(path)
}

// 获取失物招领列表（首页显示）
const fetchLostFoundList = async () => {
  // 只在首页时获取数据
  const path = route.path
  if (path !== '/home' && path !== '/') {
    return
  }
  
  lostFoundLoading.value = true
  try {
    const params = {
      pageNum: 1,
      pageSize: 6,
      type: lostFoundTab.value === 'lost' ? 'LOST' : lostFoundTab.value === 'found' ? 'FOUND' : undefined,
      category: lostFoundFilters.value.category || undefined,
      location: lostFoundFilters.value.location || undefined,
      // 不传status，让后端使用默认筛选（PENDING_CLAIM, CLAIMING, CLAIMED）
      sortBy: 'latest'
    }
    
    const response = await lostFoundApi.getList(params)
    if (response.code === 200) {
      const pageData = response.data
      console.log('首页失物招领数据:', pageData) // 调试日志
      
      // MyBatis-Plus的Page对象可能直接是数组，或者有records字段
      let records = []
      if (Array.isArray(pageData)) {
        records = pageData
      } else if (pageData.records) {
        records = pageData.records
      } else if (pageData.list) {
        records = pageData.list
      }
      
      lostFoundList.value = records.map(item => {
        let images = []
        if (item.images) {
          try {
            images = typeof item.images === 'string' ? JSON.parse(item.images) : item.images
          } catch (e) {
            console.error('解析图片失败:', e)
            images = []
          }
        }
        return {
          ...item,
          images: images,
          // 后端可能返回user对象，也可能只有userId
          userAvatar: item.user?.avatar || null,
          userName: item.user?.nickname || `用户${item.userId}` || '未知用户',
          userId: item.userId // 保存userId用于判断是否为发布者
        }
      })
      
      console.log('处理后的失物列表:', lostFoundList.value) // 调试日志
    } else {
      console.error('获取失物招领列表失败，响应码:', response.code, response.message)
    }
  } catch (error) {
    console.error('获取失物招领列表失败:', error)
  } finally {
    lostFoundLoading.value = false
  }
}

/**
 * 获取第一张图片
 */
const getFirstImage = (images) => {
  if (Array.isArray(images) && images.length > 0) {
    return getAvatarUrl(images[0])
  }
  return 'https://via.placeholder.com/300x200?text=暂无图片'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor(diff / (1000 * 60))
  
  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}

/**
 * 处理类型切换
 */
const handleTypeChange = () => {
  fetchLostFoundList()
}

// 处理筛选
const handleFilter = () => {
  fetchLostFoundList()
}

// 防抖处理地点输入框变化
const debouncedFilter = debounce(() => {
  fetchLostFoundList()
}, 500)

// 监听地点输入框变化，使用防抖
watch(() => lostFoundFilters.value.location, () => {
  debouncedFilter()
})

// 联系用户
const handleContact = (item) => {
  ElMessage.info(`联系 ${item.userName}`)
}

// 跳转详情
const goToDetail = (type, id) => {
  router.push(`/${type}/detail/${id}`)
}

// 价格协商
const handleCancelNegotiation = () => {
  negotiationPrice.value.offer = negotiationPrice.value.min
}

const handleSendOffer = () => {
  ElMessage.success('报价已发送')
}

// 接取任务
const handleAcceptTask = (task) => {
  ElMessage.success(`已接取任务：${task.title}`)
}

// 任务发布
const handlePreviewTask = () => {
  ElMessage.info('预览功能开发中')
}

const handleSubmitTask = () => {
  ElMessage.success('任务发布成功')
  // 重置表单
  taskForm.value = {
    type: '',
    description: '',
    reward: 10,
    startLocation: '',
    endLocation: '',
    deadline: null
  }
}


// 禁用菜单自动折叠功能
const disableMenuCollapse = () => {
  nextTick(() => {
    const menuEl = document.querySelector('.main-menu')
    if (menuEl) {
      // 强制显示所有菜单项
      const menuItems = menuEl.querySelectorAll('.el-menu-item')
      menuItems.forEach(item => {
        item.style.display = 'inline-flex'
        item.style.visibility = 'visible'
        item.style.opacity = '1'
      })
      // 隐藏"更多"按钮（子菜单）
      const submenus = menuEl.querySelectorAll('.el-submenu')
      submenus.forEach(submenu => {
        submenu.style.display = 'none'
      })
    }
  })
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  
  // 禁用菜单自动折叠
  disableMenuCollapse()
  // 监听窗口大小变化，重新禁用折叠
  window.addEventListener('resize', () => {
    setTimeout(() => {
      disableMenuCollapse()
    }, 100)
  })
  
  // 如果是首页，加载失物招领列表
  const path = route.path
  if (path === '/home' || path === '/') {
    fetchLostFoundList()
  }
  
  // 如果用户已登录，加载消息和连接WebSocket
  if (userStore.isLoggedIn) {
    await fetchUnreadCount()
    await fetchRecentMessages()
    
    // 连接WebSocket
    const token = getToken()
    if (token) {
      wsManager.connect(token)
      wsManager.addMessageHandler(handleWebSocketMessage)
    }
  }
  
  // 监听通知面板显示，自动刷新消息
  watch(showNotificationPanel, (show) => {
    if (show && userStore.isLoggedIn) {
      fetchRecentMessages()
    }
  })
})


onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  // 移除WebSocket消息处理器（但不关闭连接，因为可能其他页面也在使用）
  wsManager.removeMessageHandler(handleWebSocketMessage)
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background-color: var(--color-bg-primary);
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.main-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: var(--color-bg-white);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.header-container {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 var(--content-padding);
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background-color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-bg-white);
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
}

.logo-text {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: var(--color-primary);
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 40px;
  min-width: 600px; /* 增加最小宽度，确保菜单项有足够空间 */
  overflow: visible; /* 确保菜单项可见 */
}

.main-menu {
  border-bottom: none;
  /* 禁用菜单自动折叠 */
  overflow: visible !important;
  width: 100% !important;
}

/* 隐藏"更多"下拉菜单按钮和所有子菜单 */
:deep(.el-menu--horizontal .el-submenu) {
  display: none !important;
}

:deep(.el-menu--horizontal .el-submenu__title) {
  display: none !important;
}

/* 强制显示所有菜单项 */
:deep(.el-menu--horizontal .el-menu-item) {
  height: var(--header-height);
  line-height: var(--header-height);
  color: var(--color-text-regular);
  font-size: 15px;
  padding: 0 var(--spacing-2xl);
  white-space: nowrap;
  display: inline-flex !important;
  visibility: visible !important;
  opacity: 1 !important;
  min-width: 80px;
  position: relative !important;
}

/* 确保菜单容器不隐藏溢出内容 */
:deep(.el-menu--horizontal) {
  overflow: visible !important;
  width: 100% !important;
}

/* 确保跑腿服务菜单项始终显示 */
:deep(.el-menu--horizontal .el-menu-item[index="task"]) {
  display: inline-flex !important;
  visibility: visible !important;
  opacity: 1 !important;
}

:deep(.el-menu--horizontal .el-menu-item:hover) {
  color: var(--color-primary);
  background-color: transparent;
}

:deep(.el-menu--horizontal .el-menu-item.is-active) {
  color: var(--color-primary);
  border-bottom: 2px solid var(--color-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-wrapper {
  width: 200px;
}

.search-input {
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-xl);
  box-shadow: 0 0 0 1px var(--color-border) inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.publish-btn {
  border-radius: var(--radius-xl);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: var(--color-bg-primary);
}

.nickname {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.mobile-menu-btn {
  display: none;
}

.message-badge {
  margin-left: 8px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  max-width: var(--content-max-width);
  margin: 0 auto;
  width: 100%;
  padding: var(--content-padding);
  min-height: calc(100vh - var(--header-height));
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, var(--color-primary) 0%, #1976D2 100%);
  border-radius: var(--radius-lg);
  padding: var(--spacing-3xl);
  margin-bottom: var(--spacing-3xl);
  color: #FFFFFF !important;
  box-shadow: 0 4px 16px rgba(30, 136, 229, 0.2);
}

.welcome-banner * {
  color: inherit;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}

.banner-left {
  flex: 1;
  min-width: 300px;
}

.banner-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 var(--spacing-md) 0;
  color: var(--color-bg-white);
}

.banner-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 var(--spacing-2xl) 0;
}

.banner-actions {
  display: flex;
  gap: var(--spacing-md);
}

.banner-actions {
  display: flex;
  gap: var(--spacing-md);
}

.banner-btn-primary {
  background-color: #FFFFFF !important;
  color: var(--color-primary) !important;
  border: none !important;
  font-weight: 500;
}

.banner-btn-primary:hover {
  background-color: rgba(255, 255, 255, 0.9) !important;
  color: var(--color-primary-hover) !important;
}

.banner-btn-secondary {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #FFFFFF !important;
  border: 1px solid #FFFFFF !important;
  font-weight: 500;
}

.banner-btn-secondary:hover {
  background-color: rgba(255, 255, 255, 0.25) !important;
  border-color: #FFFFFF !important;
  color: #FFFFFF !important;
}

.banner-stats {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg) var(--content-padding);
  text-align: center;
  min-width: 100px;
  color: #FFFFFF !important;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
  color: #FFFFFF !important;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  color: #FFFFFF !important;
}

/* 功能模块导航 */
.feature-modules {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.module-card {
  background: linear-gradient(135deg, var(--color-bg-white) 0%, var(--color-bg-primary) 100%);
  border-radius: var(--radius-lg);
  padding: var(--spacing-3xl) var(--spacing-2xl);
  display: flex;
  align-items: center;
  gap: var(--content-padding);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.module-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.module-icon.icon-blue {
  background-color: var(--color-primary);
  color: var(--color-bg-white);
}

.module-icon.icon-green {
  background-color: var(--color-success);
  color: var(--color-bg-white);
}

.module-icon.icon-orange {
  background-color: var(--color-secondary);
  color: var(--color-bg-white);
}

.module-content {
  flex: 1;
  text-align: left;
}

.module-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.module-desc {
  font-size: 14px;
  color: var(--color-text-regular);
  margin: 0;
  line-height: 1.5;
}

/* 失物招领模块 */
.lost-found-section {
  margin-bottom: 48px;
}

.tab-buttons {
  display: flex;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.filter-bar {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--content-padding);
  margin-bottom: var(--content-padding);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.location-input :deep(.el-input__wrapper) {
  border-radius: 4px !important;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.lost-found-card,
.goods-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.lost-found-card:hover,
.goods-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  flex-shrink: 0;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-badge {
  position: absolute;
  top: var(--spacing-sm);
  left: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--color-bg-white);
  font-weight: 500;
}

.badge-red {
  background-color: var(--color-danger);
}

.badge-green {
  background-color: var(--color-success);
}

.badge-orange {
  background-color: var(--color-secondary);
}

.card-content {
  padding: 12px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 6px 0;
  line-height: 1.4;
  min-height: 42px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-desc {
  font-size: 13px;
  color: var(--color-text-regular);
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.card-meta {
  display: flex;
  gap: var(--spacing-lg);
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
  margin-top: auto;
}

.card-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 12px;
  color: var(--color-text-regular);
}

.card-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-price {
  font-size: 18px;
  font-weight: bold;
  color: var(--color-danger);
}

.card-views {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 12px;
  color: var(--color-text-secondary);
}

.card-time {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* AI智能匹配 */
.ai-match-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--content-padding);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  margin-top: var(--spacing-2xl);
}

.ai-match-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.ai-icon {
  width: 32px;
  height: 32px;
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-match-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.ai-match-content {
  background-color: var(--color-primary-lighter);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.ai-match-desc {
  font-size: 14px;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-md) 0;
}

.ai-match-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  border: 1px solid var(--color-border);
}

.match-icon {
  width: 32px;
  height: 32px;
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.match-content {
  flex: 1;
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.match-header h4 {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.match-score {
  font-size: 12px;
  color: var(--color-success);
  font-weight: 500;
}

.match-info {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* 闲置交易模块 */
.goods-section {
  margin-bottom: 48px;
}

.category-nav {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--content-padding);
  margin-bottom: var(--content-padding);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  display: flex;
  gap: var(--spacing-2xl);
  overflow-x: auto;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-text-regular);
  cursor: pointer;
  transition: all 0.3s;
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  min-width: 80px;
}

.category-item:hover {
  color: var(--color-primary);
  background-color: var(--color-bg-primary);
}

.category-item.active {
  color: var(--color-primary);
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 价格协商 */
.price-negotiation-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--content-padding);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  margin-top: var(--spacing-2xl);
}

.negotiation-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.negotiation-icon {
  width: 32px;
  height: 32px;
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.negotiation-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.negotiation-content {
  background-color: rgba(76, 175, 80, 0.05);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.price-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
  font-size: 14px;
  color: var(--color-text-primary);
}

.price-range {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: var(--spacing-sm);
}

.negotiation-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

/* 跑腿服务模块 */
.task-section {
  margin-bottom: 48px;
}

.task-categories {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  margin-bottom: var(--content-padding);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.task-category-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.task-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--content-padding);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  display: flex;
  gap: var(--spacing-lg);
}

.task-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.task-icon.icon-orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.task-content {
  flex: 1;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.task-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.task-reward {
  font-size: 18px;
  font-weight: bold;
  color: var(--color-secondary);
}

.task-desc {
  font-size: 14px;
  color: var(--color-text-regular);
  margin: 0 0 var(--spacing-md) 0;
}

.task-meta {
  display: flex;
  gap: var(--spacing-lg);
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-lg);
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 12px;
  color: var(--color-text-regular);
}

.task-actions {
  display: flex;
  gap: 8px;
}

.task-publish-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--content-padding);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  margin-top: var(--spacing-2xl);
}

.publish-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.publish-icon {
  width: 32px;
  height: 32px;
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.publish-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}



/* 底部导航栏（移动端） */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: var(--color-bg-white);
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: var(--header-height);
  z-index: 999;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  color: var(--color-text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item.active {
  color: var(--color-primary);
}

.nav-item .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.publish-nav-item {
  position: relative;
}

.publish-nav-item .el-icon {
  background-color: var(--color-primary);
  color: var(--color-bg-white);
  border-radius: var(--radius-full);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -20px;
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
}

/* 消息通知面板 */
.notification-panel {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 320px;
  max-height: 500px;
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
  z-index: 1001;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.notification-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: var(--color-text-primary);
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.notification-item:hover {
  background-color: var(--color-bg-primary);
}

.notification-empty {
  padding: 40px 0;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.notification-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--spacing-md);
  flex-shrink: 0;
}

.notification-icon.blue {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.notification-icon.green {
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
}

.notification-icon.orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 14px;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-xs) 0;
  font-weight: 500;
}

.notification-desc {
  font-size: 12px;
  color: var(--color-text-regular);
  margin: 0 0 var(--spacing-xs) 0;
}

.notification-time {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0;
}

.notification-dot {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  width: var(--spacing-sm);
  height: var(--spacing-sm);
  background-color: var(--color-danger);
  border-radius: var(--radius-full);
}

.notification-footer {
  padding: var(--spacing-md);
  border-top: 1px solid var(--color-border);
  text-align: center;
}

/* 消息通知按钮 */
.notification-btn-wrapper {
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1000;
}

.notification-btn {
  width: 48px;
  height: 48px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .header-center {
    flex: 1;
    padding: 0 var(--spacing-md);
    min-width: 500px; /* 在中等屏幕上保持足够宽度 */
  }
  
  .header-center :deep(.el-menu--horizontal .el-menu-item) {
    padding: 0 var(--spacing-md); /* 保持合理的 padding */
    font-size: 14px;
    display: inline-flex !important;
    min-width: 70px; /* 设置最小宽度 */
  }
  
  /* 确保所有菜单项都显示，包括跑腿服务 */
  .header-center :deep(.el-menu--horizontal) {
    overflow: visible !important;
  }
  
  /* 隐藏"更多"下拉菜单按钮 */
  .header-center :deep(.el-menu--horizontal .el-submenu__title) {
    display: none !important;
  }
  
  .search-wrapper {
    width: 150px;
  }
  
  .publish-btn {
    display: none;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .search-wrapper {
    display: none;
  }
  
  .nickname {
    display: none;
  }
  
  .mobile-menu-btn {
    display: block;
  }
  
  .main-content {
    padding: 16px;
    padding-bottom: 80px; /* 为底部导航栏留出空间 */
  }
  
  .notification-panel {
    width: calc(100% - 32px);
    right: 16px;
    bottom: 80px;
  }
  
  .notification-btn-wrapper {
    bottom: 80px;
    right: 16px;
  }
  
  .banner-content {
    flex-direction: column;
  }
  
  .banner-stats {
    width: 100%;
    justify-content: space-around;
  }
  
  .module-grid {
    grid-template-columns: 1fr;
  }
  
  .module-card {
    padding: 24px 20px;
  }
  
  .module-icon {
    width: 56px;
    height: 56px;
  }
  
  .module-title {
    font-size: 16px;
  }
  
  .module-desc {
    font-size: 13px;
  }
  
  .card-grid {
    grid-template-columns: 1fr;
  }
  
  .goods-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>

