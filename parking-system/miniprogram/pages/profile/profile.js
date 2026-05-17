const app = getApp()
const { get, post, put, del } = require('../../utils/request')
const { showSuccess, showConfirm } = require('../../utils/util')

Page({
  data: {
    statusBarHeight: 20,
    isLoggedIn: false,
    userInfo: null,
    vehicles: [],
    stats: {
      orderCount: 0,
      parkCount: 0,
      memberDays: 0
    }
  },

  onLoad() {
    console.log('[成功][阶段1][页面加载] 时间：' + Date.now() + ' | 参数：无 | 结果：profile页面加载')
    const sysInfo = wx.getSystemInfoSync()
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
    this.checkLoginStatus()
  },

  onShow() {
    console.log('[成功][阶段1][页面显示] 时间：' + Date.now() + ' | 参数：无 | 结果：profile页面显示')
    this.updateTabBar()
    this.checkLoginStatus()
    if (this.data.isLoggedIn) {
      this.getUserProfile()
    }
  },

  checkLoginStatus() {
    const isLoggedIn = app.isLoggedIn()
    this.setData({
      isLoggedIn,
      userInfo: isLoggedIn ? app.globalData.userInfo : null
    })
    console.log('[成功][阶段1][登录检查] 时间：' + Date.now() + ' | 参数：无 | 结果：' + (isLoggedIn ? '已登录' : '未登录'))
  },

  async getUserProfile() {
    try {
      const res = await get('/users/profile')
      const data = res.data || {}
      this.setData({
        userInfo: {
          ...this.data.userInfo,
          name: data.name || this.data.userInfo.name,
          phone: data.phone || this.data.userInfo.phone,
          avatar: data.avatar || this.data.userInfo.avatar
        },
        stats: {
          orderCount: data.orderCount || 0,
          parkCount: data.parkCount || 0,
          memberDays: data.memberDays || 0
        }
      })
      console.log('[成功][阶段2][获取用户信息] 时间：' + Date.now() + ' | 参数：/users/profile | 结果：orderCount=' + (data.orderCount || 0) + ',parkCount=' + (data.parkCount || 0) + ',memberDays=' + (data.memberDays || 0))
    } catch (error) {
      console.log('[失败][阶段2][获取用户信息] 时间：' + Date.now() + ' | 原因：' + (error.message || '请求失败') + ' | 参数：/users/profile')
      this.setData({
        stats: {
          orderCount: 0,
          parkCount: 0,
          memberDays: 0
        }
      })
    }
    this.loadVehicles()
  },

  async loadVehicles() {
    try {
      const res = await get('/vehicles')
      var list = res.data || []
      list = list.map(function (v) {
        var plate = v.plateNumber || ''
        if (plate.length > 2) {
          plate = plate.substring(0, 1) + ' · ' + plate.substring(1)
        }
        return Object.assign({}, v, { displayPlate: plate })
      })
      this.setData({ vehicles: list })
      console.log('[成功][阶段2][加载车辆列表] 时间：' + Date.now() + ' | 结果：共' + list.length + '辆')
    } catch (error) {
      this.setData({ vehicles: [] })
      console.log('[失败][阶段2][加载车辆列表] 时间：' + Date.now() + ' | 原因：' + (error.message || '请求失败'))
    }
  },

  navigateToLogin() {
    console.log('[成功][阶段1][跳转登录] 时间：' + Date.now() + ' | 参数：/pages/login/login | 结果：跳转中')
    wx.navigateTo({
      url: '/pages/login/login'
    })
  },

  navigateToRegister() {
    console.log('[成功][阶段1][跳转注册] 时间：' + Date.now() + ' | 参数：/pages/register/register | 结果：跳转中')
    wx.navigateTo({
      url: '/pages/register/register'
    })
  },

  navigateToProfile() {
    console.log('[成功][阶段2][查看个人资料] 时间：' + Date.now() + ' | 参数：无 | 结果：暂无跳转')
  },

  navigateToMyCars() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转我的车辆] 时间：' + Date.now() + ' | 结果：跳转中')
    wx.navigateTo({ url: '/pages/my-vehicles/my-vehicles' })
  },

  navigateToCoupons() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转优惠券] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  navigateToInvoice() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转发票管理] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  navigateToFAQ() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转常见问题] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  navigateToOrders() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转停车记录] 时间：' + Date.now() + ' | 参数：/pages/orders/orders | 结果：跳转中')
    wx.switchTab({
      url: '/pages/orders/orders'
    })
  },

  navigateToFeedback() {
    console.log('[成功][阶段3][跳转意见反馈] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  navigateToAbout() {
    console.log('[成功][阶段3][跳转关于我们] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  navigateToSettings() {
    console.log('[成功][阶段3][跳转设置] 时间：' + Date.now() + ' | 参数：/pages/api-settings/api-settings | 结果：跳转中')
    wx.navigateTo({ url: '/pages/api-settings/api-settings' })
  },

  navigateToMonthlyCard() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转开通月卡] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  navigateToAddCar() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转添加车辆] 时间：' + Date.now() + ' | 结果：跳转中')
    wx.navigateTo({ url: '/pages/add-vehicle/add-vehicle' })
  },

  navigateToPayment() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转无感支付] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  navigateToAddress() {
    if (!this.checkLogin()) return
    console.log('[成功][阶段3][跳转常用地址] 时间：' + Date.now() + ' | 参数：无 | 结果：功能开发中')
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  async handleLogout() {
    try {
      const confirmed = await showConfirm('确认退出登录？')
      if (!confirmed) {
        console.log('[成功][阶段5][取消退出] 时间：' + Date.now() + ' | 参数：无 | 结果：用户取消')
        return
      }

      app.clearUserInfo()
      this.setData({
        isLoggedIn: false,
        userInfo: null,
        vehicles: [],
        stats: {
          orderCount: 0,
          parkCount: 0,
          memberDays: 0
        }
      })
      console.log('[成功][阶段5][退出登录] 时间：' + Date.now() + ' | 参数：无 | 结果：退出成功')
      showSuccess('已退出登录')
    } catch (error) {
      console.log('[失败][阶段5][退出登录] 时间：' + Date.now() + ' | 原因：' + (error.message || '未知错误') + ' | 参数：无')
    }
  },

  async onDeleteVehicle(e) {
    var id = e.currentTarget.dataset.id
    var plate = e.currentTarget.dataset.plate || ''
    var that = this
    const confirmed = await showConfirm('确认删除车辆「' + plate + '」？')
    if (!confirmed) return
    try {
      await del('/vehicles/' + id)
      showSuccess('删除成功')
      that.loadVehicles()
    } catch (error) {
      console.log('[失败][阶段3][删除车辆] 时间：' + Date.now() + ' | 原因：' + (error.message || '删除失败'))
    }
  },

  async onSetDefaultVehicle(e) {
    var id = e.currentTarget.dataset.id
    var that = this
    try {
      await put('/vehicles/' + id + '/default', {})
      showSuccess('已设为默认')
      that.loadVehicles()
    } catch (error) {
      console.log('[失败][阶段3][设为默认] 时间：' + Date.now() + ' | 原因：' + (error.message || '操作失败'))
    }
  },

  checkLogin() {
    if (!this.data.isLoggedIn) {
      console.log('[失败][阶段1][权限校验] 时间：' + Date.now() + ' | 原因：未登录 | 参数：无')
      wx.navigateTo({
        url: '/pages/login/login'
      })
      return false
    }
    return true
  },

  updateTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 })
    }
  }
})
