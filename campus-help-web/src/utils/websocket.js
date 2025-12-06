/**
 * WebSocket工具类（STOMP协议）
 * 用于管理WebSocket连接、重连、消息接收
 */

import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

class WebSocketManager {
  constructor() {
    this.stompClient = null
    this.socket = null
    this.reconnectTimer = null
    this.systemMessageHandlers = []
    this.chatMessageHandlers = []
    this.subscriptions = []
    this.chatSubscription = null
    this.isConnected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    
    // 使用环境变量中的WebSocket URL
    // SockJS需要HTTP URL（会自动转换为WebSocket）
    const wsUrl = import.meta.env.VITE_WS_URL
    if (wsUrl.startsWith('ws://') || wsUrl.startsWith('wss://')) {
      // 如果是WebSocket协议，转换为HTTP协议并移除/ws
      this.baseUrl = wsUrl.replace(/^ws:\/\//, 'http://').replace(/^wss:\/\//, 'https://').replace(/\/ws$/, '')
    } else {
      // 如果已经是HTTP协议，直接使用（移除/ws后缀，如果存在）
      this.baseUrl = wsUrl.replace(/\/ws$/, '')
    }
  }
  
  /**
   * 连接WebSocket
   * @param {string} token - JWT Token
   */
  connect(token) {
    if (this.stompClient && this.stompClient.connected) {
      console.log('WebSocket已连接，无需重复连接')
      return
    }
    
    try {
      // 创建SockJS连接
      // SockJS会自动处理协议转换（http -> ws），所以直接使用baseUrl + /ws
      const wsUrl = `${this.baseUrl}/ws`
      console.log('正在连接WebSocket:', wsUrl)
      this.socket = new SockJS(wsUrl)
      
      // 检查Stomp是否正确导入
      if (!Stomp || typeof Stomp.over !== 'function') {
        throw new Error('STOMP客户端未正确加载，请检查导入')
      }
      this.stompClient = Stomp.over(this.socket)
      
      // 禁用调试日志（生产环境）
      if (import.meta.env.PROD) {
        this.stompClient.debug = () => {}
      }
      
      // 连接配置
      const headers = {
        Authorization: `Bearer ${token}`
      }
      
      // 建立连接
      this.stompClient.connect(
        headers,
        () => {
          console.log('WebSocket STOMP连接成功')
          this.isConnected = true
          this.reconnectAttempts = 0
          this.onConnect()
          // 延迟订阅系统消息，确保STOMP客户端完全就绪
          setTimeout(() => {
            if (this.stompClient && this.stompClient.connected) {
              this.subscribeToSystemMessages()
              // 如果有聊天消息处理器，自动订阅聊天消息
              if (this.chatMessageHandlers.length > 0) {
                this.subscribeChatMessages()
              }
            } else {
              console.warn('STOMP客户端未就绪，无法订阅系统消息')
            }
          }, 100)
        },
        (error) => {
          console.error('WebSocket STOMP连接失败:', error)
          this.isConnected = false
          this.onError(error)
          // 自动重连
          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnect(token)
          }
        }
      )
      
      // 监听连接关闭
      this.socket.onclose = () => {
        console.log('WebSocket连接关闭')
        this.isConnected = false
        this.onDisconnect()
        // 清理订阅
        this.clearSubscriptions()
        // 自动重连
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnect(token)
        }
      }
      
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.onError(error)
    }
  }
  
  /**
   * 订阅系统消息
   */
  subscribeToSystemMessages() {
    // 检查 STOMP 客户端是否已连接
    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('WebSocket未连接，无法订阅系统消息')
      return
    }
    
    try {
      // 订阅系统消息队列（用户专属队列）
      // Spring会自动将 /user/queue/system 路由到当前用户
      const subscription = this.stompClient.subscribe('/user/queue/system', (message) => {
        try {
          const data = JSON.parse(message.body)
          this.systemMessageHandlers.forEach(handler => {
            try {
              handler(data)
            } catch (e) {
              console.error('系统消息处理器执行失败:', e)
            }
          })
        } catch (e) {
          console.error('WebSocket系统消息解析失败:', e)
        }
      })
      
      this.subscriptions.push(subscription)
      console.log('系统消息订阅成功')
    } catch (error) {
      console.error('订阅系统消息失败:', error)
      // 如果订阅失败，尝试延迟重试
      setTimeout(() => {
        if (this.stompClient && this.stompClient.connected) {
          try {
            this.subscribeToSystemMessages()
          } catch (e) {
            console.error('重试订阅系统消息失败:', e)
          }
        }
      }, 500)
    }
  }
  
  /**
   * 订阅聊天消息（支持多个处理器）
   * @param {Function} handler - 可选的处理器（如果提供，会添加到处理器列表）
   */
  subscribeChatMessages(handler) {
    // 添加处理器到列表（无论连接是否建立，都可以先添加）
    if (handler && typeof handler === 'function') {
      // 避免重复添加同一个处理器
      if (!this.chatMessageHandlers.includes(handler)) {
        this.chatMessageHandlers.push(handler)
        console.log('已添加聊天消息处理器，当前处理器数量:', this.chatMessageHandlers.length)
      }
    }
    
    // 如果WebSocket未连接，只添加处理器，不创建订阅
    if (!this.stompClient || !this.stompClient.connected) {
      console.log('WebSocket未连接，聊天消息处理器已添加，将在连接成功后自动订阅')
      return null
    }
    
    // 如果已经有订阅，不需要重复订阅
    if (this.chatSubscription) {
      return this.chatSubscription
    }
    
    // 订阅聊天消息队列（用户专属队列）
    // Spring会自动将 /user/queue/chat 路由到当前用户
    const subscription = this.stompClient.subscribe('/user/queue/chat', (message) => {
      try {
        const data = JSON.parse(message.body)
        console.log('[WebSocket] 收到聊天消息:', data.id, '会话:', data.sessionId)
        // 调用所有注册的处理器
        this.chatMessageHandlers.forEach(handler => {
          if (typeof handler === 'function') {
            try {
              handler(data)
            } catch (e) {
              console.error('聊天消息处理器执行失败:', e)
            }
          }
        })
      } catch (e) {
        console.error('WebSocket聊天消息解析失败:', e)
      }
    })
    
    this.chatSubscription = subscription // 保存引用用于后续取消
    this.subscriptions.push(subscription)
    console.log('聊天消息订阅成功')
    return subscription
  }
  
  /**
   * 移除聊天消息处理器
   * @param {Function} handler - 要移除的处理器
   */
  removeChatMessageHandler(handler) {
    const index = this.chatMessageHandlers.indexOf(handler)
    if (index > -1) {
      this.chatMessageHandlers.splice(index, 1)
    }
    
    // 如果没有处理器了，可以取消订阅（但暂时保留，因为可能有其他地方需要）
    // 如果需要取消订阅，可以在外部调用 clearSubscriptions
  }
  
  /**
   * 取消订阅聊天消息（移除所有处理器并取消订阅）
   */
  unsubscribeChatMessages() {
    // 清空所有处理器
    this.chatMessageHandlers = []
    
    // 使用保存的聊天订阅引用来取消订阅
    if (this.chatSubscription) {
      try {
        this.chatSubscription.unsubscribe()
      } catch (e) {
        console.error('取消聊天消息订阅失败:', e)
      }
      // 从订阅列表中移除
      const index = this.subscriptions.indexOf(this.chatSubscription)
      if (index > -1) {
        this.subscriptions.splice(index, 1)
      }
      this.chatSubscription = null
    }
  }
  
  /**
   * 断线重连
   */
  reconnect(token) {
    if (this.reconnectTimer) {
      return
    }
    
    this.reconnectAttempts++
    const delay = this.reconnectInterval * this.reconnectAttempts
    
    console.log(`尝试重新连接 WebSocket... (${this.reconnectAttempts}/${this.maxReconnectAttempts})，${delay}ms后重试`)
    
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect(token)
    }, delay)
  }
  
  /**
   * 添加系统消息处理器
   */
  addSystemMessageHandler(handler) {
    if (typeof handler === 'function') {
      this.systemMessageHandlers.push(handler)
    }
  }
  
  /**
   * 移除系统消息处理器
   */
  removeSystemMessageHandler(handler) {
    const index = this.systemMessageHandlers.indexOf(handler)
    if (index > -1) {
      this.systemMessageHandlers.splice(index, 1)
    }
  }
  
  /**
   * 添加消息处理器（向后兼容方法，等同于 addSystemMessageHandler）
   */
  addMessageHandler(handler) {
    this.addSystemMessageHandler(handler)
  }
  
  /**
   * 移除消息处理器（向后兼容方法，等同于 removeSystemMessageHandler）
   */
  removeMessageHandler(handler) {
    this.removeSystemMessageHandler(handler)
  }
  
  /**
   * 发送消息
   * @param {string} destination - 目标地址（如 /app/message）
   * @param {object} message - 消息内容
   */
  send(destination, message) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(destination, {}, JSON.stringify(message))
      return true
    }
    console.warn('WebSocket未连接，无法发送消息')
    return false
  }
  
  /**
   * 清理订阅
   */
  clearSubscriptions() {
    this.subscriptions.forEach(sub => {
      if (sub) {
        sub.unsubscribe()
      }
    })
    this.subscriptions = []
  }
  
  /**
   * 关闭连接
   */
  close() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    
    // 清理订阅
    this.clearSubscriptions()
    
    // 断开STOMP连接
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        console.log('WebSocket STOMP 连接已断开')
      })
    }
    
    // 关闭SockJS连接
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    
    this.stompClient = null
    this.isConnected = false
    this.systemMessageHandlers = []
    this.chatMessageHandlers = [] // 清理聊天消息处理器
    this.chatSubscription = null // 清理聊天订阅引用
    this.reconnectAttempts = 0
  }
  
  /**
   * 连接成功回调
   */
  onConnect() {
    // 可以在这里添加连接成功后的逻辑
  }
  
  /**
   * 断开连接回调
   */
  onDisconnect() {
    // 可以在这里添加断开连接后的逻辑
  }
  
  /**
   * 错误回调
   */
  onError(error) {
    // 可以在这里添加错误处理逻辑
  }
}

// 创建单例
const wsManager = new WebSocketManager()

export default wsManager
