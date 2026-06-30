const app = getApp()
const { get, post } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, showConfirm, isValidPlateNumber, safeNavigateBack } = require('../../utils/util')

Page({
  data: {
    statusBarHeight: 20,
    id: null,
    parkingLot: {},
    fullStars: 5,
    availablePercent: 0,
    showReserveModal: false,
    vehiclePlate: '',
    plateDirty: false,
    reserving: false,
    bannerError: false,
    defaultVehicle: null,
    myVehicles: [],
    basicInfoOnly: false
  },

  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync()
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
    const id = options.id || options.code
    const basicInfoOnly = options.mode === 'basic'
    if (!id) {
      console.log('[失败][阶段1][页面加载] 时间：' + Date.now() + ' | 原因：缺少停车场id参数 | 参数：' + JSON.stringify(options))
      showError('参数错误')
      return
    }
    console.log('[成功][阶段1][页面加载] 时间：' + Date.now() + ' | 参数：id=' + id + ' | 结果：页面初始化成功')
    this.setData({ id, basicInfoOnly })
    this.loadParkingDetail()
  },

  async loadParkingDetail() {
    const { id } = this.data
    showLoading('加载中...')
    try {
      const res = await get('/parking-lots/' + id)
      const lot = res.data || {}
      const rating = lot.rating || 4.8
      const fullStars = Math.floor(rating)
      const total = lot.totalSpaces || 0
      const available = lot.availableSpaces || 0
      const percent = total > 0 ? Math.round((available / total) * 100) : 0
      var bannerImage = lot.image
      if (!bannerImage) {
        var banners = [
          '/images/parking_building.jpg',
          '/images/parking_underground.jpg',
          '/images/parking_outdoor.jpg'
        ]
        var hash = 0
        var name = lot.name || ''
        for (var i = 0; i < name.length; i++) {
          hash = ((hash << 5) - hash) + name.charCodeAt(i)
          hash = hash & hash
        }
        bannerImage = banners[Math.abs(hash) % banners.length]
      }
      this.setData({
        parkingLot: lot,
        fullStars,
        availablePercent: percent,
        bannerImage: bannerImage,
        bannerError: false
      })
      hideLoading()
      console.log('[成功][阶段2][获取停车场详情] 时间：' + Date.now() + ' | 参数：id=' + id + ' | 结果：' + JSON.stringify(lot))
    } catch (error) {
      hideLoading()
      showError('加载停车场信息失败')
      console.log('[失败][阶段2][获取停车场详情] 时间：' + Date.now() + ' | 原因：' + (error.message || '网络异常') + ' | 参数：id=' + id)
    }
    if (!this.data.basicInfoOnly) {
      this.loadMyVehicles()
    }
  },

  async loadMyVehicles() {
    if (!app.isLoggedIn()) return
    try {
      var res = await get('/vehicles')
      var list = res.data || []
      var defaultV = null
      for (var i = 0; i < list.length; i++) {
        if (list[i].isDefault === 1) {
          defaultV = list[i]
          break
        }
      }
      if (!defaultV && list.length > 0) {
        defaultV = list[0]
      }
      var defaultPlate = defaultV ? defaultV.plateNumber : ''
      var currentPlate = (this.data.vehiclePlate || '').trim()
      var shouldPrefillPlate = !this.data.showReserveModal || !this.data.plateDirty || !currentPlate
      this.setData({
        myVehicles: list,
        defaultVehicle: defaultV,
        vehiclePlate: shouldPrefillPlate ? defaultPlate : this.data.vehiclePlate
      })
      console.log('[成功][阶段2][加载我的车辆] 时间：' + Date.now() + ' | 结果：默认车辆=' + (defaultV ? defaultV.plateNumber : '无'))
    } catch (error) {
      this.setData({ myVehicles: [], defaultVehicle: null })
    }
  },

  onReserveTap() {
    if (this.data.basicInfoOnly) {
      this.onNavigateTap()
      return
    }
    if (!app.isLoggedIn()) {
      console.log('[失败][阶段3][预约入口] 时间：' + Date.now() + ' | 原因：用户未登录 | 参数：无')
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    const { parkingLot } = this.data
    if (parkingLot.availableSpaces <= 0) {
      showError('暂无空余车位')
      console.log('[失败][阶段3][预约入口] 时间：' + Date.now() + ' | 原因：无空余车位 | 参数：availableSpaces=' + parkingLot.availableSpaces)
      return
    }
    var plate = this.data.vehiclePlate || (this.data.defaultVehicle ? this.data.defaultVehicle.plateNumber : '')
    this.setData({ showReserveModal: true, vehiclePlate: plate, plateDirty: false })
    console.log('[成功][阶段3][预约入口] 时间：' + Date.now() + ' | 参数：parkingLotId=' + parkingLot.id + ' | 结果：弹出预约弹窗, 默认车牌=' + plate)
  },

  onSelectVehicle(e) {
    var index = e.currentTarget.dataset.index
    var v = this.data.myVehicles[index]
    if (v) {
      this.setData({ vehiclePlate: v.plateNumber, plateDirty: false })
      console.log('[成功][阶段3][选择车辆] 时间：' + Date.now() + ' | 结果：选中=' + v.plateNumber)
    }
  },

  onPlateInput(e) {
    var value = (e.detail.value || '').toUpperCase().replace(/\s+/g, '')
    this.setData({ vehiclePlate: value, plateDirty: true })
  },

  onModalCancel() {
    this.setData({
      showReserveModal: false,
      vehiclePlate: '',
      plateDirty: false,
      reserving: false
    })
    console.log('[成功][阶段3][取消预约] 时间：' + Date.now() + ' | 参数：无 | 结果：关闭预约弹窗')
  },

  async onModalConfirm() {
    const { id, vehiclePlate, parkingLot, reserving } = this.data

    if (reserving) {
      return
    }

    const normalizedPlate = (vehiclePlate || '').trim().toUpperCase().replace(/\s+/g, '')

    if (!normalizedPlate) {
      showError('请输入车牌号')
      console.log('[失败][阶段3][预约校验] 时间：' + Date.now() + ' | 原因：车牌号为空 | 参数：vehiclePlate=' + vehiclePlate)
      return
    }

    if (!isValidPlateNumber(normalizedPlate)) {
      showError('车牌号格式不正确')
      console.log('[失败][阶段3][预约校验] 时间：' + Date.now() + ' | 原因：车牌号格式错误 | 参数：vehiclePlate=' + vehiclePlate)
      return
    }

    const confirmed = await showConfirm('确认预约车位？费率：¥' + Number(parkingLot.hourlyRate || 0).toFixed(2) + '/小时')
    if (!confirmed) return

    showLoading('预约中...')
    this.setData({ reserving: true, vehiclePlate: normalizedPlate })

    try {
      const res = await post('/orders', {
        parkingLotId: id,
        plateNumber: normalizedPlate
      })
      hideLoading()
      showSuccess('停车开始')
      this.setData({
        showReserveModal: false,
        vehiclePlate: '',
        plateDirty: false,
        reserving: false
      })
      console.log('[成功][阶段3][创建订单] 时间：' + Date.now() + ' | 参数：parkingLotId=' + id + ',vehiclePlate=' + normalizedPlate + ' | 结果：' + JSON.stringify(res.data))
      var orderId = res.data && res.data.id
      setTimeout(function () {
        if (orderId) {
          wx.redirectTo({ url: '/pages/active-parking/active-parking?id=' + orderId })
        } else {
          wx.switchTab({ url: '/pages/orders/orders' })
        }
      }, 800)
    } catch (error) {
      hideLoading()
      this.setData({ reserving: false })
      showError(error.message || '预约失败')
      console.log('[失败][阶段3][创建订单] 时间：' + Date.now() + ' | 原因：' + (error.message || '未知错误') + ' | 参数：parkingLotId=' + id + ',vehiclePlate=' + normalizedPlate)
    }
  },

  onBannerError() {
    this.setData({ bannerError: true })
    console.log('[失败][阶段1][Banner加载] 时间：' + Date.now() + ' | 原因：图片加载失败 | 参数：/images/parking_detail_banner.jpg')
  },

  onNavigateTap() {
    const { parkingLot } = this.data
    wx.navigateTo({
      url: '/pages/navigation/navigation?name=' + encodeURIComponent(parkingLot.name || '朝阳大悦城停车场')
    })
    console.log('[成功][阶段3][导航前往] 时间：' + Date.now() + ' | 参数：parkingLotId=' + parkingLot.id + ' | 结果：进入导航页')
  },

  goBack() {
    safeNavigateBack({ fallbackUrl: '/pages/index/index' })
    console.log('[成功][阶段1][返回操作] 时间：' + Date.now() + ' | 结果：执行页面返回')
  },

  onFavoriteTap() {
    console.log('[成功][阶段3][收藏操作] 时间：' + Date.now() + ' | 参数：parkingLotId=' + this.data.id + ' | 结果：触发收藏功能')
    wx.showToast({
      title: '收藏成功',
      icon: 'success'
    })
  },

  showPriceDetail() {
    if (this.data.basicInfoOnly) {
      wx.showModal({
        title: '停车场信息',
        content: '当前页面仅展示周边停车场基础信息和导航。停车缴费以订单页面和后端订单数据为准。',
        showCancel: false,
        confirmText: '我知道了'
      })
      return
    }
    const { parkingLot } = this.data
    wx.showModal({
      title: '价格详情',
      content: '首小时：¥' + (parkingLot.hourlyRate || 6) + '\n之后每小时：¥' + (parkingLot.hourlyRate || 6) + '\n\n24小时封顶：¥60',
      showCancel: false,
      confirmText: '我知道了'
    })
    console.log('[成功][阶段3][查看价格详情] 时间：' + Date.now() + ' | 参数：hourlyRate=' + parkingLot.hourlyRate)
  }
})
