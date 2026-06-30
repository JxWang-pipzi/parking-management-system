const app = getApp()
const { safeNavigateBack } = require('../../utils/util')

Page({
  data: {
    statusBarHeight: 20,
    envVersion: 'develop',
    defaultBaseUrl: '',
    currentBaseUrl: '',
    customBaseUrl: '',
    baseUrlOverride: '',
    testing: false,
    saving: false,
    probing: false,
    presets: [
      {
        key: 'local',
        name: '本机模拟器',
        url: 'http://127.0.0.1:8081/api',
        desc: '仅适合开发者工具模拟器，不适合手机真机'
      },
      {
        key: 'lan-template',
        name: '局域网示例',
        url: 'http://192.168.1.8:8081/api',
        desc: '改成当前电脑的局域网 IPv4，适合同一 Wi-Fi 真机调试'
      },
      {
        key: 'cloud',
        name: '演示云端',
        url: 'https://api.smartparking.com/api',
        desc: '适合固定外网演示环境，前提是该地址真实可用'
      }
    ]
  },

  onLoad() {
    var sysInfo = wx.getSystemInfoSync()
    var envVersion = typeof __wxConfig !== 'undefined' && __wxConfig ? __wxConfig.envVersion : 'develop'
    var defaultBaseUrl = app.getDefaultBaseUrl()
    var currentBaseUrl = app.getBaseUrl()
    var overrideBaseUrl = wx.getStorageSync('baseUrlOverride') || ''
    this.setData({
      envVersion: envVersion,
      statusBarHeight: sysInfo.statusBarHeight || 20,
      defaultBaseUrl: defaultBaseUrl,
      currentBaseUrl: currentBaseUrl,
      customBaseUrl: overrideBaseUrl || currentBaseUrl,
      baseUrlOverride: overrideBaseUrl
    })
    console.log('[成功][阶段1][接口设置页加载] 时间：' + Date.now() + ' | 参数：baseUrl=' + currentBaseUrl)
  },

  goBack() {
    safeNavigateBack({ fallbackUrl: '/pages/profile/profile' })
  },

  onCustomInput(e) {
    this.setData({ customBaseUrl: e.detail.value })
  },

  onSelectPreset(e) {
    var url = e.currentTarget.dataset.url || ''
    this.setData({ customBaseUrl: url })
    console.log('[成功][阶段2][选择预设接口地址] 时间：' + Date.now() + ' | 参数：url=' + url)
  },

  normalizeBaseUrl(url) {
    return (url || '').trim().replace(/\/+$/, '')
  },

  validateBaseUrl(url) {
    return /^https?:\/\/[^\s]+$/i.test(url)
  },

  applyBaseUrl(url) {
    var normalized = this.normalizeBaseUrl(url)
    app.setBaseUrlOverride(normalized)
    this.setData({
      currentBaseUrl: app.getBaseUrl(),
      baseUrlOverride: normalized,
      customBaseUrl: normalized
    })
  },

  async handleTest() {
    if (this.data.testing) return
    var targetBaseUrl = this.normalizeBaseUrl(this.data.customBaseUrl)
    if (!targetBaseUrl) {
      wx.showToast({ title: '请输入接口地址', icon: 'none' })
      return
    }
    if (!this.validateBaseUrl(targetBaseUrl)) {
      wx.showToast({ title: '接口地址格式不正确', icon: 'none' })
      return
    }

    this.setData({ testing: true })
    wx.showLoading({ title: '测试中...', mask: true })
    var start = Date.now()
    try {
      await new Promise(function (resolve, reject) {
        wx.request({
          url: targetBaseUrl + '/parking-lots',
          method: 'GET',
          timeout: 8000,
          success: function (res) {
            if (res.statusCode === 200 && res.data && res.data.code === 200) {
              resolve(res)
              return
            }
            reject(new Error((res.data && res.data.message) || ('HTTP ' + res.statusCode)))
          },
          fail: function (err) {
            reject(err)
          }
        })
      })
      wx.hideLoading()
      wx.showToast({ title: '连接成功', icon: 'success' })
      console.log('[成功][阶段4][测试接口地址] 时间：' + Date.now() + ' | 参数：url=' + targetBaseUrl + ' | 耗时：' + (Date.now() - start) + 'ms')
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: '连接失败', icon: 'none' })
      console.log('[失败][阶段4][测试接口地址] 时间：' + Date.now() + ' | 原因：' + (error.errMsg || error.message || '未知错误') + ' | 参数：url=' + targetBaseUrl)
    } finally {
      this.setData({ testing: false })
    }
  },

  handleSave() {
    if (this.data.saving) return
    var targetBaseUrl = this.normalizeBaseUrl(this.data.customBaseUrl)
    if (!targetBaseUrl) {
      wx.showToast({ title: '请输入接口地址', icon: 'none' })
      return
    }
    if (!this.validateBaseUrl(targetBaseUrl)) {
      wx.showToast({ title: '接口地址格式不正确', icon: 'none' })
      return
    }

    this.setData({ saving: true })
    this.applyBaseUrl(targetBaseUrl)
    wx.showToast({ title: '保存成功', icon: 'success' })
    console.log('[成功][阶段4][保存接口地址] 时间：' + Date.now() + ' | 参数：url=' + targetBaseUrl)
    this.setData({ saving: false })
  },

  handleReset() {
    app.clearBaseUrlOverride()
    var defaultBaseUrl = app.getDefaultBaseUrl()
    this.setData({
      currentBaseUrl: defaultBaseUrl,
      baseUrlOverride: '',
      customBaseUrl: defaultBaseUrl
    })
    wx.showToast({ title: '已恢复默认', icon: 'success' })
    console.log('[成功][阶段4][恢复默认接口地址] 时间：' + Date.now() + ' | 参数：url=' + defaultBaseUrl)
  },

  handleAutoProbe() {
    if (this.data.probing) return
    this.setData({ probing: true })
    wx.showLoading({ title: '探测中...', mask: true })

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
    var startTime = Date.now()

    function probeBatch() {
      if (found) return
      var start = currentBatch * batchSize
      var end = Math.min(start + batchSize, allIps.length)
      if (start >= allIps.length) {
        wx.hideLoading()
        that.setData({ probing: false })
        wx.showToast({ title: '未找到服务器', icon: 'none' })
        console.log('[失败][阶段2][自动探测] 时间：' + Date.now() + ' | 原因：所有候选IP均无法连接 | 耗时：' + (Date.now() - startTime) + 'ms')
        return
      }

      var batchDone = 0
      var batchTotal = end - start

      for (var j = start; j < end; j++) {
        ;(function (probeIp, probeSubnet) {
          wx.request({
            url: 'http://' + probeIp + ':8081/api/parking-lots',
            method: 'GET',
            timeout: 2000,
            success: function (res) {
              if (!found && res.statusCode === 200) {
                found = true
                var foundUrl = 'http://' + probeIp + ':8081/api'
                wx.hideLoading()
                that.setData({
                  probing: false,
                  customBaseUrl: foundUrl,
                  currentBaseUrl: foundUrl
                })
                app.setBaseUrlOverride(foundUrl)
                wx.showToast({ title: '探测成功: ' + probeIp, icon: 'success' })
                console.log('[成功][阶段2][自动探测] 时间：' + Date.now() + ' | 参数：ip=' + probeIp + ' | 结果：探测成功 | 耗时：' + (Date.now() - startTime) + 'ms')
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

    console.log('[成功][阶段2][自动探测] 时间：' + Date.now() + ' | 参数：候选IP数=' + allIps.length + ' | 结果：开始探测')
    probeBatch()
  }
})
