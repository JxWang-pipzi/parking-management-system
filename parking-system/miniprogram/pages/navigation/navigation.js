const { safeNavigateBack } = require('../../utils/util')

Page({
  data: {
    statusBarHeight: 20,
    parkName: '朝阳大悦城停车场'
  },

  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync()
    this.setData({
      statusBarHeight: sysInfo.statusBarHeight || 20,
      parkName: options.name ? decodeURIComponent(options.name) : '朝阳大悦城停车场'
    })
  },

  goBack() {
    safeNavigateBack({ fallbackUrl: '/pages/parking-detail/parking-detail' })
  },

  exitNavigation() {
    this.goBack()
  },

  refreshRoute() {
    wx.showToast({ title: '路线已刷新', icon: 'none' })
  },

  viewDetail() {
    this.goBack()
  }
})
