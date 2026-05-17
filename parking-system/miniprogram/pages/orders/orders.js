const app = getApp()
const { get, post, put, del } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, showConfirm, formatDate, formatDuration, formatMoney } = require('../../utils/util')
const wsManager = require('../../utils/websocket')

const STATUS_MAP = {
  0: '待支付',
  1: '已完成',
  2: '已取消',
  3: '停车中'
}

const TAB_STATUS_MAP = {
  all: null,
  active: 3,
  completed: 1,
  cancel: 2
}

Page({
  data: {
    statusBarHeight: 20,
    activeTab: 'all',
    orders: [],
    pendingCount: 0,
    loading: false,
    isLoggedIn: false
  },

  onLoad() {
    console.log('[成功][阶段1][页面加载] 时间：' + Date.now() + ' | 参数：无 | 结果：订单页加载')
    const sysInfo = wx.getSystemInfoSync()
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
    this.checkLogin()
  },

  onShow() {
    this.updateTabBar()
    this.checkLogin()
    if (this.data.isLoggedIn) {
      this.fetchOrders()
      this._connectWS()
    }
  },

  onHide() {
    this._disconnectWS()
  },

  onUnload() {
    this._disconnectWS()
  },

  onPullDownRefresh() {
    if (this.data.isLoggedIn) {
      this.fetchOrders().then(() => {
        wx.stopPullDownRefresh()
      })
    } else {
      wx.stopPullDownRefresh()
    }
  },

  checkLogin() {
    const loggedIn = app.isLoggedIn()
    this.setData({ isLoggedIn: loggedIn })
    console.log('[成功][阶段1][登录检查] 时间：' + Date.now() + ' | 参数：无 | 结果：' + (loggedIn ? '已登录' : '未登录'))
  },

  goLogin() {
    console.log('[成功][阶段1][跳转登录] 时间：' + Date.now() + ' | 参数：无 | 结果：跳转登录页')
    wx.navigateTo({ url: '/pages/login/login' })
  },

  goParkingHome() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.activeTab) return
    console.log('[成功][阶段3][切换标签] 时间：' + Date.now() + ' | 参数：tab=' + tab + ' | 结果：切换筛选')
    this.setData({ activeTab: tab, orders: [] })
    this.fetchOrders()
  },

  async fetchOrders() {
    const { activeTab } = this.data

    if (!app.isLoggedIn()) {
      console.log('[失败][阶段2][获取订单] 时间：' + Date.now() + ' | 原因：未登录 | 参数：无')
      return
    }

    this.setData({ loading: true })
    console.log('[成功][阶段2][获取订单] 时间：' + Date.now() + ' | 参数：tab=' + activeTab + ' | 结果：请求发起')

    try {
      const params = {}
      const status = TAB_STATUS_MAP[activeTab]
      if (status !== null && status !== undefined) {
        params.status = status
      }

      const res = await get('/orders', params)
      const rawList = res.data || []

      const orders = rawList.map(item => {
        const durationMinutes = this.calcDuration(item.startTime, item.endTime)
        var amt = typeof item.amount === 'number' ? item.amount : 0
        return {
          ...item,
          startTime: formatDate(item.startTime, 'MM-DD HH:mm'),
          endTime: item.endTime ? formatDate(item.endTime, 'MM-DD HH:mm') : null,
          durationText: durationMinutes !== null ? formatDuration(durationMinutes) : '--',
          amountText: amt === 0 ? '免费' : amt.toFixed(2),
          isFreeOrder: amt === 0,
          statusText: STATUS_MAP[item.status] || '未知',
          actionText: item.status === 0 ? '立即支付' : (item.status === 3 ? '支付并离场' : '')
        }
      })

      let pendingCount = this.data.pendingCount
      if (activeTab === 'all') {
        pendingCount = rawList.filter(o => o.status === 0).length
      } else if (activeTab === 'pending') {
        pendingCount = rawList.length
      }

      this.setData({ orders, pendingCount, loading: false })
      console.log('[成功][阶段4][获取订单] 时间：' + Date.now() + ' | 参数：tab=' + activeTab + ' | 结果：获取' + orders.length + '条订单')
    } catch (error) {
      this.setData({ orders: [], loading: false })
      console.log('[失败][阶段4][获取订单] 时间：' + Date.now() + ' | 原因：' + (error.message || '请求异常') + ' | 参数：tab=' + activeTab)
    }
  },

  calcDuration(start, end) {
    if (!start) return null
    const startDate = new Date(start)
    const endDate = end ? new Date(end) : new Date()
    const diffMs = endDate.getTime() - startDate.getTime()
    if (diffMs < 0) return null
    return Math.ceil(diffMs / 60000)
  },

  async payOrder(e) {
    const id = e.currentTarget.dataset.id
    const amount = e.currentTarget.dataset.amount
    const status = e.currentTarget.dataset.status

    if (status !== 0 && status !== '0') {
      showError('该订单状态不可支付')
      console.log('[失败][阶段2][支付订单] 时间：' + Date.now() + ' | 原因：订单状态非待支付 | 参数：orderId=' + id + ',status=' + status)
      return
    }

    console.log('[成功][阶段2][支付订单] 时间：' + Date.now() + ' | 参数：orderId=' + id + ',amount=' + amount + ' | 结果：弹出确认框')

    const confirmed = await showConfirm('确认支付该订单？金额：¥' + amount)
    if (!confirmed) {
      console.log('[成功][阶段3][支付取消] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：用户取消支付')
      return
    }

    showLoading('支付中...')

    try {
      await post('/orders/' + id + '/pay', { paymentMethod: 1 })
      hideLoading()
      showSuccess('支付成功')
      console.log('[成功][阶段4][支付订单] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：支付成功')
      this.fetchOrders()
    } catch (error) {
      hideLoading()
      showError(error.message || '支付失败')
      console.log('[失败][阶段4][支付订单] 时间：' + Date.now() + ' | 原因：' + (error.message || '支付异常') + ' | 参数：orderId=' + id)
    }
  },

  async completeOrder(e) {
    const id = e.currentTarget.dataset.id

    console.log('[成功][阶段2][确认出场] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：弹出确认框')

    const confirmed = await showConfirm('确认支付并离场？出场后车位将被释放')
    if (!confirmed) {
      console.log('[成功][阶段3][出场取消] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：用户取消出场')
      return
    }

    showLoading('处理中...')

    try {
      await put('/orders/' + id + '/complete')
      hideLoading()
      showSuccess('离场成功，车位已释放')
      console.log('[成功][阶段4][确认出场] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：出场成功')
      this.fetchOrders()
    } catch (error) {
      hideLoading()
      showError(error.message || '出场失败')
      console.log('[失败][阶段4][确认出场] 时间：' + Date.now() + ' | 原因：' + (error.message || '出场异常') + ' | 参数：orderId=' + id)
    }
  },

  async cancelOrder(e) {
    const id = e.currentTarget.dataset.id

    console.log('[成功][阶段2][取消订单] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：弹出确认框')

    const confirmed = await showConfirm('确认取消该订单？')
    if (!confirmed) {
      console.log('[成功][阶段3][取消放弃] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：用户放弃取消')
      return
    }

    showLoading('取消中...')

    try {
      await put('/orders/' + id + '/cancel')
      hideLoading()
      showSuccess('取消成功')
      console.log('[成功][阶段4][取消订单] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：取消成功')
      this.fetchOrders()
    } catch (error) {
      hideLoading()
      showError(error.message || '取消失败')
      console.log('[失败][阶段4][取消订单] 时间：' + Date.now() + ' | 原因：' + (error.message || '取消异常') + ' | 参数：orderId=' + id)
    }
  },

  async deleteOrder(e) {
    const id = e.currentTarget.dataset.id

    console.log('[成功][阶段2][删除订单] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：弹出确认框')

    const confirmed = await showConfirm('确认删除该订单？删除后不可恢复')
    if (!confirmed) {
      console.log('[成功][阶段3][删除放弃] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：用户放弃删除')
      return
    }

    showLoading('删除中...')

    try {
      await del('/orders/' + id)
      hideLoading()
      showSuccess('删除成功')
      console.log('[成功][阶段4][删除订单] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：删除成功')
      this.fetchOrders()
    } catch (error) {
      hideLoading()
      showError(error.message || '删除失败')
      console.log('[失败][阶段4][删除订单] 时间：' + Date.now() + ' | 原因：' + (error.message || '删除异常') + ' | 参数：orderId=' + id)
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    const status = Number(e.currentTarget.dataset.status)
    console.log('[成功][阶段2][查看详情] 时间：' + Date.now() + ' | 参数：orderId=' + id + ' | 结果：跳转详情页')
    if (status === 3) {
      wx.navigateTo({ url: '/pages/active-parking/active-parking?id=' + id })
      return
    }
    wx.navigateTo({ url: '/pages/order-detail/order-detail?id=' + id })
  },

  _connectWS: function () {
    var that = this
    if (!wsManager.isConnected()) {
      wsManager.connect()
    }
    wsManager.off('order_update')
    wsManager.on('order_update', function (data) {
      console.log('[成功][阶段2][WebSocket订单更新] 时间：' + Date.now() + ' | 参数：' + JSON.stringify(data) + ' | 结果：自动刷新订单列表')
      that.fetchOrders()
    })
  },

  _disconnectWS: function () {
    wsManager.off('order_update')
  },

  updateTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 })
    }
  }
})
