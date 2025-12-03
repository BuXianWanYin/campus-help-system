<template>
  <div class="messages-container">
    <div class="messages-header">
      <h1 class="page-title">消息通知</h1>
      <div class="header-actions">
        <el-button 
          type="primary" 
          :disabled="unreadCount === 0"
          @click="handleMarkAllAsRead"
        >
          全部标记为已读
        </el-button>
      </div>
    </div>
    
    <div class="messages-filter">
      <el-radio-group v-model="filterType" @change="handleFilterChange">
        <el-radio-button label="all">全部消息</el-radio-button>
        <el-radio-button label="unread">未读消息</el-radio-button>
      </el-radio-group>
    </div>
    
    <div class="messages-list" v-loading="loading">
      <div v-if="messages.length === 0" class="empty-state">
        <el-empty description="暂无消息" />
      </div>
      
      <div 
        v-for="message in messages" 
        :key="message.id" 
        class="message-item"
        :class="{ 'unread': message.isRead === 0 }"
        @click="handleMessageClick(message)"
      >
        <div class="message-icon" :class="getMessageTypeClass(message.type)">
          <el-icon><component :is="getMessageIcon(message.type)" /></el-icon>
        </div>
        <div class="message-content">
          <div class="message-header">
            <h3 class="message-title">{{ message.title }}</h3>
            <el-tag v-if="message.isRead === 0" type="danger" size="small" effect="plain">未读</el-tag>
          </div>
          <p class="message-text">{{ message.content }}</p>
          <p class="message-time">{{ formatTime(message.createTime) }}</p>
        </div>
        <div class="message-actions">
          <el-button 
            text 
            type="danger" 
            size="small"
            @click.stop="handleDelete(message.id)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="messages-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Bell, Check, Close, Warning, InfoFilled, 
  Document, ShoppingBag, TrendCharts, ChatDotRound
} from '@element-plus/icons-vue'
import { messageApi } from '@/api'

const userStore = useUserStore()

const loading = ref(false)
const messages = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const filterType = ref('all')
const unreadCount = ref(0)

// 获取消息列表
const fetchMessages = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (filterType.value === 'unread') {
      params.unreadOnly = true
    }
    
    const response = await messageApi.getMessagePage(params)
    if (response.code === 200) {
      messages.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '获取消息列表失败')
  } finally {
    loading.value = false
  }
}

// 获取未读数量
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

// 标记为已读
const handleMessageClick = async (message) => {
  if (message.isRead === 0) {
    try {
      await messageApi.markAsRead(message.id)
      message.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  try {
    await messageApi.markAllAsRead()
    ElMessage.success('已全部标记为已读')
    messages.value.forEach(msg => {
      msg.isRead = 1
    })
    unreadCount.value = 0
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 删除消息
const handleDelete = async (messageId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await messageApi.deleteMessage(messageId)
    ElMessage.success('删除成功')
    // 重新加载列表
    await fetchMessages()
    await fetchUnreadCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchMessages()
}

// 分页变化
const handlePageChange = () => {
  fetchMessages()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchMessages()
}

// 获取消息类型样式类
const getMessageTypeClass = (type) => {
  const typeMap = {
    'VERIFICATION_APPROVED': 'type-success',
    'VERIFICATION_REJECTED': 'type-error',
    'ORDER_STATUS': 'type-info',
    'TASK_STATUS': 'type-warning',
    'ANNOUNCEMENT': 'type-primary'
  }
  return typeMap[type] || 'type-default'
}

// 获取消息图标
const getMessageIcon = (type) => {
  const iconMap = {
    'VERIFICATION_APPROVED': Check,
    'VERIFICATION_REJECTED': Close,
    'ORDER_STATUS': ShoppingBag,
    'TASK_STATUS': TrendCharts,
    'ANNOUNCEMENT': Bell
  }
  return iconMap[type] || InfoFilled
}

// 格式化时间
const formatTime = (timeStr) => {
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

onMounted(async () => {
  await fetchMessages()
  await fetchUnreadCount()
})
</script>

<style scoped>
.messages-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-2xl);
  min-height: calc(100vh - var(--header-height));
  background-color: var(--color-bg-primary);
}

.messages-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0;
}

.messages-filter {
  margin-bottom: var(--spacing-lg);
}

.messages-list {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  min-height: 400px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.message-item {
  display: flex;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.message-item:last-child {
  border-bottom: none;
}

.message-item:hover {
  background-color: var(--color-bg-secondary);
}

.message-item.unread {
  background-color: #f0f9ff;
}

.message-item.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: var(--color-primary);
}

.message-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--spacing-lg);
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

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.message-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
  flex: 1;
}

.message-text {
  font-size: 14px;
  color: var(--color-text-regular);
  margin: var(--spacing-xs) 0;
  line-height: 1.6;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: var(--spacing-xs) 0 0 0;
}

.message-actions {
  display: flex;
  align-items: center;
  margin-left: var(--spacing-lg);
}

.messages-pagination {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .messages-container {
    padding: var(--spacing-lg);
  }
  
  .messages-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
  
  .message-item {
    flex-direction: column;
  }
  
  .message-actions {
    margin-left: 0;
    margin-top: var(--spacing-md);
    justify-content: flex-end;
  }
}
</style>

