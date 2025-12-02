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
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="nickname">{{ userStore.nickname || userStore.email }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
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

      <!-- 功能模块导航 -->
      <section class="feature-modules">
        <div class="section-header">
          <h2 class="section-title">服务分类</h2>
          <el-link type="primary" :underline="false" @click="goToRoute('/home')">查看全部</el-link>
        </div>
        <div class="module-grid">
          <div v-for="module in featureModules" :key="module.id" class="module-card" @click="goToModule(module.path)">
            <div class="module-icon" :class="module.colorClass">
              <el-icon :size="32"><component :is="module.icon" /></el-icon>
            </div>
            <div class="module-content">
              <h3 class="module-title">{{ module.title }}</h3>
              <p class="module-desc">{{ module.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 失物招领模块 -->
      <section class="lost-found-section">
        <div class="section-header">
          <h2 class="section-title">失物招领</h2>
          <div class="section-actions">
            <div class="tab-buttons">
              <el-button :type="lostFoundTab === 'lost' ? 'primary' : ''" size="small" @click="lostFoundTab = 'lost'">失物</el-button>
              <el-button :type="lostFoundTab === 'found' ? 'primary' : ''" size="small" @click="lostFoundTab = 'found'">招领</el-button>
            </div>
            <el-link type="primary" :underline="false" @click="goToRoute('/lost-found/list')">更多</el-link>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="分类：">
              <el-select v-model="lostFoundFilters.category" placeholder="全部" style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="电子产品" value="electronics" />
                <el-option label="证件" value="id" />
                <el-option label="衣物" value="clothing" />
                <el-option label="书籍" value="book" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="地点：">
              <el-select v-model="lostFoundFilters.location" placeholder="全部" style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="教学楼" value="teaching" />
                <el-option label="图书馆" value="library" />
                <el-option label="食堂" value="canteen" />
                <el-option label="宿舍" value="dormitory" />
                <el-option label="体育场" value="stadium" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间：">
              <el-select v-model="lostFoundFilters.time" placeholder="全部" style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="今天" value="today" />
                <el-option label="最近3天" value="3days" />
                <el-option label="最近一周" value="week" />
                <el-option label="最近一月" value="month" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleFilter">筛选</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 失物列表 -->
        <div class="card-grid">
          <div v-for="item in lostFoundList" :key="item.id" class="lost-found-card" @click="goToDetail('lost-found', item.id)">
            <div class="card-image-wrapper">
              <img :src="item.image" :alt="item.title" class="card-image" />
              <span class="card-badge" :class="item.type === 'lost' ? 'badge-red' : 'badge-green'">
                {{ item.type === 'lost' ? '失物' : '招领' }}
              </span>
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ item.title }}</h3>
              <p class="card-desc">{{ item.description }}</p>
              <div class="card-meta">
                <span class="meta-item">
                  <el-icon><Location /></el-icon>
                  {{ item.location }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ item.time }}
                </span>
              </div>
              <div class="card-footer">
                <div class="card-user">
                  <el-avatar :size="24" :src="item.userAvatar">{{ item.userName.charAt(0) }}</el-avatar>
                  <span>{{ item.userName }}</span>
                </div>
                <el-button type="primary" size="small" text @click.stop="handleContact(item)">联系TA</el-button>
              </div>
            </div>
          </div>
        </div>

      </section>

      <!-- 闲置交易模块 -->
      <section class="goods-section">
        <div class="section-header">
          <h2 class="section-title">闲置交易</h2>
          <el-link type="primary" :underline="false" @click="goToRoute('/goods/list')">更多</el-link>
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
                  <el-avatar :size="20" :src="item.userAvatar">{{ item.userName.charAt(0) }}</el-avatar>
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
            <el-link type="primary" :underline="false" @click="goToRoute('/task/list')">更多</el-link>
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
                  <el-avatar :size="24" :src="task.userAvatar">{{ task.userName.charAt(0) }}</el-avatar>
                  <span>{{ task.userName }}</span>
                  <div class="task-rating">
                    <el-icon v-for="i in 5" :key="i" :class="{ 'star-filled': i <= task.rating }"><Star /></el-icon>
                  </div>
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

      <!-- 信用评价体系 -->
      <section class="credit-section">
        <div class="section-header">
          <h2 class="section-title">信用评价</h2>
          <el-link type="primary" :underline="false" @click="goToRoute('/user/profile')">查看我的信用</el-link>
        </div>
        <div class="credit-card">
          <div class="credit-left">
            <div class="credit-profile">
              <el-avatar :size="96" :src="userStore.userInfo?.avatar">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <h3>{{ userStore.nickname || userStore.email }}</h3>
              <p>计算机科学与技术学院</p>
              <div class="credit-rating">
                <el-icon v-for="i in 5" :key="i" :class="{ 'star-filled': i <= 4 }"><Star /></el-icon>
              </div>
              <div class="credit-score">
                <span class="score-value">{{ userStore.userInfo?.creditScore || 92 }}</span>
                <span class="score-label">信用分</span>
              </div>
              <el-tag type="success" size="small">优秀信用用户</el-tag>
            </div>
          </div>
          <div class="credit-right">
            <h3 class="credit-subtitle">信用评分详情</h3>
            <div class="credit-metrics">
              <div v-for="metric in creditMetrics" :key="metric.name" class="metric-item">
                <div class="metric-header">
                  <span>{{ metric.name }}</span>
                  <span>{{ metric.value }}%</span>
                </div>
                <el-progress :percentage="metric.value" :color="metric.color" />
              </div>
            </div>
            <h3 class="credit-subtitle">信用记录</h3>
            <div class="credit-records">
              <div v-for="record in creditRecords" :key="record.id" class="credit-record">
                <div class="record-icon" :class="record.type">
                  <el-icon><component :is="record.icon" /></el-icon>
                </div>
                <div class="record-content">
                  <div class="record-header">
                    <span>{{ record.title }}</span>
                    <span>{{ record.date }}</span>
                  </div>
                  <p>{{ record.desc }}</p>
                </div>
                <div class="record-score" :class="record.scoreType">
                  {{ record.score > 0 ? '+' : '' }}{{ record.score }}分
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 数据统计分析 -->
      <section class="stats-section">
        <div class="section-header">
          <h2 class="section-title">校园互助数据</h2>
          <el-select v-model="statsPeriod" style="width: 120px">
            <el-option label="最近7天" value="7days" />
            <el-option label="最近30天" value="30days" />
            <el-option label="本学期" value="semester" />
            <el-option label="本学年" value="year" />
          </el-select>
        </div>
        <div class="stats-cards">
          <div v-for="stat in statsData" :key="stat.id" class="stat-card">
            <div class="stat-card-content">
              <div>
                <p class="stat-label">{{ stat.label }}</p>
                <h3 class="stat-value">{{ stat.value }}</h3>
                <p class="stat-change" :class="stat.changeType">
                  <el-icon><component :is="stat.changeIcon" /></el-icon>
                  {{ stat.change }} 较上周
                </p>
              </div>
              <div class="stat-icon" :class="stat.colorClass">
                <el-icon :size="24"><component :is="stat.icon" /></el-icon>
              </div>
            </div>
          </div>
        </div>
        <div class="charts-grid">
          <div class="chart-card">
            <h3>互助类型分布</h3>
            <div ref="typeChartRef" class="chart-container"></div>
          </div>
          <div class="chart-card">
            <h3>每日互助趋势</h3>
            <div ref="trendChartRef" class="chart-container"></div>
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
      <div class="notification-list">
        <!-- 消息列表内容 -->
        <div v-for="item in notifications" :key="item.id" class="notification-item">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, ArrowDown, User, Setting, SwitchButton, Menu,
  HomeFilled, Plus, ShoppingBag, Document, Message, Bell, ChatDotRound, Check, Star,
  Location, Clock, View, Star as LightbulbIcon, Link, Box as ComputerIcon, Document as NotebookIcon, ShoppingBag as TShirtIcon, Star as BasketballIcon,
  Message as HeadsetIcon, Edit as EditPenIcon, More, ShoppingCart, ShoppingBag as ForkSpoonIcon, Link as ConnectionIcon, User as UsersIcon, View as TrendChartsIcon,
  Close, ArrowUp, ArrowDown as ArrowDownIcon, Box
} from '@element-plus/icons-vue'
import { initChart, resizeChart } from '@/utils/echarts'
import appConfig from '@/config'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 判断是否是首页
const isHomePage = computed(() => {
  return route.path === '/home' || route.path === '/'
})

const searchKeyword = ref('')
const activeMenu = ref('home')
const showMobileMenu = ref(false)
const showNotificationPanel = ref(false)
const unreadCount = ref(3)
const notifications = ref([
  { id: 1, title: '您的闲置商品有新的询价', description: 'iPhone 13 128GB 午夜色 9成新', time: '10分钟前', read: false, type: 'blue', icon: ChatDotRound },
  { id: 2, title: '您的跑腿任务已被接取', description: '取快递 - 顺丰快递', time: '30分钟前', read: false, type: 'green', icon: Check },
  { id: 3, title: '您收到了新的评价', description: '任务：取快递 - 顺丰快递', time: '2小时前', read: false, type: 'orange', icon: Star },
  { id: 4, title: '您的信用评分提升了', description: '当前信用分：92，较上周提升3分', time: '昨天', read: true, type: 'blue', icon: TrendChartsIcon }
])
const isMobile = ref(false)

// 功能模块
const featureModules = ref([
  { id: 1, title: '失物招领', icon: Search, path: '/lost-found/list', colorClass: 'icon-blue', description: '发布失物信息，寻找丢失物品' },
  { id: 2, title: '闲置交易', icon: ShoppingBag, path: '/goods/list', colorClass: 'icon-green', description: '买卖闲置物品，让资源再利用' },
  { id: 3, title: '跑腿服务', icon: TrendChartsIcon, path: '/task/list', colorClass: 'icon-orange', description: '发布或接取任务，互帮互助' }
])

// 失物招领
const lostFoundTab = ref('lost')
const lostFoundFilters = ref({ category: '', location: '', time: '' })
const lostFoundList = ref([
  { id: 1, title: '遗失黑色钱包一个', description: '内有身份证、银行卡等物品', location: '图书馆二楼', time: '2025-07-20', type: 'lost', image: 'https://via.placeholder.com/300x200?text=钱包', userAvatar: '', userName: '张同学' },
  { id: 2, title: '遗失小米手机一部', description: '黑色小米11，有蓝色手机壳', location: '教学楼A302', time: '2025-07-19', type: 'lost', image: 'https://via.placeholder.com/300x200?text=手机', userAvatar: '', userName: '李同学' },
  { id: 3, title: '捡到校园卡一张', description: '姓名：王小明，学号：20220101', location: '食堂一楼', time: '2025-07-20', type: 'found', image: 'https://via.placeholder.com/300x200?text=校园卡', userAvatar: '', userName: '赵同学' }
])

// 闲置交易
const goodsActiveCategory = ref(1)
const goodsCategories = ref([
  { id: 1, name: '数码产品', icon: ComputerIcon },
  { id: 2, name: '图书教材', icon: NotebookIcon },
  { id: 3, name: '服装鞋包', icon: TShirtIcon },
  { id: 4, name: '生活用品', icon: HomeFilled },
  { id: 5, name: '运动健身', icon: BasketballIcon },
  { id: 6, name: '乐器', icon: HeadsetIcon },
  { id: 7, name: '文创用品', icon: EditPenIcon },
  { id: 8, name: '更多分类', icon: More }
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
  { id: 1, title: '取快递 - 顺丰快递', description: '快递单号：SF1234567890，放在南门快递点', route: '南门快递点 → 东区宿舍', deadline: '2025-07-20 18:00前', reward: 10, icon: ShoppingCart, colorClass: 'icon-orange', userAvatar: '', userName: '周同学', rating: 5 },
  { id: 2, title: '买饭 - 西区食堂', description: '一份红烧肉盖浇饭，不要辣，加一个煎蛋', route: '西区食堂 → 教学楼C301', deadline: '2025-07-20 12:00前', reward: 8, icon: ForkSpoonIcon, colorClass: 'icon-orange', userAvatar: '', userName: '吴同学', rating: 4 },
  { id: 3, title: '送文件 - 教务处', description: '将成绩单送到教务处张老师办公室，需要签字带回', route: '行政楼 → 教务处', deadline: '2025-07-20 16:00前', reward: 15, icon: Document, colorClass: 'icon-orange', userAvatar: '', userName: '郑同学', rating: 4 }
])
const taskForm = ref({
  type: '',
  description: '',
  reward: 10,
  startLocation: '',
  endLocation: '',
  deadline: null
})

// 信用评价
const creditMetrics = ref([
  { name: '交易完成率', value: 98, color: 'var(--color-success)' },
  { name: '响应速度', value: 90, color: 'var(--color-primary)' },
  { name: '服务质量', value: 95, color: '#9c27b0' },
  { name: '用户评价', value: 88, color: 'var(--color-secondary)' }
])
const creditRecords = ref([
  { id: 1, title: '成功完成闲置交易', date: '2025-07-18', desc: '交易物品：MacBook Pro 2020款', score: 2, type: 'green', icon: Check, scoreType: 'score-positive' },
  { id: 2, title: '成功完成跑腿任务', date: '2025-07-15', desc: '任务内容：取快递', score: 1, type: 'green', icon: Check, scoreType: 'score-positive' },
  { id: 3, title: '失物招领信息真实有效', date: '2025-07-10', desc: '招领物品：校园卡', score: 2, type: 'green', icon: Check, scoreType: 'score-positive' },
  { id: 4, title: '任务超时未完成', date: '2025-06-20', desc: '任务内容：买饭', score: -3, type: 'red', icon: Close, scoreType: 'score-negative' }
])

// 统计数据
const statsPeriod = ref('7days')
const statsData = ref([
  { id: 1, label: '总互助次数', value: '12,580', change: '12.5%', changeType: 'change-up', changeIcon: ArrowUp, icon: ConnectionIcon, colorClass: 'icon-blue' },
  { id: 2, label: '活跃用户', value: '3,245', change: '8.3%', changeType: 'change-up', changeIcon: ArrowUp, icon: UsersIcon, colorClass: 'icon-green' },
  { id: 3, label: '平均响应时间', value: '15分钟', change: '5.2%', changeType: 'change-down', changeIcon: ArrowDownIcon, icon: Clock, colorClass: 'icon-orange' }
])

// 图表引用
const typeChartRef = ref(null)
const trendChartRef = ref(null)
let typeChartInstance = null
let trendChartInstance = null

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
  router.push('/publish')
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
const markAllAsRead = () => {
  unreadCount.value = 0
  notifications.value.forEach(item => {
    item.read = true
  })
  ElMessage.success('已全部标记为已读')
}

// 前往消息页面
const goToMessages = () => {
  router.push('/user/messages')
  showNotificationPanel.value = false
}

// 更新当前激活菜单
const updateActiveMenu = () => {
  const path = route.path
  if (path.startsWith('/lost-found')) {
    activeMenu.value = 'lost-found'
  } else if (path.startsWith('/goods')) {
    activeMenu.value = 'goods'
  } else if (path.startsWith('/task')) {
    activeMenu.value = 'task'
  } else if (path === '/home' || path === '/') {
    activeMenu.value = 'home'
  } else {
    activeMenu.value = 'home'
  }
}

// 跳转到功能模块
const goToModule = (path) => {
  router.push(path)
}

// 处理筛选
const handleFilter = () => {
  ElMessage.success('筛选功能开发中')
}

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

// 初始化图表
const initCharts = () => {
  if (typeChartRef.value) {
    typeChartInstance = initChart(typeChartRef.value, {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center'
      },
      series: [
        {
          name: '互助类型',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: 30, name: '失物招领', itemStyle: { color: '#1E88E5' } },
            { value: 40, name: '闲置交易', itemStyle: { color: '#4CAF50' } },
            { value: 20, name: '跑腿服务', itemStyle: { color: '#FF9800' } },
            { value: 5, name: '校园活动', itemStyle: { color: '#9C27B0' } },
            { value: 5, name: '志愿互助', itemStyle: { color: '#F44336' } }
          ]
        }
      ]
    })
  }

  if (trendChartRef.value) {
    trendChartInstance = initChart(trendChartRef.value, {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['失物招领', '闲置交易', '跑腿服务'],
        top: 10
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '失物招领',
          type: 'line',
          stack: 'Total',
          smooth: true,
          data: [45, 52, 48, 61, 55, 42, 38],
          itemStyle: { color: '#1E88E5' },
          areaStyle: { opacity: 0.3 }
        },
        {
          name: '闲置交易',
          type: 'line',
          stack: 'Total',
          smooth: true,
          data: [78, 82, 75, 90, 85, 70, 65],
          itemStyle: { color: '#4CAF50' },
          areaStyle: { opacity: 0.3 }
        },
        {
          name: '跑腿服务',
          type: 'line',
          stack: 'Total',
          smooth: true,
          data: [35, 40, 38, 45, 42, 30, 28],
          itemStyle: { color: '#FF9800' },
          areaStyle: { opacity: 0.3 }
        }
      ]
    })
  }
}

const handleResize = () => {
  resizeChart(typeChartInstance)
  resizeChart(trendChartInstance)
}

onMounted(() => {
  checkMobile()
  updateActiveMenu()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('resize', handleResize)
  router.afterEach(() => {
    updateActiveMenu()
  })
  
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initCharts()
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('resize', handleResize)
  if (typeChartInstance) {
    typeChartInstance.dispose()
  }
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
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
}

.main-menu {
  border-bottom: none;
}

:deep(.el-menu--horizontal .el-menu-item) {
  height: var(--header-height);
  line-height: var(--header-height);
  color: var(--color-text-regular);
  font-size: 15px;
  padding: 0 var(--spacing-2xl);
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
  height: 200px;
  overflow: hidden;
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
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.card-desc {
  font-size: 14px;
  color: var(--color-text-regular);
  margin: 0 0 var(--spacing-md) 0;
  line-height: 1.5;
}

.card-meta {
  display: flex;
  gap: var(--spacing-lg);
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-md);
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
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
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

.task-rating {
  display: flex;
  gap: 2px;
  margin-left: 8px;
}

.task-rating .el-icon {
  font-size: 12px;
  color: var(--color-border);
}

.task-rating .star-filled {
  color: var(--color-warning);
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

/* 信用评价体系 */
.credit-section {
  margin-bottom: 48px;
}

.credit-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  display: flex;
  gap: var(--spacing-3xl);
}

.credit-left {
  width: 33.33%;
  border-right: 1px solid var(--color-border);
  padding-right: var(--spacing-3xl);
}

.credit-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.credit-profile h3 {
  font-size: 18px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: var(--spacing-lg) 0 var(--spacing-xs) 0;
}

.credit-profile p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 var(--spacing-md) 0;
}

.credit-rating {
  display: flex;
  gap: 4px;
  margin: 12px 0;
}

.credit-rating .el-icon {
  font-size: 20px;
  color: var(--color-border);
}

.credit-rating .star-filled {
  color: var(--color-warning);
}

.credit-score {
  margin: var(--spacing-md) 0;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--color-primary);
}

.score-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-left: var(--spacing-xs);
}

.credit-right {
  flex: 1;
}

.credit-subtitle {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.credit-metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--color-text-regular);
}

.credit-records {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.credit-record {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.record-icon.green {
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
}

.record-icon.red {
  background-color: rgba(244, 67, 54, 0.1);
  color: var(--color-danger);
}

.record-content {
  flex: 1;
}

.record-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.record-content p {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0;
}

.record-score {
  font-size: 12px;
  font-weight: 500;
}

.record-score.score-positive {
  color: var(--color-success);
}

.record-score.score-negative {
  color: var(--color-danger);
}

/* 数据统计分析 */
.stats-section {
  margin-bottom: 48px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 var(--spacing-sm) 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.stat-change {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-change.change-up {
  color: var(--color-success);
}

.stat-change.change-down {
  color: var(--color-success);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.icon-blue {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.stat-icon.icon-green {
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
}

.stat-icon.icon-orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.chart-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 var(--content-padding) 0;
}

.chart-container {
  width: 100%;
  height: 256px;
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
    display: none;
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
  
  .card-grid,
  .goods-grid {
    grid-template-columns: 1fr;
  }
  
  .credit-card {
    flex-direction: column;
  }
  
  .credit-left {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--color-border);
    padding-right: 0;
    padding-bottom: var(--spacing-2xl);
    margin-bottom: var(--spacing-2xl);
  }
  
  .credit-metrics {
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

