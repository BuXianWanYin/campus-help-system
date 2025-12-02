/**
 * WebSocket 工具类（STOMP协议）
 * 用于管理WebSocket连接、重连、消息接收
 */

import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

class WebSocketManager {
  constructor() {
    this.stompClient = null
    this.socket = null
    this.reconnectTimer = null
    this.messageHandlers = []
    this.subscriptions = [] // 存储订阅对象，用于取消订阅
    this.isConnected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000 // 3秒
    // 使用环境变量中的 WebSocket URL
    // SockJS 需要 HTTP URL（会自动转换为 WebSocket）
    const wsUrl = import.meta.env.VITE_WS_URL
    if (wsUrl.startsWith('ws://') || wsUrl.startsWith('wss://')) {
      // 如果是 WebSocket 协议，转换为 HTTP 协议并移除 /ws
      this.baseUrl = wsUrl.replace(/^ws:\/\//, 'http://').replace(/^wss:\/\//, 'https://').replace(/\/ws$/, '')
    } else {
      // 如果已经是 HTTP 协议，直接使用（移除 /ws 后缀，如果存在）
      this.baseUrl = wsUrl.replace(/\/ws$/, '')
    }
  }
  
  /**
   * 连接WebSocket
   * @param {string} token - JWT token
   */
  connect(token) {
    if (this.stompClient && this.stompClient.connected) {
      console.log('WebSocket已连接，无需重复连接')
      return
    }
    
    try {
      // 创建SockJS连接
      // SockJS 会自动处理协议转换（http -> ws），所以直接使用 baseUrl + /ws
      const wsUrl = `${this.baseUrl}/ws`
      console.log('正在连接 WebSocket:', wsUrl)
      this.socket = new SockJS(wsUrl)
      // 检查 Stomp 是否正确导入
      if (!Stomp || typeof Stomp.over !== 'function') {
        throw new Error('STOMP 客户端未正确加载，请检查导入')
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
          console.log('WebSocket STOMP 连接成功')
          this.isConnected = true
          this.reconnectAttempts = 0
          this.onConnect()
          // 订阅消息
          this.subscribeToMessages()
        },
        (error) => {
          console.error('WebSocket STOMP 连接失败:', error)
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
        console.log('WebSocket 连接关闭')
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
   * 订阅消息
   */
  subscribeToMessages() {
    // 订阅系统消息队列（用户专属队列）
    // Spring会自动将 /user/queue/system 路由到当前用户
    const subscription = this.stompClient.subscribe('/user/queue/system', (message) => {
      try {
        const data = JSON.parse(message.body)
        this.handleMessage(data)
      } catch (e) {
        console.error('WebSocket消息解析失败:', e)
      }
    })
    
    this.subscriptions.push(subscription)
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
   * 处理消息
   */
  handleMessage(message) {
    // 调用所有注册的消息处理器
    this.messageHandlers.forEach(handler => {
      try {
        handler(message)
      } catch (e) {
        console.error('消息处理器执行失败:', e)
      }
    })
  }
  
  /**
   * 添加消息处理器
   */
  addMessageHandler(handler) {
    if (typeof handler === 'function') {
      this.messageHandlers.push(handler)
    }
  }
  
  /**
   * 移除消息处理器
   */
  removeMessageHandler(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
    }
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
    this.messageHandlers = []
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
