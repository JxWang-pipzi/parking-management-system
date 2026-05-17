App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: (function () {
      var env = __wxConfig ? __wxConfig.envVersion : 'develop'
      var overrideBaseUrl = wx.getStorageSync('baseUrlOverride')
      if (overrideBaseUrl) {
        return overrideBaseUrl
      }
      if (env === 'release' || env === 'trial') {
        return 'https://api.smartparking.com/api'
      }
      return 'http://127.0.0.1:8080/api'
    })()
  },

  onLaunch() {
    console.log('[成功][阶段1][应用启动] 时间：' + Date.now() + ' | 参数：无 | 结果：启动完成')
    this._detectDevServerIp()
    console.log('[成功][阶段1][环境信息] 时间：' + Date.now() + ' | 参数：baseUrl=' + this.globalData.baseUrl)
    this.checkLoginStatus()
  },

  _detectDevServerIp() {
    var overrideBaseUrl = wx.getStorageSync('baseUrlOverride')
    if (overrideBaseUrl) {
      console.log('[成功][阶段1][开发服务器检测] 时间：' + Date.now() + ' | 结果：使用手动覆盖地址，跳过自动检测')
      return
    }

    var env = __wxConfig ? __wxConfig.envVersion : 'develop'
    if (env !== 'develop') {
      return
    }

    try {
      var systemInfo = wx.getSystemInfoSync()
      if (systemInfo.platform === 'devtools') {
        console.log('[成功][阶段1][开发服务器检测] 时间：' + Date.now() + ' | 结果：模拟器环境，使用127.0.0.1')
        return
      }

      var cachedIp = wx.getStorageSync('__devServerIp')
      if (cachedIp) {
        this.globalData.baseUrl = 'http://' + cachedIp + ':8080/api'
        console.log('[成功][阶段1][开发服务器检测] 时间：' + Date.now() + ' | 结果：使用缓存IP=' + cachedIp)
        return
      }

      this._probeDevServerIp()
    } catch (e) {
      console.log('[失败][阶段1][开发服务器检测] 时间：' + Date.now() + ' | 原因：' + (e.message || '检测异常'))
    }
  },

  _probeDevServerIp() {
    var that = this
    var subnets = ['192.168.1', '192.168.0', '192.168.31', '10.0.0', '172.16.0']

    var cachedSubnet = wx.getStorageSync('__devServerSubnet')
    if (cachedSubnet && subnets.indexOf(cachedSubnet) === -1) {
      subnets.unshift(cachedSubnet)
    } else if (cachedSubnet) {
      subnets.splice(subnets.indexOf(cachedSubnet), 1)
      subnets.unshift(cachedSubnet)
    }

    var allIps = []
    for (var s = 0; s < subnets.length; s++) {
      for (var i = 1; i <= 254; i++) {
        allIps.push({ ip: subnets[s] + '.' + i, subnet: subnets[s] })
      }
    }

    var found = false
    var batchSize = 10
    var currentBatch = 0

    function probeBatch() {
      if (found) return
      var start = currentBatch * batchSize
      var end = Math.min(start + batchSize, allIps.length)
      if (start >= allIps.length) {
        console.log('[失败][阶段1][开发服务器探测] 时间：' + Date.now() + ' | 原因：所有候选IP均无法连接')
        return
      }

      var batchDone = 0
      var batchTotal = end - start

      for (var j = start; j < end; j++) {
        ;(function (probeIp, probeSubnet) {
          wx.request({
            url: 'http://' + probeIp + ':8080/api/parking-lots',
            method: 'GET',
            timeout: 2000,
            success: function (res) {
              if (!found && res.statusCode === 200) {
                found = true
                that.globalData.baseUrl = 'http://' + probeIp + ':8080/api'
                wx.setStorageSync('__devServerIp', probeIp)
                wx.setStorageSync('__devServerSubnet', probeSubnet)
                console.log('[成功][阶段1][开发服务器探测] 时间：' + Date.now() + ' | 参数：ip=' + probeIp + ' | 结果：探测成功')
              }
              batchDone++
              if (batchDone >= batchTotal && !found) {
                currentBatch++
                probeBatch()
              }
            },
            fail: function () {
              batchDone++
              if (batchDone >= batchTotal && !found) {
                currentBatch++
                probeBatch()
              }
            }
          })
        })(allIps[j].ip, allIps[j].subnet)
      }
    }

    console.log('[成功][阶段1][开发服务器探测] 时间：' + Date.now() + ' | 参数：候选IP数=' + allIps.length + ' | 结果：开始分批探测')
    probeBatch()
  },

  getDefaultBaseUrl() {
    var env = __wxConfig ? __wxConfig.envVersion : 'develop'
    if (env === 'release' || env === 'trial') {
      return 'https://api.smartparking.com/api'
    }
    return 'http://127.0.0.1:8080/api'
  },

  getBaseUrl() {
    return this.globalData.baseUrl || wx.getStorageSync('baseUrlOverride') || this.getDefaultBaseUrl()
  },

  setBaseUrlOverride(baseUrl) {
    var normalized = (baseUrl || '').trim().replace(/\/+$/, '')
    this.globalData.baseUrl = normalized || this.getDefaultBaseUrl()
    if (normalized) {
      wx.setStorageSync('baseUrlOverride', normalized)
      var ipMatch = normalized.match(/(\d+\.\d+\.\d+\.\d+)/)
      if (ipMatch) {
        wx.setStorageSync('__devServerIp', ipMatch[1])
        var subnet = ipMatch[1].replace(/\.\d+$/, '')
        wx.setStorageSync('__devServerSubnet', subnet)
      }
    } else {
      wx.removeStorageSync('baseUrlOverride')
    }
    console.log('[成功][阶段1][设置接口地址] 时间：' + Date.now() + ' | 参数：baseUrl=' + this.globalData.baseUrl)
  },

  clearBaseUrlOverride() {
    wx.removeStorageSync('baseUrlOverride')
    wx.removeStorageSync('__devServerIp')
    wx.removeStorageSync('__devServerSubnet')
    this.globalData.baseUrl = this.getDefaultBaseUrl()
    console.log('[成功][阶段1][清除接口地址覆盖] 时间：' + Date.now() + ' | 参数：baseUrl=' + this.globalData.baseUrl)
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userInfo = userInfo
      console.log('[成功][阶段1][登录检查] 时间：' + Date.now() + ' | 结果：已登录')
    } else {
      console.log('[成功][阶段1][登录检查] 时间：' + Date.now() + ' | 结果：未登录')
    }
  },

  setUserInfo(userInfo, token) {
    const current = this.globalData.userInfo || wx.getStorageSync('userInfo') || {}
    const mergedUserInfo = Object.assign({}, current, userInfo)
    this.globalData.userInfo = mergedUserInfo
    this.globalData.token = token
    wx.setStorageSync('userInfo', mergedUserInfo)
    wx.setStorageSync('token', token)
    console.log('[成功][阶段1][设置用户信息] 时间：' + Date.now() + ' | 参数：userId=' + (mergedUserInfo.id || 'unknown'))
  },

  clearUserInfo() {
    this.globalData.userInfo = null
    this.globalData.token = null
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('token')
    console.log('[成功][阶段5][清除用户信息] 时间：' + Date.now())
  },

  isLoggedIn() {
    return !!this.globalData.token
  }
})
