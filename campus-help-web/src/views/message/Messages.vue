<template>
  <div class="messages-container">
    <div class="messages-header">
      <h1 class="page-title">消息通知</h1>
      <div class="header-actions">
        <el-button 
          v-if="selectedMessages.length > 0"
          type="danger" 
          @click="handleBatchDelete"
          :loading="batchDeleting"
        >
          批量删除 ({{ selectedMessages.length }})
        </el-button>
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
      
      <div v-else class="messages-list-content">
        <div class="list-header">
          <el-checkbox 
            v-model="selectAll" 
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >
            全选
          </el-checkbox>
          <span v-if="selectedMessages.length > 0" class="selected-count">
            已选择 {{ selectedMessages.length }} 条
          </span>
        </div>
        
        <div 
          v-for="message in messages" 
          :key="message.id" 
          class="message-item"
          :class="{ 'unread': message.isRead === 0, 'selected': selectedMessages.includes(message.id) }"
          @click="handleMessageClick(message)"
        >
          <el-checkbox 
            :model-value="selectedMessages.includes(message.id)"
            @change="(val) => handleSelectMessage(message.id, val)"
            @click.stop
            class="message-checkbox"
          />
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Bell, Check, Close, Warning, InfoFilled, 
  Document, ShoppingBag, TrendCharts, ChatDotRound
} from '@element-plus/icons-vue'
import { messageApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const messages = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const filterType = ref('all')
const unreadCount = ref(0)
const selectedMessages = ref([])
const selectAll = ref(false)
const batchDeleting = ref(false)

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
      // 更新全选状态
      updateSelectAllState()
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
  
  // 根据消息类型跳转到相关页面
  if (message.relatedType && message.relatedId) {
    if (message.relatedType === 'LOST_FOUND') {
      // 失物招领相关消息
      if (userStore.isAdmin && (message.type === 'ADMIN_AUDIT_REQUIRED')) {
        // 管理员审核消息，跳转到审核页面
        router.push('/admin/lost-found-audit')
      } else {
        // 普通用户，跳转到失物详情
        router.push(`/lost-found/detail/${message.relatedId}`)
      }
    } else if (message.relatedType === 'VERIFICATION') {
      // 实名认证相关消息
      if (userStore.isAdmin && (message.type === 'ADMIN_VERIFICATION_REQUIRED')) {
        // 管理员审核消息，跳转到审核页面
        router.push('/admin/verification')
      } else {
        // 普通用户，跳转到实名认证页面
        router.push('/user/verification')
      }
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
    // 从选中列表中移除
    selectedMessages.value = selectedMessages.value.filter(id => id !== messageId)
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
  selectedMessages.value = []
  selectAll.value = false
  fetchMessages()
}

// 选择/取消选择消息
const handleSelectMessage = (messageId, checked) => {
  if (checked) {
    if (!selectedMessages.value.includes(messageId)) {
      selectedMessages.value.push(messageId)
    }
  } else {
    selectedMessages.value = selectedMessages.value.filter(id => id !== messageId)
  }
  updateSelectAllState()
}

// 全选/取消全选
const handleSelectAll = (checked) => {
  if (checked) {
    selectedMessages.value = messages.value.map(msg => msg.id)
  } else {
    selectedMessages.value = []
  }
}

// 更新全选状态
const updateSelectAllState = () => {
  if (messages.value.length === 0) {
    selectAll.value = false
    return
  }
  const allSelected = messages.value.every(msg => selectedMessages.value.includes(msg.id))
  selectAll.value = allSelected
}

// 计算是否半选状态
const isIndeterminate = computed(() => {
  if (messages.value.length === 0) return false
  const someSelected = messages.value.some(msg => selectedMessages.value.includes(msg.id))
  const allSelected = messages.value.every(msg => selectedMessages.value.includes(msg.id))
  return someSelected && !allSelected
})

// 批量删除
const handleBatchDelete = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请选择要删除的消息')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchDeleting.value = true
    // 批量删除
    const deletePromises = selectedMessages.value.map(id => messageApi.deleteMessage(id))
    await Promise.all(deletePromises)
    
    ElMessage.success(`成功删除 ${selectedMessages.value.length} 条消息`)
    selectedMessages.value = []
    selectAll.value = false
    
    // 重新加载列表
    await fetchMessages()
    await fetchUnreadCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除失败')
    }
  } finally {
    batchDeleting.value = false
  }
}

// 分页变化
const handlePageChange = () => {
  selectedMessages.value = []
  selectAll.value = false
  fetchMessages()
}

const handleSizeChange = () => {
  currentPage.value = 1
  selectedMessages.value = []
  selectAll.value = false
  fetchMessages()
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
    'LOST_FOUND_APPROVED': Check,
    'LOST_FOUND_REJECTED': Close,
    'CLAIM_APPLY': InfoFilled,
    'CLAIM_CONFIRMED': Check,
    'CLAIM_REJECTED': Close,
    'ADMIN_AUDIT_REQUIRED': Warning,
    'ADMIN_VERIFICATION_REQUIRED': Warning,
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

// 监听消息列表变化，更新全选状态
watch(() => messages.value, () => {
  updateSelectAllState()
}, { deep: true })

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

.messages-list-content {
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: var(--spacing-md);
}

.selected-count {
  font-size: 14px;
  color: var(--color-text-regular);
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  border-radius: 8px;
  margin-bottom: 8px;
}

.message-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.message-item:hover {
  background-color: #f5f7fa;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-item.unread {
  background-color: #f0f9ff;
  border-left: 4px solid var(--color-primary);
}

.message-item.unread::before {
  display: none;
}

.message-item.selected {
  background-color: #ecf5ff;
  border-color: var(--color-primary);
}

.message-checkbox {
  margin-right: var(--spacing-md);
  flex-shrink: 0;
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
  font-size: 20px;
}

.message-icon.type-error {
  background-color: #fef0f0;
  color: #f56c6c;
  font-size: 20px;
}

.message-icon.type-info {
  background-color: #ecf5ff;
  color: #409eff;
  font-size: 20px;
}

.message-icon.type-warning {
  background-color: #fdf6ec;
  color: #e6a23c;
  font-size: 20px;
}

.message-icon.type-primary {
  background-color: #ecf5ff;
  color: #409eff;
  font-size: 20px;
}

.message-icon.type-default {
  background-color: #f4f4f5;
  color: #909399;
  font-size: 20px;
}

.message-content {
  flex: 1;
  min-width: 0;
  padding-right: var(--spacing-md);
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 8px;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  flex: 1;
}

.message-text {
  font-size: 14px;
  color: var(--color-text-regular);
  margin: 0 0 8px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
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

