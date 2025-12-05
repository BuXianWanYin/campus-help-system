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

        <!-- 订单卡片（仅在GOODS类型会话且存在订单时显示，紧凑型横向布局） -->
        <div v-if="currentOrder" class="order-card-wrapper">
          <OrderCard :order="currentOrder" @update-order="handleOrderUpdate" @send-order="handleSendOrder" />
        </div>

        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0 && !messagesLoading" class="empty-messages">
            <el-empty description="暂无消息，开始聊天吧" />
          </div>
          <transition-group
            name="message-list"
            tag="div"
            class="message-list-wrapper"
          >
            <div
              v-for="message in messages"
              :key="`${currentSessionId}-${message.id}`"
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
                  <div class="message-images-container">
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
                  </div>
                  <div v-if="message.content" class="message-image-caption">{{ message.content }}</div>
                </div>
                <div v-else-if="message.messageType === 'GOODS_CARD'" class="message-goods-card">
                  <GoodsCardMessage :message="message" />
                </div>
                <div v-else-if="message.messageType === 'ORDER_CARD'" class="message-order-card">
                  <OrderCard :order="parseOrderFromMessage(message)" :is-in-message="true" />
                </div>
              </div>
              <div class="message-time">
                {{ formatMessageTime(message.createTime) }}
                <span v-if="isSentMessage(message) && message.isRead === 1" class="read-status">
                  <el-icon class="read-icon"><CircleCheck /></el-icon>
                  <span class="read-text">已读</span>
                </span>
                <span v-else-if="isSentMessage(message)" class="read-status unread">
                  <el-icon class="read-icon unread-icon"><CircleCheck /></el-icon>
                </span>
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
          </transition-group>
        </div>

        <!-- 输入框 -->
        <div class="chat-input-area">
          <div v-if="selectedImages.length > 0" class="image-preview-list">
            <div v-for="(img, index) in selectedImages" :key="index" class="image-preview-item">
              <el-image :src="img" fit="cover" class="preview-image" />
              <el-icon class="remove-image" @click="removeImage(index)"><Close /></el-icon>
            </div>
          </div>
          <div class="input-wrapper">
            <textarea
              v-model="inputMessage"
              class="chat-textarea"
              :placeholder="inputMessage.trim() === '' ? '输入消息... (Shift+Enter换行, Enter发送)' : ''"
              :maxlength="500"
              @keydown="handleKeydown"
              @input="handleInput"
            ></textarea>
            <div class="input-actions">
              <div
                v-if="canSendGoodsCard"
                class="input-icon goods-card-icon"
                title="发送商品卡片"
                @click="handleSendGoodsCard"
              >
                <el-icon :size="21"><ShoppingBag /></el-icon>
              </div>
              <el-upload
                :action="''"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleImageSelect"
                accept="image/*"
                :multiple="true"
              >
                <template #trigger>
                  <div class="input-icon image-icon" title="上传图片">
                    <el-icon :size="21"><Picture /></el-icon>
                  </div>
                </template>
              </el-upload>
              <div 
                class="input-icon send-icon" 
                :class="{ 'send-disabled': !canSend }"
                title="发送消息"
                @click="handleSendMessage"
              >
                <el-icon :size="21"><Promotion /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 商品选择对话框 -->
    <el-dialog
      v-model="goodsSelectDialogVisible"
      title="选择商品"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-loading="goodsListLoading" class="goods-select-content">
        <div v-if="selectableGoods.length === 0 && !goodsListLoading" class="empty-goods">
          <el-empty description="暂无在售商品" />
        </div>
        <div v-else class="goods-list">
          <div
            v-for="goods in selectableGoods"
            :key="goods.id"
            class="goods-item"
            :class="{ selected: selectedGoodsId === goods.id }"
            @click="selectedGoodsId = goods.id"
          >
            <el-image
              :src="getFirstGoodsImage(goods.images)"
              fit="cover"
              class="goods-item-image"
            >
              <template #error>
                <div class="image-error">图片加载失败</div>
              </template>
            </el-image>
            <div class="goods-item-info">
              <div class="goods-item-title">{{ goods.title }}</div>
              <div class="goods-item-price">¥{{ goods.currentPrice }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="goodsSelectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSendGoodsCard" :disabled="!selectedGoodsId">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Close, Promotion, CircleCheck, ShoppingBag } from '@element-plus/icons-vue'
import { chatApi, fileApi, messageApi, orderApi, goodsApi } from '@/api'
import OrderCard from './components/OrderCard.vue'
import GoodsCardMessage from './components/GoodsCardMessage.vue'
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
// 消息缓存，按会话ID存储消息，避免切换时闪烁
const messagesCache = ref(new Map())
const sending = ref(false)
const inputMessage = ref('')
const messagesContainer = ref(null)
const selectedImages = ref([])
const chatSubscription = ref(null)
const currentOrder = ref(null)
const orderLoading = ref(false)
const goodsSelectDialogVisible = ref(false)
const selectableGoods = ref([])
const goodsListLoading = ref(false)
const selectedGoodsId = ref(null)

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

// 当前会话信息
const currentSession = computed(() => {
  return sessions.value.find(s => s.id === currentSessionId.value)
})

// 是否可以发送商品卡片（任何会话都可以发送，但需要知道对方用户ID）
const canSendGoodsCard = computed(() => {
  return !!currentOtherUser.value?.id
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
const fetchSessions = async (autoSelectFromUrl = false, forceRefresh = false) => {
  // 如果正在加载且不是强制刷新，避免重复请求
  if (sessionsLoading.value && !forceRefresh) {
    return
  }
  
  sessionsLoading.value = true
  try {
    const response = await chatApi.getSessionList()
    if (response.code === 200) {
      sessions.value = response.data || []
      
      
      // 如果URL中有sessionId参数且需要自动选中，自动选中
      if (autoSelectFromUrl) {
        const sessionId = route.query.sessionId
        if (sessionId && !currentSessionId.value) {
          const session = sessions.value.find(s => s.id === parseInt(sessionId))
          if (session) {
            // 立即更新本地会话的未读数量为0
            session.unreadCount = 0
            // 直接设置会话ID并加载消息，避免触发 watch 和 selectSession 的循环
            currentSessionId.value = session.id
            await fetchMessages(session.id, false)
            // 如果是商品类型会话，获取订单信息
            if (session.relatedType === 'GOODS' && session.id) {
              fetchOrderBySessionId(session.id)
            }
            // 标记系统消息为已读
            try {
              await messageApi.markChatMessagesAsRead(session.id)
            } catch (error) {
              console.error('标记聊天系统消息为已读失败:', error)
            }
            // 确保滚动到底部（fetchMessages 中已经调用了，这里再次确保）
            scrollToBottom(true)
          } else {
            console.warn('未找到会话:', sessionId, '所有会话:', sessions.value.map(s => s.id))
          }
        }
      }
    } else {
      console.error('获取会话列表失败，响应码:', response.code, response.message)
      ElMessage.error(response.message || '获取会话列表失败')
    }
  } catch (error) {
    console.error('获取会话列表异常:', error)
    ElMessage.error(error.message || '获取会话列表失败')
  } finally {
    sessionsLoading.value = false
  }
}

// 选择会话
const selectSession = async (session) => {
  // 如果选择的会话就是当前会话，不需要重复加载
  if (currentSessionId.value === session.id) {
    return
  }
  
  // 保存当前会话的消息到缓存
  if (currentSessionId.value) {
    messagesCache.value.set(currentSessionId.value, [...messages.value])
  }
  
  // 先更新会话ID
  currentSessionId.value = session.id
  
  // 立即从缓存中恢复消息（如果有），避免空白闪烁
  if (messagesCache.value.has(session.id)) {
    messages.value = messagesCache.value.get(session.id)
  } else {
    // 如果缓存中没有，先清空消息（避免显示上一个会话的消息）
    messages.value = []
  }
  
  // 立即更新本地会话的未读数量为0（优化用户体验，避免延迟显示）
  const targetSession = sessions.value.find(s => s.id === session.id)
  if (targetSession) {
    targetSession.unreadCount = 0
  }
  
  // 如果是商品类型会话，尝试获取关联订单
  if (session.relatedType === 'GOODS' && session.id) {
    fetchOrderBySessionId(session.id)
  } else {
    currentOrder.value = null
  }
  
  // 异步加载最新消息（不显示加载状态，避免闪烁）
  // fetchMessages 中已经会滚动到底部，这里不需要额外处理
  fetchMessages(session.id, false)
  
  // 标记该会话相关的系统消息为已读
  try {
    await messageApi.markChatMessagesAsRead(session.id)
  } catch (error) {
    console.error('标记聊天系统消息为已读失败:', error)
  }
  
  // 更新URL但不刷新页面
  router.replace({ query: { sessionId: session.id } })
  
  // 使用防抖更新会话列表以更新未读数量（从服务器获取最新数据）
  debouncedUpdateSessions()
}

// 获取消息列表
const fetchMessages = async (sessionId, showLoading = true) => {
  if (!sessionId) return
  // 只有在需要显示加载状态时才显示（切换会话时不显示，避免闪烁）
  if (showLoading) {
    messagesLoading.value = true
  }
  try {
    const response = await chatApi.getMessageList(sessionId)
    if (response.code === 200) {
      // 只有在获取的消息属于当前会话时才更新（避免异步请求导致的错误更新）
      if (currentSessionId.value === sessionId) {
        const newMessages = response.data || []
        // 更新消息列表
        messages.value = newMessages
        // 更新缓存
        messagesCache.value.set(sessionId, [...newMessages])
        // 加载消息后，确保当前会话的未读数量为0（后端会标记消息为已读）
        const targetSession = sessions.value.find(s => s.id === sessionId)
        if (targetSession) {
          targetSession.unreadCount = 0
        }
        // 消息加载完成后，滚动到底部
        scrollToBottom(true)
      }
    }
  } catch (error) {
    ElMessage.error('获取消息列表失败')
  } finally {
    if (showLoading) {
      messagesLoading.value = false
    }
  }
}

// 定期检查已读状态（用于实时更新）
let readStatusCheckTimer = null
const checkReadStatus = async () => {
  if (!currentSessionId.value) return
  
  try {
    const response = await chatApi.getMessageList(currentSessionId.value)
    if (response.code === 200) {
      const newMessages = response.data || []
      // 更新已读状态
      newMessages.forEach(newMsg => {
        const existingIndex = messages.value.findIndex(m => m.id === newMsg.id)
        if (existingIndex !== -1 && messages.value[existingIndex].isRead !== newMsg.isRead) {
          // 只更新isRead状态，避免影响其他响应式数据
          messages.value[existingIndex].isRead = newMsg.isRead
        }
      })
    }
  } catch (error) {
    // 静默失败，不影响用户体验
    console.error('检查已读状态失败:', error)
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

// 判断是否可以发送
const canSend = computed(() => {
  return inputMessage.value.trim().length > 0 || selectedImages.value.length > 0
})

// 处理键盘事件
const handleKeydown = (event) => {
  // Shift+Enter: 换行
  if (event.shiftKey && event.key === 'Enter') {
    return // 允许默认行为（换行）
  }
  // Enter: 发送消息
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    if (canSend.value) {
      handleSendMessage()
    }
  }
}

// 处理输入事件
const handleInput = (event) => {
  // 自动调整高度
  const textarea = event.target
  textarea.style.height = 'auto'
  textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px'
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
      // 获取新发送的消息ID
      const messageId = response.data?.messageId
      
      // 立即创建临时消息对象显示在界面上（优化用户体验）
      const tempMessage = {
        id: messageId || Date.now(), // 使用后端返回的ID或临时ID
        sessionId: currentSessionId.value,
        senderId: userStore.userInfo.id,
        receiverId: currentOtherUser.value?.id,
        messageType: messageType,
        content: content,
        images: images ? JSON.stringify(images) : null,
        isRead: 0,
        createTime: new Date().toISOString()
      }
      
      // 检查消息是否已存在（避免重复）
      const exists = messages.value.find(m => m.id === messageId || (m.id === tempMessage.id && m.senderId === tempMessage.senderId))
      if (!exists) {
        // 添加到消息列表（立即显示）
        messages.value.push(tempMessage)
        // 更新缓存
        if (messagesCache.value.has(currentSessionId.value)) {
          const cachedMessages = messagesCache.value.get(currentSessionId.value)
          cachedMessages.push(tempMessage)
          messagesCache.value.set(currentSessionId.value, cachedMessages)
        } else {
          messagesCache.value.set(currentSessionId.value, [...messages.value])
        }
        nextTick(() => {
          scrollToBottom()
        })
      }
      
      // 清空输入框和图片
      inputMessage.value = ''
      selectedImages.value = []
      
      // 更新会话列表（更新最后消息时间和内容）- 使用防抖
      debouncedUpdateSessions()
      
      // 不需要延迟重新获取消息，WebSocket 会推送消息并更新临时消息
    }
  } catch (error) {
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = (force = false) => {
  if (messagesContainer.value) {
    // 使用 nextTick 确保 DOM 更新完成
    nextTick(() => {
      // 使用 setTimeout 确保渲染完成后再滚动
      setTimeout(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      }, force ? 0 : 100)
    })
  }
}

// 防抖更新会话列表
let sessionUpdateTimer = null
const debouncedUpdateSessions = () => {
  if (sessionUpdateTimer) {
    clearTimeout(sessionUpdateTimer)
  }
  sessionUpdateTimer = setTimeout(async () => {
    await fetchSessions()
    // 更新会话列表后，确保当前会话的未读数量为0（用户正在查看）
    if (currentSessionId.value) {
      const targetSession = sessions.value.find(s => s.id === currentSessionId.value)
      if (targetSession) {
        targetSession.unreadCount = 0
      }
    }
    sessionUpdateTimer = null
  }, 500) // 500ms 防抖
}

// 防重复通知机制：使用会话ID+消息ID的组合键
const notificationCache = new Map()
const NOTIFICATION_CACHE_DURATION = 5000 // 5秒内同一消息不重复显示

const debouncedShowNotification = (sessionName, sessionId, messageId) => {
  // 使用会话ID+消息ID作为唯一键
  const cacheKey = `${sessionId}-${messageId}`
  
  // 检查是否在缓存期内
  const cached = notificationCache.get(cacheKey)
  const now = Date.now()
  
  if (cached && (now - cached) < NOTIFICATION_CACHE_DURATION) {
    // 在缓存期内，不重复显示
    return
  }
  
  // 记录当前时间
  notificationCache.set(cacheKey, now)
  
  // 显示通知
  ElMessage.success({
    message: `收到来自 ${sessionName || '用户'} 的新消息`,
    duration: 3000,
    showClose: true
  })
  
  // 清理过期的缓存（每10条消息清理一次，避免内存泄漏）
  if (notificationCache.size > 100) {
    const expiredKeys = []
    notificationCache.forEach((timestamp, key) => {
      if (now - timestamp > NOTIFICATION_CACHE_DURATION) {
        expiredKeys.push(key)
      }
    })
    expiredKeys.forEach(key => notificationCache.delete(key))
  }
}

// WebSocket消息处理
const handleChatMessage = (message) => {
  // 检查消息是否已存在（避免重复）
  const exists = messages.value.find(m => m.id === message.id)
  
  // 如果是当前会话的消息
  if (message.sessionId === currentSessionId.value) {
    // 当前会话的消息，确保未读数量为0（用户正在查看）
    const targetSession = sessions.value.find(s => s.id === currentSessionId.value)
    if (targetSession) {
      targetSession.unreadCount = 0
    }
    
    // 如果消息不存在，添加到消息列表
    if (!exists) {
      messages.value.push(message)
      // 更新缓存
      if (messagesCache.value.has(currentSessionId.value)) {
        const cachedMessages = messagesCache.value.get(currentSessionId.value)
        cachedMessages.push(message)
        messagesCache.value.set(currentSessionId.value, cachedMessages)
      } else {
        messagesCache.value.set(currentSessionId.value, [...messages.value])
      }
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      // 如果消息已存在（可能是自己发送的临时消息），更新为完整消息
      const index = messages.value.findIndex(m => m.id === message.id)
      if (index !== -1) {
        // 保留原有的消息对象，只更新关键字段（特别是isRead状态）
        const existingMessage = messages.value[index]
        messages.value[index] = {
          ...existingMessage,
          ...message,
          // 确保isRead状态实时更新
          isRead: message.isRead !== undefined ? message.isRead : existingMessage.isRead
        }
        // 更新缓存
        if (messagesCache.value.has(currentSessionId.value)) {
          const cachedMessages = messagesCache.value.get(currentSessionId.value)
          const cachedIndex = cachedMessages.findIndex(m => m.id === message.id)
          if (cachedIndex !== -1) {
            cachedMessages[cachedIndex] = messages.value[index]
            messagesCache.value.set(currentSessionId.value, cachedMessages)
          }
        }
        nextTick(() => {
          scrollToBottom()
        })
      }
    }
  } else {
    // 其他会话的新消息
    const session = sessions.value.find(s => s.id === message.sessionId)
    if (session && message.senderId !== userStore.userInfo?.id) {
      // 只对接收到的消息显示通知，不显示自己发送的（使用防重复机制）
      debouncedShowNotification(
        session.otherUser?.nickname || '用户',
        message.sessionId,
        message.id
      )
    }
  }
  
  // 检查是否有已读状态更新（用于更新自己发送的消息的已读状态）
  if (message.senderId === userStore.userInfo?.id && message.isRead !== undefined) {
    // 更新所有相同ID的消息的已读状态
    messages.value.forEach((msg, index) => {
      if (msg.id === message.id && msg.senderId === message.senderId) {
        messages.value[index] = {
          ...msg,
          isRead: message.isRead
        }
      }
    })
  }
  
  // 使用防抖更新会话列表（避免频繁请求）
  debouncedUpdateSessions()
}

/**
 * 根据会话ID获取订单信息
 */
const fetchOrderBySessionId = async (sessionId) => {
  if (!sessionId) {
    currentOrder.value = null
    return
  }
  
  orderLoading.value = true
  try {
    const response = await orderApi.getBySessionId(sessionId)
    if (response.code === 200 && response.data) {
      currentOrder.value = response.data
    } else {
      currentOrder.value = null
    }
  } catch (error) {
    // 订单不存在或无权查看，不显示订单卡片
    currentOrder.value = null
  } finally {
    orderLoading.value = false
  }
}

/**
 * 处理订单更新
 */
const handleOrderUpdate = () => {
  // 重新获取订单信息
  if (currentSessionId.value) {
    fetchOrderBySessionId(currentSessionId.value)
  }
}

/**
 * 处理发送订单
 */
const handleSendOrder = async (order) => {
  if (!currentSessionId.value || !order) {
    return
  }
  
  try {
    // 将订单信息作为消息内容发送（使用JSON格式）
    const orderData = {
      orderId: order.id,
      orderNo: order.orderNo,
      goodsId: order.goodsId,
      goodsTitle: order.goods?.title,
      quantity: order.quantity,
      price: order.price,
      totalAmount: order.totalAmount,
      shippingFee: order.shippingFee,
      status: order.status,
      tradeMethod: order.tradeMethod
    }
    
    const response = await chatApi.sendMessage({
      sessionId: currentSessionId.value,
      messageType: 'ORDER_CARD',
      content: JSON.stringify(orderData)
    })
    
    if (response.code === 200) {
      ElMessage.success('订单已发送')
      // 刷新消息列表
      await fetchMessages(currentSessionId.value)
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error(error.message || '发送订单失败')
  }
}

/**
 * 从消息内容解析订单信息
 */
const parseOrderFromMessage = (message) => {
  if (!message || !message.content) {
    return null
  }
  
  try {
    const orderData = JSON.parse(message.content)
    // 构造一个简化的订单对象用于显示
    return {
      id: orderData.orderId,
      orderNo: orderData.orderNo,
      goodsId: orderData.goodsId,
      goods: {
        title: orderData.goodsTitle,
        images: null // 可以从goodsId获取，这里简化处理
      },
      quantity: orderData.quantity,
      price: orderData.price,
      totalAmount: orderData.totalAmount,
      shippingFee: orderData.shippingFee || 0,
      status: orderData.status,
      tradeMethod: orderData.tradeMethod,
      buyerId: null, // 消息中不包含，需要从当前用户判断
      sellerId: null
    }
  } catch (e) {
    console.error('解析订单消息失败:', e)
    return null
  }
}

/**
 * 获取第一张商品图片
 */
const getFirstGoodsImage = (imagesJson) => {
  if (!imagesJson) return ''
  try {
    const images = typeof imagesJson === 'string' ? JSON.parse(imagesJson) : imagesJson
    if (Array.isArray(images) && images.length > 0) {
      return getAvatarUrl(images[0])
    }
  } catch (e) {
    console.error('解析商品图片失败:', e)
  }
  return ''
}

/**
 * 打开商品选择对话框
 */
const handleSendGoodsCard = async () => {
  if (!currentSessionId.value || !canSendGoodsCard.value || sending.value) {
    return
  }
  
  // 获取对方用户ID
  const otherUserId = currentOtherUser.value?.id
  if (!otherUserId) {
    ElMessage.warning('无法获取对方用户信息')
    return
  }
  
  // 打开对话框并加载商品列表
  goodsSelectDialogVisible.value = true
  selectedGoodsId.value = null
  await fetchSelectableGoods(otherUserId)
}

/**
 * 获取可选商品列表（对方用户发布的在售商品）
 */
const fetchSelectableGoods = async (userId) => {
  goodsListLoading.value = true
  try {
    const response = await goodsApi.getList({
      userId: userId,
      status: 'ON_SALE',
      pageNum: 1,
      pageSize: 50
    })
    
    if (response.code === 200) {
      selectableGoods.value = response.data?.records || []
    } else {
      ElMessage.error(response.message || '获取商品列表失败')
      selectableGoods.value = []
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    selectableGoods.value = []
  } finally {
    goodsListLoading.value = false
  }
}

/**
 * 确认发送商品卡片
 */
const confirmSendGoodsCard = async () => {
  if (!selectedGoodsId.value || !currentSessionId.value || sending.value) {
    return
  }
  
  sending.value = true
  try {
    // 发送商品卡片消息，content存储商品ID的JSON
    const goodsCardContent = JSON.stringify({ goodsId: selectedGoodsId.value })
    
    const response = await chatApi.sendMessage({
      sessionId: currentSessionId.value,
      messageType: 'GOODS_CARD',
      content: goodsCardContent,
      images: null
    })
    
    if (response.code === 200) {
      // 获取新发送的消息ID
      const messageId = response.data?.messageId || response.data
      
      // 检查消息是否已存在（避免重复添加临时消息）
      const exists = messages.value.find(m => m.id === messageId)
      if (!exists && messageId) {
        // 立即创建临时消息对象显示在界面上（优化用户体验）
        const tempMessage = {
          id: messageId,
          sessionId: currentSessionId.value,
          senderId: userStore.userInfo.id,
          receiverId: currentOtherUser.value?.id,
          messageType: 'GOODS_CARD',
          content: goodsCardContent,
          images: null,
          isRead: 0,
          createTime: new Date().toISOString()
        }
        
        messages.value.push(tempMessage)
        
        // 滚动到底部
        nextTick(() => {
          scrollToBottom()
        })
      }
      
      // 关闭对话框
      goodsSelectDialogVisible.value = false
      selectedGoodsId.value = null
      
      // 清空输入框
      inputMessage.value = ''
      selectedImages.value = []
      
      // 更新会话列表（更新最后消息时间和内容）
      debouncedUpdateSessions()
    } else {
      ElMessage.error(response.message || '发送失败')
    }
  } catch (error) {
    console.error('发送商品卡片失败:', error)
    ElMessage.error(error.response?.data?.message || '发送商品卡片失败')
  } finally {
    sending.value = false
  }
}

// 监听会话ID变化（避免重复调用）
watch(currentSessionId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    // 不在这里调用fetchMessages，因为selectSession已经调用了
    // 只在URL参数变化时调用（比如直接通过URL访问）
    if (!oldId) {
      fetchMessages(newId, false).then(() => {
        // 确保滚动到底部
        scrollToBottom(true)
      })
    }
    // 如果是商品类型会话，获取订单信息
    const session = sessions.value.find(s => s.id === newId)
    if (session && session.relatedType === 'GOODS') {
      fetchOrderBySessionId(newId)
    } else {
      currentOrder.value = null
    }
  }
})

// 监听 WebSocket 连接状态，自动订阅/重新订阅聊天消息
watchEffect(() => {
  // 只在连接状态或登录状态真正变化时才处理
  const isConnected = wsManager.isConnected
  const isLoggedIn = userStore.isLoggedIn
  const hasSubscription = !!chatSubscription.value
  
  if (isConnected && isLoggedIn && !hasSubscription) {
    // WebSocket 已连接且用户已登录，但未订阅，则订阅
    chatSubscription.value = wsManager.subscribeChatMessages(handleChatMessage)
  } else if ((!isConnected || !isLoggedIn) && hasSubscription) {
    // WebSocket 断开或未登录，清理订阅
    if (chatSubscription.value) {
      wsManager.unsubscribeChatMessages()
      chatSubscription.value = null
    }
  }
})

// 初始化聊天页面
const initChat = async () => {
  // 确保用户已登录
  if (!userStore.isLoggedIn) {
    console.warn('用户未登录，无法加载聊天会话')
    return
  }
  
  // 总是加载会话列表（无论是否有会话），强制刷新
  await fetchSessions(true, true)
  
  // 连接WebSocket
  const token = getToken()
  if (token) {
    // 如果 WebSocket 未连接，则连接
    if (!wsManager.isConnected) {
      wsManager.connect(token)
    } else {
      // 如果已连接，立即订阅
      chatSubscription.value = wsManager.subscribeChatMessages(handleChatMessage)
    }
  }
}

onMounted(async () => {
  // 初始化聊天页面，加载历史会话
  await initChat()
  
  // 启动定期检查已读状态（每5秒检查一次）
  readStatusCheckTimer = setInterval(() => {
    if (currentSessionId.value) {
      checkReadStatus()
    }
  }, 5000)
})

// 监听路由变化，如果路由变化到聊天页面，确保加载会话列表
watch(() => route.path, (newPath, oldPath) => {
  if (newPath === '/user/chat' && oldPath !== '/user/chat') {
    // 路由变化到聊天页面，如果会话列表为空且未在加载中，则加载
    if (sessions.value.length === 0 && !sessionsLoading.value) {
      console.log('路由变化到聊天页面，重新加载会话列表')
      fetchSessions(true)
    }
  }
}, { immediate: false })

onUnmounted(() => {
  // 清理防抖定时器
  if (sessionUpdateTimer) {
    clearTimeout(sessionUpdateTimer)
    sessionUpdateTimer = null
  }
  
  // 清理通知定时器（如果存在）
  // notificationTimer 可能在某些情况下未定义，所以需要检查
  
  // 清理已读状态检查定时器
  if (readStatusCheckTimer) {
    clearInterval(readStatusCheckTimer)
    readStatusCheckTimer = null
  }
  
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
  height: calc(95vh - var(--header-height));
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
  min-height: 0; /* 确保 flex 子元素可以缩小 */
  overflow: hidden; /* 防止整体溢出 */
}

.no-session {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 确保 flex 子元素可以缩小 */
  overflow: hidden; /* 防止整体溢出 */
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0; /* 头部不缩小 */
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
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0; /* 确保 flex 子元素可以缩小 */
  max-height: calc(100vh - 280px); /* 降低聊天框高度 */
  /* 隐藏滚动条但保持滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 和 Edge */
}

.message-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 消息列表过渡动画（平滑切换） */
.message-list-enter-active,
.message-list-leave-active {
  transition: opacity 0.15s ease;
}

.message-list-enter-from,
.message-list-leave-to {
  opacity: 0;
}

/* 隐藏 WebKit 浏览器的滚动条 */
.messages-container::-webkit-scrollbar {
  display: none;
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
  justify-content: flex-end;
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

/* 图片消息的气泡只需要少量内边距，让图片和文字自然排列 */
.message-bubble:has(.message-images) {
  padding: 8px;
}

.bubble-sent {
  background-color: var(--color-primary);
  color: white;
}

.bubble-received {
  background-color: #f0f0f0;
  color: var(--color-text-primary);
  border: 1px solid #e0e0e0;
}

.message-text {
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-images {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.message-images-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.message-image {
  width: 200px;
  height: 200px;
  border-radius: 4px;
  cursor: pointer;
  flex-shrink: 0;
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
  margin-top: 0;
  padding: 0;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 发送的图片消息文字颜色（白色，因为气泡是蓝色的） */
.message-item.message-sent .message-image-caption {
  color: #ffffff;
}

/* 接收的图片消息文字颜色 */
.message-item.message-received .message-image-caption {
  color: var(--color-text-primary);
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
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: #67c23a;
  font-size: 12px;
}

.read-icon {
  font-size: 14px;
}

.read-text {
  font-size: 11px;
}

.read-status.unread {
  color: #909399;
}

.read-status.unread .unread-icon {
  color: #909399;
}

.message-avatar {
  flex-shrink: 0;
}

.chat-input-area {
  padding: 12px 20px;
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0; /* 输入区域不缩小 */
  background-color: var(--color-bg-white);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background-color: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 8px 12px;
  min-height: 40px;
}

.chat-textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  padding: 0;
  padding-right: 12px;
  max-height: 150px;
  overflow-y: auto;
  font-family: inherit;
  color: var(--color-text-primary);
  background: transparent;
}

.chat-textarea::placeholder {
  color: var(--color-text-placeholder);
}

.chat-textarea::-webkit-scrollbar {
  width: 6px;
}

.chat-textarea::-webkit-scrollbar-track {
  background: transparent;
}

.chat-textarea::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.chat-textarea::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  flex-wrap: nowrap;
  height: 23px;
}

.input-icon {
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: color 0.2s;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 0;
  width: 23px;
  height: 23px;
  flex-shrink: 0;
}

.input-icon .el-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  width: 23px;
  height: 23px;
}

/* 确保 el-upload 内的图标也对齐 */
.input-actions :deep(.el-upload) {
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 0;
  height: 23px;
  width: 23px;
}

.input-actions :deep(.el-upload .input-icon) {
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 0;
  width: 23px;
  height: 23px;
}

.input-icon:hover {
  color: var(--color-primary);
}

.send-icon {
  color: var(--color-primary);
}

.send-icon.send-disabled {
  color: var(--color-text-placeholder);
  cursor: not-allowed;
}

.send-icon.send-disabled:hover {
  color: var(--color-text-placeholder);
}

.goods-card-icon {
  color: var(--color-primary);
}

.goods-card-icon:hover {
  color: var(--color-primary);
  opacity: 0.8;
}

.message-goods-card {
  max-width: 100%;
}

.image-preview-list {
  display: flex;
  gap: 8px;
  padding: 8px;
  flex-wrap: wrap;
  border-bottom: 1px solid var(--color-border);
  max-height: 200px; /* 限制最大高度 */
  overflow-y: auto; /* 如果图片太多可以滚动 */
  flex-shrink: 0; /* 不缩小 */
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

.order-card-wrapper {
  padding: 0 20px;
  margin-bottom: 8px;
  width: 100%;
  box-sizing: border-box;
}

.message-order-card {
  max-width: 100%;
}

.message-order-card :deep(.order-card-compact) {
  margin-bottom: 0;
}

.goods-select-content {
  max-height: 500px;
  overflow-y: auto;
}

.goods-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  padding: 8px 0;
}

.goods-item {
  display: flex;
  flex-direction: column;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #fff;
}

.goods-item:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.goods-item.selected {
  border-color: var(--color-primary);
  background-color: #f0f9ff;
}

.goods-item-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.goods-item-info {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.goods-item-title {
  font-size: 13px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.goods-item-price {
  font-size: 16px;
  font-weight: bold;
  color: var(--color-danger);
}

.empty-goods {
  padding: 40px 0;
  text-align: center;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #f5f5f5;
  color: #999;
  font-size: 12px;
}

</style>

