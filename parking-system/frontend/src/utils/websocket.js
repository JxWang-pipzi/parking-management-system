import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

class WebSocketManager {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 3
    this.listeners = new Map()
    this._connecting = false
  }

  getWsUrl() {
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
    return `${protocol}//${window.location.host}/api/ws`
  }

  connect(token) {
    if (this._connecting || this.connected) return
    this._connecting = true

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.getWsUrl()),
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      reconnectDelay: 0,
      heartbeatIncoming: 25000,
      heartbeatOutgoing: 25000,
      onConnect: (frame) => {
        this.connected = true
        this._connecting = false
        this.reconnectAttempts = 0
        console.log(`[成功][阶段1][WebSocket连接] 时间：${Date.now()} | 结果：连接成功`)
        this.subscribeSystem()
        this.resubscribeAll()
      },
      onDisconnect: (frame) => {
        this.connected = false
        this._connecting = false
        console.log(`[失败][阶段1][WebSocket断开] 时间：${Date.now()}`)
      },
      onStompError: (frame) => {
        this._connecting = false
        console.error(`[失败][阶段1][STOMP错误] 时间：${Date.now()} | 原因：${frame.headers?.message || 'unknown'}`)
      },
      onWebSocketClose: (evt) => {
        this.connected = false
        this._connecting = false
        this.tryReconnect()
      }
    })

    this.stompClient.activate()
  }

  tryReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log(`[失败][阶段5][WebSocket重连] 已达最大重连次数，停止重连`)
      return
    }
    this.reconnectAttempts++
    const delay = 15000 * this.reconnectAttempts
    console.log(`[成功][阶段5][WebSocket重连] 第${this.reconnectAttempts}次，${delay}ms后重连`)
    setTimeout(() => {
      if (this.stompClient && !this.connected && !this._connecting) {
        this.stompClient.activate()
      }
    }, delay)
  }

  subscribeSystem() {
    if (!this.stompClient || !this.connected) return
    this.stompClient.subscribe('/topic/system', (message) => {
      try {
        const data = JSON.parse(message.body)
        this.emit('system', data)
      } catch (e) {
        console.error('[失败][阶段3][系统消息解析]', e)
      }
    })
  }

  subscribe(topic, callback) {
    if (!this.stompClient || !this.connected) {
      this.subscriptions.set(topic, callback)
      return
    }
    const sub = this.stompClient.subscribe(topic, (message) => {
      try {
        const data = JSON.parse(message.body)
        callback(data)
      } catch (e) {
        console.error(`[失败][阶段3][消息解析] topic=${topic}`, e)
      }
    })
    this.subscriptions.set(topic, { callback, subscription: sub })
  }

  resubscribeAll() {
    for (const [topic, value] of this.subscriptions) {
      if (typeof value === 'function') {
        this.subscribe(topic, value)
      } else if (value && value.callback) {
        this.subscribe(topic, value.callback)
      }
    }
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  off(event, callback) {
    if (!this.listeners.has(event)) return
    if (callback) {
      this.listeners.set(event, this.listeners.get(event).filter(cb => cb !== callback))
    } else {
      this.listeners.delete(event)
    }
  }

  emit(event, data) {
    if (!this.listeners.has(event)) return
    this.listeners.get(event).forEach(cb => {
      try {
        cb(data)
      } catch (e) {
        console.error(`[失败][阶段3][事件处理] event=${event}`, e)
      }
    })
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate()
      this.stompClient = null
      this.connected = false
      this._connecting = false
      this.subscriptions.clear()
      this.listeners.clear()
    }
  }
}

const wsManager = new WebSocketManager()

export default wsManager
