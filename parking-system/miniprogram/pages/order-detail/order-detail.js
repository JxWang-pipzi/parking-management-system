const { get, post, put } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, showConfirm, formatDate, formatDuration, safeNavigateBack } = require('../../utils/util')

var FREE_DURATION_MINUTES = 15

const STATUS_MAP = {
  0: '待支付',
  1: '已完成',
  2: '已取消',
  3: '停车中'
}

Page({
  data: {
    statusBarHeight: 20,
    id: null,
    order: null,
    durationText: '--',
    loading: false,
    hourlyRate: 6,
    isFree: false,
    isFirstParking: true,
    selectedPaymentMethod: 1,
    paymentMethods: [
      { value: 1, label: '微信支付', iconType: 'wechat' },
      { value: 2, label: '支付宝支付', iconType: 'alipay' },
      { value: 3, label: '银行卡支付', iconType: 'bank' }
    ]
  },

  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync()
    const id = options.id
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20, id })
    if (!id) {
      showError('缺少订单参数')
      return
    }
    this.loadOrder()
  },

  onPullDownRefresh() {
    this.loadOrder().finally(function () {
      wx.stopPullDownRefresh()
    })
  },

  async loadOrder() {
    const id = this.data.id
    if (!id) return

    this.setData({ loading: true })
    showLoading('加载中...')
    try {
      const res = await get('/orders/' + id)
      const raw = res.data || {}
      const durationMinutes = this.calcDuration(raw.startTime, raw.endTime)
      var rate = 6
      if (raw.parkingLotId) {
        try {
          var lotRes = await get('/parking-lots/' + raw.parkingLotId)
          if (lotRes.data && lotRes.data.hourlyRate) {
            rate = Number(lotRes.data.hourlyRate)
          }
        } catch (e) {}
      }

      var isFirst = true
      if (raw.parkingLotId && raw.status !== 1 && raw.status !== 2) {
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

      var amountText = '0.00'
      var isFree = false
      if (raw.status === 1) {
        amountText = Number(raw.amount || 0).toFixed(2)
        isFree = Number(raw.amount || 0) === 0
      } else if (durationMinutes !== null) {
        var calcResult = this.calcEstimatedFee(durationMinutes, rate, isFirst)
        amountText = calcResult.fee
        isFree = calcResult.isFree
      }

      const order = {
        ...raw,
        statusText: STATUS_MAP[raw.status] || '未知',
        startTimeText: raw.startTime ? formatDate(raw.startTime, 'YYYY-MM-DD HH:mm') : '--',
        endTimeText: raw.endTime ? formatDate(raw.endTime, 'YYYY-MM-DD HH:mm') : '--',
        amountText: amountText
      }
      this.setData({
        order,
        durationText: durationMinutes !== null ? formatDuration(durationMinutes) : '--',
        loading: false,
        hourlyRate: rate,
        isFree: isFree,
        isFirstParking: isFirst
      })
      console.log('[成功][阶段2][订单详情] 时间：' + Date.now() + ' | 参数：id=' + id + ',isFirstParking=' + isFirst + ',isFree=' + isFree + ' | 结果：加载成功')
    } catch (e) {
      this.setData({ loading: false })
      showError(e.message || '加载失败')
      console.log('[失败][阶段2][订单详情] 时间：' + Date.now() + ' | 原因：' + (e.message || '请求失败') + ' | 参数：id=' + id)
    } finally {
      hideLoading()
    }
  },

  calcEstimatedFee(durationMinutes, rate, isFirstParking) {
    if (isFirstParking && durationMinutes <= FREE_DURATION_MINUTES) {
      return { fee: '0.00', isFree: true }
    }
    var hours = durationMinutes / 60
    var fee = (hours * rate).toFixed(2)
    return { fee: fee, isFree: false }
  },

  calcDuration(start, end) {
    if (!start) return null
    const startDate = new Date(start)
    const endDate = end ? new Date(end) : new Date()
    const diffMs = endDate.getTime() - startDate.getTime()
    if (diffMs < 0) return null
    return Math.max(1, Math.ceil(diffMs / 60000))
  },

  async payOrder() {
    const order = this.data.order
    if (!order) return

    var confirmMsg = '确认支付该订单？'
    if (this.data.isFree) {
      confirmMsg = '首次停车15分钟免费期内，确认免费离场？'
    } else if (order.status === 3) {
      confirmMsg = '确认支付¥' + order.amountText + '并离场？出场后车位将被释放'
    }

    const confirmed = await showConfirm(confirmMsg)
    if (!confirmed) return

    showLoading('支付中...')
    try {
      if (order.status === 3) {
        await put('/orders/' + order.id + '/complete')
      } else {
        await post('/orders/' + order.id + '/pay', { paymentMethod: this.data.selectedPaymentMethod })
      }
      hideLoading()
      showSuccess(this.data.isFree ? '免费离场成功' : '支付成功')
      console.log('[成功][阶段4][订单支付] 时间：' + Date.now() + ' | 参数：orderId=' + order.id + ',isFree=' + this.data.isFree + ',paymentMethod=' + this.data.selectedPaymentMethod + ' | 结果：支付成功，刷新详情页')

      var that = this
      setTimeout(function () {
        that.loadOrder()
      }, 800)
    } catch (e) {
      hideLoading()
      showError(e.message || '支付失败')
      console.log('[失败][阶段4][订单支付] 时间：' + Date.now() + ' | 原因：' + (e.message || '支付异常') + ' | 参数：orderId=' + order.id)
    }
  },

  onSelectPayment(e) {
    var value = Number(e.currentTarget.dataset.value || 1)
    this.setData({ selectedPaymentMethod: value })
    console.log('[成功][阶段3][切换支付方式] 时间：' + Date.now() + ' | 参数：paymentMethod=' + value + ' | 结果：已选择')
  },

  async cancelOrder() {
    const order = this.data.order
    if (!order) return

    const confirmed = await showConfirm('确认取消该订单？取消后车位将释放')
    if (!confirmed) return

    showLoading('取消中...')
    try {
      await put('/orders/' + order.id + '/cancel')
      hideLoading()
      showSuccess('取消成功')
      this.loadOrder()
    } catch (e) {
      hideLoading()
      showError(e.message || '取消失败')
    }
  },

  goActiveParking() {
    const order = this.data.order
    if (!order) return
    wx.navigateTo({ url: '/pages/active-parking/active-parking?id=' + order.id })
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  finishLeaving() {
    wx.switchTab({ url: '/pages/orders/orders' })
  },

  goBack() {
    safeNavigateBack({ fallbackUrl: '/pages/orders/orders' })
  }
})
