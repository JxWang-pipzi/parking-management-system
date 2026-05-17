const app = getApp()
const { get, put, post } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, showConfirm, formatDate, safeNavigateBack } = require('../../utils/util')

var FREE_DURATION_MINUTES = 15

Page({
  data: {
    statusBarHeight: 20,
    order: null,
    timer: '00:00:00',
    seconds: 0,
    timerInterval: null,
    estimatedFee: '0.00',
    isFree: true,
    hourlyRate: 6,
    isFirstParking: true
  },
  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync()
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
    console.log('[成功][阶段1][页面加载] 时间：' + Date.now() + ' | 参数：' + JSON.stringify(options) + ' | 结果：active-parking页面加载')
    const id = options.id
    if (id) this.loadOrder(id)
  },
  onUnload() {
    if (this.data.timerInterval) clearInterval(this.data.timerInterval)
    console.log('[成功][阶段5][页面卸载] 时间：' + Date.now() + ' | 参数：无 | 结果：定时器已清除')
  },
  async loadOrder(id) {
    console.log('[成功][阶段2][加载订单] 时间：' + Date.now() + ' | 参数：id=' + id + ' | 结果：请求中')
    showLoading()
    try {
      const res = await get('/orders/' + id)
      const raw = res.data || {}
      var rate = Number(raw.hourlyRate || 6)
      if (!raw.hourlyRate && raw.parkingLotId) {
        try {
          var lotRes = await get('/parking-lots/' + raw.parkingLotId)
          if (lotRes.data && lotRes.data.hourlyRate) {
            rate = Number(lotRes.data.hourlyRate)
          }
        } catch (e) {}
      }
      var isFirst = true
      if (raw.parkingLotId) {
        try {
          var ordersRes = await get('/orders')
          var allOrders = ordersRes.data || []
          for (var i = 0; i < allOrders.length; i++) {
            var o = allOrders[i]
            if (o.id !== raw.id && o.parkingLotId === raw.parkingLotId && (o.status === 1 || o.status === 3)) {
              isFirst = false
              break
            }
          }
        } catch (e) {}
      }
      const order = {
        ...raw,
        startTimeRaw: raw.startTime,
        startTime: raw.startTime ? formatDate(raw.startTime, 'YYYY-MM-DD HH:mm') : '--'
      }
      this.setData({ order: order, hourlyRate: rate, isFirstParking: isFirst })
      this.startTimer(raw.startTime)
      hideLoading()
      console.log('[成功][阶段2][加载订单] 时间：' + Date.now() + ' | 参数：id=' + id + ',isFirstParking=' + isFirst + ' | 结果：加载成功')
    } catch (e) {
      hideLoading()
      showError(e.message || '加载失败')
      console.log('[失败][阶段2][加载订单] 时间：' + Date.now() + ' | 原因：' + (e.message || '请求失败') + ' | 参数：id=' + id)
    }
  },
  calcEstimatedFee(durationMinutes) {
    var rate = this.data.hourlyRate
    var isFirst = this.data.isFirstParking
    if (isFirst && durationMinutes <= FREE_DURATION_MINUTES) {
      return { fee: '0.00', isFree: true }
    }
    var hours = durationMinutes / 60
    var fee = (hours * rate).toFixed(2)
    return { fee: fee, isFree: false }
  },
  startTimer(startTime) {
    if (!startTime) {
      this.setData({ timer: '00:00:00', seconds: 0, estimatedFee: '0.00', isFree: true })
      return
    }
    const start = new Date(startTime).getTime()
    const update = () => {
      const diff = Math.floor((Date.now() - start) / 1000)
      const h = Math.floor(diff / 3600)
      const m = Math.floor((diff % 3600) / 60)
      const s = diff % 60
      var durationMinutes = Math.floor(diff / 60)
      var feeResult = this.calcEstimatedFee(durationMinutes)
      this.setData({
        seconds: diff,
        timer: [h, m, s].map(v => String(v).padStart(2, '0')).join(':'),
        estimatedFee: feeResult.fee,
        isFree: feeResult.isFree
      })
    }
    update()
    const interval = setInterval(update, 1000)
    this.setData({ timerInterval: interval })
    console.log('[成功][阶段2][启动计时] 时间：' + Date.now() + ' | 参数：startTime=' + startTime + ' | 结果：计时器已启动')
  },
  goBack() {
    console.log('[成功][阶段5][返回操作] 时间：' + Date.now() + ' | 参数：无 | 结果：返回上一页')
    safeNavigateBack({ fallbackUrl: '/pages/orders/orders' })
  },
  async handlePay() {
    const order = this.data.order
    if (!order) return
    console.log('[成功][阶段2][进入确认支付] 时间：' + Date.now() + ' | 参数：orderId=' + order.id + ' | 结果：跳转确认支付页')
    wx.navigateTo({ url: '/pages/order-detail/order-detail?id=' + order.id })
  }
})
