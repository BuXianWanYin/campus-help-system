<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
      <div class="header-container">
        <div class="header-left">
        </div>
        
        <div class="header-right">
          <!-- 消息通知按钮 -->
          <el-popover
            placement="bottom-end"
            :width="350"
            trigger="click"
            v-model:visible="messagePanelVisible"
            :teleported="true"
            :hide-after="0"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                <el-button
                  text
                  class="notification-btn"
                >
                  <el-icon><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="message-panel">
              <div class="message-panel-header">
                <h3>消息通知</h3>
                <el-button text type="primary" size="small" @click="handleMarkAllAsRead" :disabled="unreadCount === 0">
                  全部已读
                </el-button>
              </div>
              <div class="message-panel-content">
                <div v-if="recentMessages.length === 0" class="empty-messages">
                  <p>暂无消息</p>
                </div>
                <div v-else class="message-list">
                  <div
                    v-for="item in recentMessages"
                    :key="item.id"
                    class="message-item"
                    :class="{ 'unread': item.isRead === 0 }"
                    @click="handleMessageClick(item)"
                  >
                    <div class="message-icon" :class="item.type">
                      <el-icon><component :is="item.icon" /></el-icon>
                    </div>
                    <div class="message-info">
                      <div class="message-title">{{ item.title }}</div>
                      <div class="message-content">{{ item.content }}</div>
                      <div class="message-time">{{ item.time }}</div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="message-panel-footer">
                <el-button text type="primary" @click="goToMessages">查看全部消息</el-button>
              </div>
            </div>
          </el-popover>
          
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="getAvatarUrl(userStore.userInfo?.avatar)">
                {{ userStore.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="nickname">{{ userStore.nickname || userStore.email }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">
                  <el-icon><HomeFilled /></el-icon>
                  校园帮首页
                </el-dropdown-item>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
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
        </div>
      </div>
    </header>
    
    <div class="admin-container">
      <!-- 左侧菜单 -->
      <aside class="admin-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          router
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/verification">
            <el-icon><DocumentChecked /></el-icon>
            <span>实名认证审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/lost-found-audit">
            <el-icon><DocumentChecked /></el-icon>
            <span>失物招领审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/goods-audit">
            <el-icon><DocumentChecked /></el-icon>
            <span>商品审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/question-audit">
            <el-icon><DocumentChecked /></el-icon>
            <span>学习问题审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/messages">
            <el-icon><Message /></el-icon>
            <span>消息通知</span>
          </el-menu-item>
          <el-menu-item index="/admin/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/admin/content" disabled>
            <el-icon><Files /></el-icon>
            <span>信息审核</span>
            <el-tag size="small" type="info" style="margin-left: 8px;">开发中</el-tag>
          </el-menu-item>
          <el-menu-item index="/admin/statistics" disabled>
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
            <el-tag size="small" type="info" style="margin-left: 8px;">开发中</el-tag>
          </el-menu-item>
        </el-menu>
      </aside>
      
      <!-- 主内容区 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, markRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Tools, ArrowDown, User, SwitchButton, HomeFilled,
  DataAnalysis, DocumentChecked, UserFilled, Files, TrendCharts,
  Document, Message, Setting, Bell, Check, Close, Warning, InfoFilled
} from '@element-plus/icons-vue'
import { getAvatarUrl } from '@/utils/image'
import { messageApi } from '@/api'
import wsManager from '@/utils/websocket'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 消息相关
const messagePanelVisible = ref(false)
const unreadCount = ref(0)
const recentMessages = ref([])

const handleCommand = (command) => {
  switch (command) {
    case 'home':
      // 在新标签页打开首页
      window.open('/home', '_blank')
      break
    case 'profile':
      // 跳转到后台的个人中心页面
      router.push('/admin/profile')
      break
    case 'settings':
      // 在新标签页打开设置
      window.open('/user/settings', '_blank')
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

// 获取未读消息数量
const fetchUnreadCount = async () => {
  try {
    const response = await messageApi.getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 获取最新消息列表
const fetchRecentMessages = async () => {
  try {
    const response = await messageApi.getMessagePage({
      current: 1,
      size: 5
    })
    if (response.code === 200) {
      const messages = response.data.records || []
      recentMessages.value = messages.map(msg => ({
        id: msg.id,
        title: msg.title,
        content: msg.content,
        time: formatMessageTime(msg.createTime),
        isRead: msg.isRead,
        type: getMessageTypeClass(msg.type),
        icon: getMessageIcon(msg.type),
        relatedType: msg.relatedType,
        relatedId: msg.relatedId
      }))
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
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
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 获取消息类型样式类
const getMessageTypeClass = (type) => {
  const typeMap = {
    'VERIFICATION_APPROVED': 'type-success',
    'VERIFICATION_REJECTED': 'type-error',
    'LOST_FOUND_APPROVED': 'type-success',
    'LOST_FOUND_REJECTED': 'type-error',
    'CLAIM_APPLY': 'type-info',
    'CLAIM_CONFIRMED': 'type-success',
    'CLAIM_REJECTED': 'type-error',
    'ADMIN_AUDIT_REQUIRED': 'type-warning',
    'ADMIN_VERIFICATION_REQUIRED': 'type-warning',
    'ORDER_STATUS': 'type-info',
    'STUDY_STATUS': 'type-warning',
    'ANNOUNCEMENT': 'type-primary'
  }
  return typeMap[type] || 'type-default'
}

// 获取消息图标
const getMessageIcon = (type) => {
  const iconMap = {
    'VERIFICATION_APPROVED': markRaw(Check),
    'VERIFICATION_REJECTED': markRaw(Close),
    'LOST_FOUND_APPROVED': markRaw(Check),
    'LOST_FOUND_REJECTED': markRaw(Close),
    'CLAIM_APPLY': markRaw(InfoFilled),
    'CLAIM_CONFIRMED': markRaw(Check),
    'CLAIM_REJECTED': markRaw(Close),
    'ADMIN_AUDIT_REQUIRED': markRaw(Warning),
    'ADMIN_VERIFICATION_REQUIRED': markRaw(Warning),
    'ORDER_STATUS': markRaw(InfoFilled),
    'STUDY_STATUS': markRaw(Warning),
    'ANNOUNCEMENT': markRaw(Bell)
  }
  return iconMap[type] || markRaw(InfoFilled)
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  try {
    await messageApi.markAllAsRead()
    ElMessage.success('已全部标记为已读')
    recentMessages.value.forEach(msg => {
      msg.isRead = 1
    })
    unreadCount.value = 0
    await fetchRecentMessages()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 前往消息页面
const goToMessages = () => {
  router.push('/admin/messages')
  messagePanelVisible.value = false
}

// 点击消息
const handleMessageClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await messageApi.markAsRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 根据消息类型跳转到相应页面
  if (item.relatedType === 'LOST_FOUND' && item.relatedId) {
    // 失物招领相关消息，跳转到审核页面
    router.push(`/admin/lost-found-audit`)
    messagePanelVisible.value = false
  } else if (item.relatedType === 'VERIFICATION' && item.relatedId) {
    // 实名认证相关消息，跳转到认证审核页面
    router.push(`/admin/verification`)
    messagePanelVisible.value = false
  } else {
    // 其他消息，跳转到消息列表页面
    goToMessages()
  }
}

// WebSocket消息处理
const handleWebSocketMessage = (message) => {
  if (message && message.id) {
    ElMessage.success('您有新的消息')
    fetchUnreadCount()
    if (messagePanelVisible.value) {
      fetchRecentMessages()
    } else {
      // 即使面板未打开，也刷新消息列表，以便下次打开时显示最新消息
      fetchRecentMessages()
    }
  }
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await fetchUnreadCount()
    await fetchRecentMessages()
    
    // 连接WebSocket并订阅系统消息
    if (wsManager.isConnected) {
      wsManager.addMessageHandler(handleWebSocketMessage)
    } else {
      wsManager.connect(userStore.token).then(() => {
        wsManager.addMessageHandler(handleWebSocketMessage)
      })
    }
    
    // 监听通知面板显示，自动刷新消息
    watch(messagePanelVisible, (visible) => {
      if (visible) {
        fetchRecentMessages()
      }
    })
  }
})

onUnmounted(() => {
  wsManager.removeMessageHandler(handleWebSocketMessage)
})
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background-color: var(--color-bg-primary);
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.admin-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: var(--color-bg-white);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.header-container {
  max-width: 100%;
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

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
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

/* 消息通知 */
.notification-badge {
  margin-right: 12px;
}

.notification-btn {
  font-size: 20px;
  color: var(--color-text-primary);
}

.message-panel {
  max-height: 500px;
  display: flex;
  flex-direction: column;
}

.message-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}

.message-panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.message-panel-content {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
}

.empty-messages {
  padding: 40px;
  text-align: center;
  color: var(--color-text-secondary);
}

.message-list {
  padding: 8px 0;
}

.message-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-bottom: 1px solid var(--color-border);
}

.message-item:hover {
  background-color: var(--color-bg-primary);
}

.message-item.unread {
  background-color: #f0f9ff;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.message-icon.type-success {
  background-color: #f0f9ff;
  color: #67c23a;
}

.message-icon.type-error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.message-icon.type-info {
  background-color: #f4f4f5;
  color: #909399;
}

.message-icon.type-warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.message-icon.type-primary {
  background-color: #ecf5ff;
  color: #409eff;
}

.message-icon.type-default {
  background-color: #f4f4f5;
  color: #909399;
}

.message-info {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.message-content {
  font-size: 12px;
  color: var(--color-text-regular);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.message-panel-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);
  text-align: center;
}

/* 主容器 */
.admin-container {
  display: flex;
  flex: 1;
  min-height: calc(100vh - var(--header-height));
}

/* 左侧菜单 */
.admin-sidebar {
  width: 240px;
  background-color: var(--color-bg-white);
  border-right: 1px solid var(--color-border);
  overflow-y: auto;
}

.admin-menu {
  border-right: none;
  padding: var(--spacing-md) 0;
}

.admin-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px var(--spacing-md);
  border-radius: var(--radius-md);
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: var(--color-bg-primary);
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

/* 主内容区 */
.admin-content {
  flex: 1;
  padding: var(--spacing-2xl);
  overflow-y: auto;
  background-color: var(--color-bg-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 200px;
  }
  
  .admin-content {
    padding: var(--spacing-lg);
  }
}
</style>

