var app = getApp()

function log(stage, status, operation, params, result) {
  var now = Date.now()
  if (status === '成功') {
    console.log('[成功][阶段' + stage + '][' + operation + '] 时间：' + now + ' | 参数：' + params + ' | 结果：' + result)
  } else {
    console.log('[失败][阶段' + stage + '][' + operation + '] 时间：' + now + ' | 原因：' + result + ' | 参数：' + params)
  }
}

var wsManager = {
  socketTask: null,
  connected: false,
  reconnectTimer: null,
  heartbeatTimer: null,
  reconnectCount: 0,
  maxReconnect: 5,
  listeners: {},
  _connectFlag: false,
  _eventsBound: false,

  connect: function () {
    if (this._connectFlag) return
    if (this.connected) return
    this._connectFlag = true

    var token = app.globalData.token || wx.getStorageSync('token')
    if (!token) {
      log(1, '失败', 'WebSocket连接', '', 'Token为空，无法连接')
      this._connectFlag = false
      return
    }

    var baseUrl = app.getBaseUrl()
    if (/127\.0\.0\.1|localhost/.test(baseUrl)) {
      try {
        var systemInfo = wx.getSystemInfoSync()
        if (systemInfo.platform !== 'devtools') {
          log(1, '失败', 'WebSocket连接', '', '真机无法连接127.0.0.1，请前往接口设置页配置局域网IP')
          wx.showModal({
            title: '连接失败',
            content: '真机无法访问127.0.0.1，请在接口设置中配置电脑的局域网IP地址（如192.168.x.x）',
            confirmText: '去设置',
            cancelText: '取消',
            success: function (res) {
              if (res.confirm) {
                wx.navigateTo({ url: '/pages/api-settings/api-settings' })
              }
            }
          })
          this._connectFlag = false
          return
        }
      } catch (e) {}
    }

    var wsUrl = this._buildWsUrl(baseUrl, token)
    var safeWsUrl = wsUrl.replace(token, '***')

    log(1, '成功', 'WebSocket连接', 'url=' + safeWsUrl, '开始连接')

    var that = this
    if (this.socketTask) {
      try {
        this.socketTask.close({})
      } catch (e) {}
      this.socketTask = null
    }
    this.socketTask = wx.connectSocket({
      url: wsUrl,
      success: function () {
        log(1, '成功', 'WebSocket连接', '', '连接请求已发送')
      },
      fail: function (err) {
        log(1, '失败', 'WebSocket连接', 'url=' + safeWsUrl, err.errMsg || '连接失败')
        that._connectFlag = false
        that._scheduleReconnect()
      }
    })

    if (this._eventsBound) return
    this._eventsBound = true
    wx.onSocketOpen(function (res) {
      that.connected = true
      that.reconnectCount = 0
      that._connectFlag = false
      log(1, '成功', 'WebSocket连接', '', '连接已建立')
      that._startHeartbeat()
      that._emit('connected', {})
    })

    wx.onSocketMessage(function (res) {
      try {
        var data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
        log(2, '成功', 'WebSocket消息', 'type=' + (data.type || 'unknown'), '收到消息')

        if (data.type === 'pong') return

        if (data.type === 'order_update') {
          that._emit('order_update', data.data)
        } else if (data.type === 'parking_lot_update') {
          that._emit('parking_lot_update', data)
        } else if (data.type === 'space_update') {
          that._emit('space_update', data)
        } else if (data.type === 'system_notification') {
          that._emit('system_notification', data)
        } else if (data.type === 'error') {
          log(3, '失败', 'WebSocket错误', '', data.message || '服务器错误')
        } else {
          that._emit('message', data)
        }
      } catch (e) {
        log(3, '失败', 'WebSocket消息解析', '', e.message || '解析失败')
      }
    })

    wx.onSocketClose(function (res) {
      that.connected = false
      that._connectFlag = false
      that._stopHeartbeat()
      log(5, '成功', 'WebSocket断开', '', 'code=' + res.code)
      that._emit('disconnected', { code: res.code })
      that._scheduleReconnect()
    })

    wx.onSocketError(function (err) {
      that.connected = false
      that._connectFlag = false
      that._stopHeartbeat()
      log(3, '失败', 'WebSocket错误', '', err.errMsg || '连接异常')
      that._emit('error', { message: err.errMsg })
      that._scheduleReconnect()
    })
  },

  _buildWsUrl: function (baseUrl, token) {
    var normalizedBaseUrl = (baseUrl || '').replace(/\/+$/, '')
    return normalizedBaseUrl.replace(/^http(s)?/i, 'ws$1') + '/ws/parking/' + encodeURIComponent(token)
  },

  _startHeartbeat: function () {
    var that = this
    this._stopHeartbeat()
    this.heartbeatTimer = setInterval(function () {
      if (that.connected && that.socketTask) {
        wx.sendSocketMessage({
          data: JSON.stringify({ action: 'ping' }),
          fail: function () {}
        })
      }
    }, 30000)
  },

  _stopHeartbeat: function () {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  },

  _scheduleReconnect: function () {
    var that = this
    if (this.reconnectCount >= this.maxReconnect) {
      log(5, '失败', 'WebSocket重连', '', '已达最大重连次数' + this.maxReconnect)
      return
    }
    this.reconnectCount++
    var delay = Math.min(10000, 2000 * this.reconnectCount)
    log(5, '成功', 'WebSocket重连', '', '第' + this.reconnectCount + '次，' + delay + 'ms后重连')
    this.reconnectTimer = setTimeout(function () {
      that.connect()
    }, delay)
  },

  send: function (data) {
    if (!this.connected) return false
    try {
      wx.sendSocketMessage({
        data: typeof data === 'string' ? data : JSON.stringify(data),
        fail: function (err) {
          log(3, '失败', 'WebSocket发送', '', err.errMsg || '发送失败')
        }
      })
      return true
    } catch (e) {
      return false
    }
  },

  subscribe: function (channel) {
    this.send({ action: 'subscribe', channel: channel })
  },

  on: function (event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  },

  off: function (event, callback) {
    if (!this.listeners[event]) return
    if (callback) {
      this.listeners[event] = this.listeners[event].filter(function (cb) { return cb !== callback })
    } else {
      delete this.listeners[event]
    }
  },

  _emit: function (event, data) {
    var callbacks = this.listeners[event]
    if (!callbacks || callbacks.length === 0) return
    for (var i = 0; i < callbacks.length; i++) {
      try {
        callbacks[i](data)
      } catch (e) {
        log(4, '失败', '事件处理', 'event=' + event, e.message || '回调执行失败')
      }
    }
  },

  disconnect: function () {
    this._connectFlag = false
    this._stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.reconnectCount = this.maxReconnect
    if (this.socketTask) {
      try {
        wx.closeSocket({})
      } catch (e) {}
      this.socketTask = null
    }
    this.connected = false
    this.listeners = {}
    log(5, '成功', 'WebSocket断开', '', '主动断开连接')
  },

  isConnected: function () {
    return this.connected
  }
}

module.exports = wsManager
