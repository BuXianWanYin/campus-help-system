<template>
  <div class="chat-container">
    <!-- 左侧：会话列表 -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">聊天</h2>
      </div>
      <div class="session-list" v-loading="sessionsLoading">
        <div v-if="sessions.length === 0 && !sessionsLoading" class="empty-sessions">
          <el-empty description="暂无聊天会话" />
        </div>
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: currentSessionId === session.id }"
          @click="selectSession(session)"
        >
          <el-avatar :size="48" :src="getAvatarUrl(getOtherUser(session)?.avatar)">
            {{ getOtherUser(session)?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="session-info">
            <div class="session-name">{{ getOtherUser(session)?.nickname || '未知用户' }}</div>
            <div class="session-preview">{{ session.lastMessageContent || '暂无消息' }}</div>
          </div>
          <div class="session-meta">
            <div class="session-time">{{ formatTime(session.lastMessageTime || session.createTime) }}</div>
            <el-badge v-if="getUnreadCount(session) > 0" :value="getUnreadCount(session) > 99 ? '99+' : getUnreadCount(session)" class="session-badge" :max="99" />
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：聊天窗口 -->
    <div class="chat-main">
      <div v-if="!currentSessionId" class="no-session">
        <el-empty description="请选择一个会话开始聊天" />
      </div>
      <div v-else class="chat-window">
        <!-- 聊天窗口头部 -->
        <div class="chat-header">
          <div class="chat-user-info">
            <el-avatar :size="40" :src="getAvatarUrl(currentOtherUser?.avatar)">
              {{ currentOtherUser?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="user-details">
              <div class="user-name">{{ currentOtherUser?.nickname || '未知用户' }}</div>
              <div class="user-status" v-if="currentOtherUser">在线</div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer" v-loading="messagesLoading">
          <div v-if="messages.length === 0 && !messagesLoading" class="empty-messages">
            <el-empty description="暂无消息，开始聊天吧" />
          </div>
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{ 'message-sent': isSentMessage(message), 'message-received': !isSentMessage(message) }"
          >
            <el-avatar
              v-if="!isSentMessage(message)"
              :size="36"
              :src="getAvatarUrl(currentOtherUser?.avatar)"
              class="message-avatar"
            >
              {{ currentOtherUser?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="message-content-wrapper">
              <div class="message-bubble" :class="{ 'bubble-sent': isSentMessage(message), 'bubble-received': !isSentMessage(message) }">
                <div v-if="message.messageType === 'TEXT'" class="message-text">{{ message.content }}</div>
                <div v-else-if="message.messageType === 'IMAGE'" class="message-images">
                  <el-image
                    v-for="(img, index) in parseImages(message.images)"
                    :key="index"
                    :src="getAvatarUrl(img)"
                    fit="cover"
                    :preview-src-list="parseImages(message.images).map(i => getAvatarUrl(i))"
                    class="message-image"
                    loading="lazy"
                  >
                    <template #error>
                      <div class="image-error-small">图片加载失败</div>
                    </template>
                  </el-image>
                  <div v-if="message.content" class="message-image-caption">{{ message.content }}</div>
                </div>
              </div>
              <div class="message-time">
                {{ formatMessageTime(message.createTime) }}
                <span v-if="isSentMessage(message) && message.isRead === 1" class="read-status">✓ 已读</span>
                <span v-else-if="isSentMessage(message)" class="read-status unread">✓</span>
              </div>
            </div>
            <el-avatar
              v-if="isSentMessage(message)"
              :size="36"
              :src="getAvatarUrl(userStore.userInfo?.avatar)"
              class="message-avatar"
            >
              {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="chat-input-area">
          <div v-if="selectedImages.length > 0" class="image-preview-list">
            <div v-for="(img, index) in selectedImages" :key="index" class="image-preview-item">
              <el-image :src="img" fit="cover" class="preview-image" />
              <el-icon class="remove-image" @click="removeImage(index)"><Close /></el-icon>
            </div>
          </div>
          <div class="input-toolbar">
            <el-upload
              :action="''"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageSelect"
              accept="image/*"
              :multiple="true"
            >
              <el-button text type="primary">
                <el-icon><Picture /></el-icon>
              </el-button>
            </el-upload>
          </div>
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            :maxlength="500"
            placeholder="输入消息...（Ctrl+Enter 发送）"
            show-word-limit
            @keydown.ctrl.enter="handleSendMessage"
            @keydown.enter.exact.prevent="handleSendMessage"
          />
          <div class="input-actions">
            <el-button type="primary" @click="handleSendMessage" :loading="sending">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Close } from '@element-plus/icons-vue'
import { chatApi, fileApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'
import wsManager from '@/utils/websocket'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref(null)
const messages = ref([])
const messagesLoading = ref(false)
const sending = ref(false)
const inputMessage = ref('')
const messagesContainer = ref(null)
const selectedImages = ref([])
const chatSubscription = ref(null)

// 获取对方用户信息
const getOtherUser = (session) => {
  if (!session || !userStore.userInfo) return null
  
  // 如果后端返回了对方用户信息，直接使用
  if (session.otherUser) {
    return session.otherUser
  }
  
  // 否则从 user1Id 和 user2Id 判断（前端需要单独查询用户信息）
  // 这里暂时返回一个占位对象，实际需要调用 API 查询用户信息
  const otherUserId = session.user1Id === userStore.userInfo.id 
    ? session.user2Id 
    : session.user1Id
  
  return {
    id: otherUserId,
    nickname: `用户${otherUserId}`,
    avatar: null
  }
}

// 当前会话的对方用户
const currentOtherUser = computed(() => {
  const session = sessions.value.find(s => s.id === currentSessionId.value)
  return session ? getOtherUser(session) : null
})

// 获取未读数量（需要后端支持）
const getUnreadCount = (session) => {
  return session.unreadCount || 0
}

// 判断是否是自己发送的消息
const isSentMessage = (message) => {
  return message.senderId === userStore.userInfo?.id
}

// 解析图片JSON
const parseImages = (imagesJson) => {
  if (!imagesJson) return []
  try {
    if (typeof imagesJson === 'string') {
      return JSON.parse(imagesJson)
    }
    return imagesJson
  } catch {
    return []
  }
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
      month: '2-digit',
      day: '2-digit'
    })
  }
}

// 格式化消息时间
const formatMessageTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取会话列表
const fetchSessions = async () => {
  sessionsLoading.value = true
  try {
    const response = await chatApi.getSessionList()
    if (response.code === 200) {
      sessions.value = response.data || []
      // 如果URL中有sessionId参数，自动选中
      const sessionId = route.query.sessionId
      if (sessionId) {
        const session = sessions.value.find(s => s.id === parseInt(sessionId))
        if (session) {
          selectSession(session)
        }
      }
    }
  } catch (error) {
    ElMessage.error('获取会话列表失败')
  } finally {
    sessionsLoading.value = false
  }
}

// 选择会话
const selectSession = async (session) => {
  currentSessionId.value = session.id
  await fetchMessages(session.id)
  // 更新URL但不刷新页面
  router.replace({ query: { sessionId: session.id } })
}

// 获取消息列表
const fetchMessages = async (sessionId) => {
  if (!sessionId) return
  messagesLoading.value = true
  try {
    const response = await chatApi.getMessageList(sessionId)
    if (response.code === 200) {
      messages.value = response.data || []
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    ElMessage.error('获取消息列表失败')
  } finally {
    messagesLoading.value = false
  }
}

// 选择图片
const handleImageSelect = (file) => {
  // 限制图片数量最多9张
  if (selectedImages.value.length >= 9) {
    ElMessage.warning('最多只能选择9张图片')
    return false
  }
  
  // 限制图片大小（5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.raw.size > maxSize) {
    ElMessage.warning('图片大小不能超过5MB')
    return false
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    selectedImages.value.push(e.target.result)
  }
  reader.readAsDataURL(file.raw)
  return false // 阻止自动上传
}

// 移除图片
const removeImage = (index) => {
  selectedImages.value.splice(index, 1)
}

// 上传图片并获取URL
const uploadImages = async () => {
  const uploadedUrls = []
  for (const imageDataUrl of selectedImages.value) {
    try {
      // 将 DataURL 转换为 Blob
      const response = await fetch(imageDataUrl)
      const blob = await response.blob()
      const file = new File([blob], 'image.png', { type: 'image/png' })
      
      const uploadResponse = await fileApi.upload(file, 'chat')
      if (uploadResponse.code === 200) {
        uploadedUrls.push(uploadResponse.data.url || uploadResponse.data)
      }
    } catch (error) {
      console.error('图片上传失败:', error)
      throw new Error('图片上传失败')
    }
  }
  return uploadedUrls
}

// 发送消息
const handleSendMessage = async () => {
  if (!currentSessionId.value) return
  
  const hasText = inputMessage.value.trim().length > 0
  const hasImages = selectedImages.value.length > 0
  
  if (!hasText && !hasImages) {
    ElMessage.warning('请输入消息内容或选择图片')
    return
  }
  
  if (inputMessage.value.length > 500) {
    ElMessage.warning('消息内容不能超过500字')
    return
  }
  
  sending.value = true
  try {
    let imageUrls = []
    
    // 如果有图片，先上传
    if (hasImages) {
      try {
        imageUrls = await uploadImages()
      } catch (error) {
        ElMessage.error(error.message || '图片上传失败')
        sending.value = false
        return
      }
    }
    
    // 发送消息（如果有图片，发送图片消息；如果有文字，发送文字消息；如果都有，发送图片消息并在内容中包含文字）
    let messageType = 'TEXT'
    let content = inputMessage.value.trim()
    let images = null
    
    if (hasImages) {
      messageType = 'IMAGE'
      images = imageUrls
      // 如果有文字，可以作为图片说明
      if (!hasText) {
        content = ''
      }
    }
    
    const response = await chatApi.sendMessage({
      sessionId: currentSessionId.value,
      messageType: messageType,
      content: content,
      images: images
    })
    
    if (response.code === 200) {
      // 清空输入框和图片
      inputMessage.value = ''
      selectedImages.value = []
      
      // 获取新发送的消息ID
      const messageId = response.data?.messageId
      
      // 等待 WebSocket 推送消息（后端会同时推送给发送者和接收者）
      // 如果 WebSocket 没有及时推送，延迟后重新获取消息列表
      setTimeout(async () => {
        // 检查消息是否已通过 WebSocket 添加到列表
        const messageExists = messages.value.find(m => m.id === messageId)
        if (!messageExists) {
          // 如果 WebSocket 没有推送，重新获取消息列表
          await fetchMessages(currentSessionId.value)
        }
      }, 500)
      
      // 更新会话列表（更新最后消息时间和内容）
      await fetchSessions()
    }
  } catch (error) {
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// WebSocket消息处理
const handleChatMessage = (message) => {
  // 检查消息是否已存在（避免重复）
  const exists = messages.value.find(m => m.id === message.id)
  
  // 如果是当前会话的消息
  if (message.sessionId === currentSessionId.value) {
    // 如果消息不存在，添加到消息列表
    if (!exists) {
      messages.value.push(message)
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      // 如果消息已存在（可能是自己发送的临时消息），更新为完整消息
      const index = messages.value.findIndex(m => m.id === message.id || (m.id === message.id && m.senderId === message.senderId))
      if (index !== -1) {
        messages.value[index] = message
      }
    }
  } else {
    // 其他会话的新消息
    const session = sessions.value.find(s => s.id === message.sessionId)
    if (session && message.senderId !== userStore.userInfo?.id) {
      // 只对接收到的消息显示通知，不显示自己发送的
      ElMessage.info({
        message: `收到来自 ${session.otherUser?.nickname || '用户'} 的新消息`,
        duration: 3000,
        showClose: true
      })
    }
  }
  
  // 更新会话列表（刷新未读数量等）
  fetchSessions()
}

// 监听会话ID变化
watch(currentSessionId, (newId) => {
  if (newId) {
    fetchMessages(newId)
  }
})

// 监听 WebSocket 连接状态，自动订阅/重新订阅聊天消息
watchEffect(() => {
  if (wsManager.isConnected && userStore.isLoggedIn && !chatSubscription.value) {
    // WebSocket 已连接且用户已登录，但未订阅，则订阅
    chatSubscription.value = wsManager.subscribeChatMessages(handleChatMessage)
  } else if (!wsManager.isConnected && chatSubscription.value) {
    // WebSocket 断开，清理订阅
    chatSubscription.value = null
  }
})

onMounted(async () => {
  await fetchSessions()
  
  // 连接WebSocket
  const token = getToken()
  if (token && userStore.isLoggedIn) {
    // 如果 WebSocket 未连接，则连接
    if (!wsManager.isConnected) {
      wsManager.connect(token)
    } else {
      // 如果已连接，立即订阅
      chatSubscription.value = wsManager.subscribeChatMessages(handleChatMessage)
    }
  }
})

onUnmounted(() => {
  // 取消订阅聊天消息
  if (chatSubscription.value) {
    wsManager.unsubscribeChatMessages()
    chatSubscription.value = null
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - var(--header-height));
  background-color: var(--color-bg-primary);
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid var(--color-border);
  background-color: var(--color-bg-white);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-primary);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.empty-sessions {
  padding: 40px 20px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
}

.session-item:hover {
  background-color: var(--color-bg-secondary);
}

.session-item.active {
  background-color: var(--color-primary-light);
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.session-preview {
  font-size: 12px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.session-time {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-white);
}

.no-session {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.user-status {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-messages {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-item.message-sent {
  flex-direction: row-reverse;
}

.message-content-wrapper {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.message-item.message-sent .message-content-wrapper {
  align-items: flex-end;
}

.message-item.message-received .message-content-wrapper {
  align-items: flex-start;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  word-wrap: break-word;
}

.bubble-sent {
  background-color: var(--color-primary);
  color: white;
}

.bubble-received {
  background-color: var(--color-bg-secondary);
  color: var(--color-text-primary);
}

.message-text {
  line-height: 1.5;
}

.message-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.message-image {
  width: 200px;
  height: 200px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 8px;
  margin-bottom: 8px;
}

.image-error-small {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F5F5F5;
  color: #909399;
  font-size: 12px;
  border-radius: 4px;
}

.message-image-caption {
  margin-top: 8px;
  font-size: 13px;
  color: var(--color-text-regular);
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-top: 4px;
  padding: 0 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.read-status {
  color: #67c23a;
  font-size: 10px;
}

.read-status.unread {
  color: #909399;
}

.message-avatar {
  flex-shrink: 0;
}

.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}

.input-toolbar {
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border);
}

.image-preview-list {
  display: flex;
  gap: 8px;
  padding: 8px;
  flex-wrap: wrap;
  border-bottom: 1px solid var(--color-border);
}

.image-preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.preview-image {
  width: 100%;
  height: 100%;
}

.remove-image {
  position: absolute;
  top: 4px;
  right: 4px;
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 50%;
  padding: 4px;
  font-size: 14px;
}

.remove-image:hover {
  background-color: rgba(0, 0, 0, 0.7);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  gap: 8px;
}

.input-tip {
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>

