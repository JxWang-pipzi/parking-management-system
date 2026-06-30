const app = getApp()
const { get } = require('../../utils/request')
const wsManager = require('../../utils/websocket')

const NEARBY_RADIUS_METERS = 5000

function log(stage, status, operation, params, result) {
  const now = Date.now()
  if (status === '成功') {
    console.log('[成功][阶段' + stage + '][' + operation + '] 时间：' + now + ' | 参数：' + params + ' | 结果：' + result)
  } else {
    console.log('[失败][阶段' + stage + '][' + operation + '] 时间：' + now + ' | 原因：' + result + ' | 参数：' + params)
  }
}

function toRad(value) {
  return value * Math.PI / 180
}

function calcStraightDistanceMeters(from, lot) {
  var lat = Number(lot.latitude || lot.lat)
  var lng = Number(lot.longitude || lot.lng)
  if (!from || !lat || !lng) return 0
  var earthRadius = 6371000
  var dLat = toRad(lat - from.latitude)
  var dLng = toRad(lng - from.longitude)
  var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(from.latitude)) * Math.cos(toRad(lat)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  return Math.round(earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)))
}

function formatDistance(meters) {
  if (!meters) return '未知距离'
  return meters >= 1000 ? (meters / 1000).toFixed(meters >= 10000 ? 0 : 1) + '公里' : meters + '米'
}

function normalizeParkingLots(rawList, location, options) {
  var strictRadius = options && options.strictRadius
  var banners = ['/images/parking_building.jpg', '/images/parking_underground.jpg', '/images/parking_outdoor.jpg']
  return (rawList || []).map(function (lot) {
    var validDistance = !!(lot.routeDistanceMeters || lot.routeDistanceText)
    var straightDistance = calcStraightDistanceMeters(location, lot)
    var dist = lot.routeDistanceMeters || (typeof lot.distance === 'number' ? lot.distance : straightDistance)
    var distText = lot.routeDistanceText || (validDistance ? formatDistance(dist) : '未知距离')
    if (!validDistance && straightDistance) {
      validDistance = true
      distText = formatDistance(straightDistance)
    }
    var avail = lot.availableSpaces || 0
    var total = lot.totalSpaces || 1
    var ratio = avail / total
    var statusType = 'plenty'
    var statusText = '空位充足'
    if (avail <= 0) {
      statusType = 'full'
      statusText = '已满'
    } else if (ratio < 0.2) {
      statusType = 'scarce'
      statusText = '空位紧张'
    } else if (ratio < 0.5) {
      statusType = 'normal'
      statusText = '空位较少'
    }
    var hash = 0
    var name = lot.name || ''
    for (var j = 0; j < name.length; j++) { hash = ((hash << 5) - hash) + name.charCodeAt(j); hash = hash & hash }
    var bannerImage = banners[Math.abs(hash) % banners.length]
    return Object.assign({}, lot, {
      distance: distText,
      distanceValue: dist || Number.MAX_SAFE_INTEGER,
      distanceText: distText,
      validDistance: validDistance,
      statusType: statusType,
      statusText: statusText,
      walkTime: lot.routeDurationText || '',
      bannerImage: bannerImage
    })
  }).filter(function (lot) {
    return !strictRadius || (lot.validDistance && lot.distanceValue <= NEARBY_RADIUS_METERS)
  }).sort(function (a, b) {
    if (!a.validDistance) return 1
    if (!b.validDistance) return -1
    return a.distanceValue - b.distanceValue
  })
}

Page({
  data: {
    statusBarHeight: 20,
    locationText: '定位中...',
    activeTab: 'distance',
    quickEntries: [
      { type: 'nearbyParking', label: '附近停车', icon: '/images/btn-nearby-parking.png' },
      { type: 'scan', label: '扫码缴费', icon: '/images/btn-pay.png' },
      { type: 'orders', label: '订单记录', icon: '/images/order-active.png' },
      { type: 'vehicles', label: '我的车辆', icon: '/images/btn-add-car.png' },
      { type: 'coupon', label: '优惠券', icon: '/images/btn-coupon.png' },
      { type: 'monthCard', label: '月卡中心', icon: '/images/btn-month-card.png' },
      { type: 'charge', label: '充电桩', icon: '/images/btn-charge.png' },
      { type: 'service', label: '客服帮助', icon: '/images/profile-help.svg' }
    ],
    banners: [
      { id: 1, image: '/images/banner_parking_1.jpg' },
      { id: 2, image: '/images/banner_parking_2.jpg' },
      { id: 3, image: '/images/banner_parking_3.jpg' }
    ],
    parkingLots: [],
    displayList: [],
    loading: false,
    location: null,
    bannerDefault: '/images/parking_building.jpg'
  },

  onLoad() {
    log(1, '成功', '页面加载', 'index', '开始初始化')
    this.getSystemInfo()
    this.checkLoginAndLoad()
  },

  onShow() {
    this.updateTabBar()
    if (this.data.location) {
      this.loadParkingLots()
    }
    this._connectWS()
  },

  onHide() {
    this._disconnectWS()
  },

  onUnload() {
    this._disconnectWS()
  },

  onPullDownRefresh() {
    log(1, '成功', '下拉刷新', '', '触发刷新')
    this.getLocation().finally(function () {
      wx.stopPullDownRefresh()
    })
  },

  getSystemInfo() {
    try {
      const sysInfo = wx.getSystemInfoSync()
      this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
      log(1, '成功', '获取系统信息', '', 'statusBarHeight=' + sysInfo.statusBarHeight)
    } catch (e) {
      log(1, '失败', '获取系统信息', '', e.message || '未知错误')
    }
  },

  checkLoginAndLoad() {
    var token = wx.getStorageSync('token')
    if (token) {
      log(1, '成功', '登录校验', 'token存在', '校验通过')
    } else {
      log(1, '成功', '登录校验', 'token为空', '未登录，允许浏览')
    }
    this.getLocation()
  },

  getLocation() {
    var that = this
    return new Promise(function (resolve) {
      log(1, '成功', '获取定位', '', '开始定位')
      wx.getLocation({
        type: 'gcj02',
        success: function (res) {
          var location = { latitude: res.latitude, longitude: res.longitude }
          that.setData({
            location: location,
            locationText: '定位中...'
          })
          log(1, '成功', '获取定位', 'latitude=' + res.latitude + ',longitude=' + res.longitude, '定位成功')
          that.resolveLocationText(location)
          that.loadParkingLots()
          resolve(res)
        },
        fail: function (err) {
          var defaultLocation = { latitude: 30.5728, longitude: 104.0668, isFallback: true }
          that.setData({
            location: defaultLocation,
            locationText: '定位未授权，显示演示车场'
          })
          log(1, '失败', '获取定位', '', err.errMsg || '定位失败，使用默认位置')
          if (err && /auth deny|authorize|denied/i.test(err.errMsg || '')) {
            wx.showModal({
              title: '需要定位授权',
              content: '授权后可展示你附近5公里内的停车场；未授权时将显示演示停车场。',
              confirmText: '去授权',
              cancelText: '先看看',
              success: function (modalRes) {
                if (modalRes.confirm) {
                  wx.openSetting({
                    success: function (settingRes) {
                      if (settingRes.authSetting && settingRes.authSetting['scope.userLocation']) {
                        that.getLocation()
                      }
                    }
                  })
                }
              }
            })
          }
          that.loadParkingLots()
          resolve()
        }
      })
    })
  },

  async resolveLocationText(location) {
    if (!location || location.isFallback) return
    try {
      var res = await get('/geo/location-summary', {
        latitude: location.latitude,
        longitude: location.longitude
      })
      var displayName = res.data && res.data.displayName
      if (displayName) {
        this.setData({ locationText: displayName })
        log(1, '成功', '解析定位文案', 'latitude=' + location.latitude + ',longitude=' + location.longitude, 'displayName=' + displayName)
      } else {
        this.setData({ locationText: '当前位置' })
      }
    } catch (e) {
      this.setData({ locationText: '当前位置' })
      log(1, '失败', '解析定位文案', '', e.message || '位置解析失败')
    }
  },

  async loadParkingLots() {
    var location = this.data.location
    if (!location) {
      log(2, '失败', '加载停车场', 'location为空', '无法加载')
      return
    }

    this.setData({ loading: true })
    log(2, '成功', '加载停车场', 'latitude=' + location.latitude + ',longitude=' + location.longitude, '开始请求')

    try {
      var res = await get('/parking-lots/nearby', {
        latitude: location.latitude,
        longitude: location.longitude,
        radius: NEARBY_RADIUS_METERS
      })
      var list = normalizeParkingLots(res.data || [], location, { strictRadius: !location.isFallback })

      if (list.length === 0) {
        log(2, '成功', '附近停车场为空', 'radius=' + NEARBY_RADIUS_METERS, '回退旧列表接口')
        var fallbackRes = await get('/parking-lots', {
          latitude: location.latitude,
          longitude: location.longitude
        })
        list = normalizeParkingLots(fallbackRes.data || [], location, { strictRadius: false })
      }

      this.setData({ parkingLots: list.slice(0, 10), displayList: list.slice(0, 10), loading: false })
      log(2, '成功', '加载停车场', 'API请求', '获取' + list.length + '条数据')
    } catch (error) {
      log(2, '失败', '加载停车场', 'API请求', error.message || '请求失败')
      this.setData({ parkingLots: [], loading: false })
      wx.showToast({ title: '加载停车场失败', icon: 'none' })
    }
  },

  onSearchTap() {
    log(2, '成功', '点击搜索栏', '', '跳转停车场列表页')
    wx.switchTab({ url: '/pages/parking-lots/parking-lots' })
  },

  onBannerTap(e) {
    var item = e.currentTarget.dataset.item
    log(3, '成功', '点击轮播图', 'id=' + item.id, '无跳转链接')
  },

  onEntryTap(e) {
    var type = e.currentTarget.dataset.type
    log(2, '成功', '点击快捷入口', 'type=' + type, '开始跳转')

    switch (type) {
      case 'nearbyParking':
        wx.switchTab({ url: '/pages/parking-lots/parking-lots' })
        break
      case 'coupon':
        wx.showToast({ title: '功能开发中', icon: 'none' })
        break
      case 'vehicles':
        wx.navigateTo({ url: '/pages/my-vehicles/my-vehicles' })
        break
      case 'monthCard':
        wx.showToast({ title: '功能开发中', icon: 'none' })
        break
      case 'charge':
        wx.showToast({ title: '功能开发中', icon: 'none' })
        break
      case 'service':
        wx.showToast({ title: '客服功能开发中', icon: 'none' })
        break
      case 'orders':
        wx.switchTab({ url: '/pages/orders/orders' })
        break
      case 'scan':
        this.handleScanPay()
        break
      default:
        log(2, '失败', '点击快捷入口', 'type=' + type, '未知入口类型')
    }
  },

  onLocationTap() {
    log(2, '成功', '点击位置选择器', '', '切换位置')
    var that = this
    wx.chooseLocation({
        success: function (res) {
          var locationName = res.name || res.address || '已选择位置'
          that.setData({
            locationText: locationName,
            location: {
              latitude: res.latitude,
              longitude: res.longitude
            }
          })
          that.loadParkingLots()
          console.log('[成功][阶段2][选择位置] 时间：' + Date.now() + ' | 参数：' + res.address)
        }
    })
  },

  onMessageTap() {
    log(2, '成功', '点击消息图标', '', '暂无消息')
    wx.showToast({ title: '暂无消息', icon: 'none' })
  },

  onScanTap() {
    this.handleScanPay()
  },

  onFilterTab(e) {
    var tab = e.currentTarget.dataset.tab
    log(2, '成功', '切换筛选标签', 'tab=' + tab, '筛选已切换')
    this.setData({ activeTab: tab })
    this.applyFilter()
  },

  applyFilter() {
    var tab = this.data.activeTab
    var list = this.data.parkingLots.slice()
    if (tab === 'price') {
      list.sort(function (a, b) { return (a.hourlyRate || 0) - (b.hourlyRate || 0) })
    } else if (tab === 'space') {
      list.sort(function (a, b) { return (b.availableSpaces || 0) - (a.availableSpaces || 0) })
    } else {
      list.sort(function (a, b) { return (a.distanceValue || 0) - (b.distanceValue || 0) })
    }
    this.setData({ displayList: list.slice(0, 10) })
  },

  onFilterMore() {
    log(2, '成功', '点击更多筛选', '', '展开更多选项')
    wx.showToast({ title: '更多筛选开发中', icon: 'none' })
  },

  handleScanPay() {
    log(2, '成功', '扫码支付', '', '开始扫码')
    var that = this
    wx.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode', 'barCode'],
      success: function (res) {
        log(2, '成功', '扫码支付', 'result=' + res.result, '扫码成功')
        wx.navigateTo({ url: '/pages/parking-detail/parking-detail?code=' + res.result })
      },
      fail: function (err) {
        log(2, '失败', '扫码支付', '', err.errMsg || '扫码取消或失败')
      }
    })
  },

  onParkingTap(e) {
    var id = e.currentTarget.dataset.id
    log(2, '成功', '点击停车场卡片', 'id=' + id, '跳转详情页')
    wx.navigateTo({ url: '/pages/parking-detail/parking-detail?id=' + id + '&mode=basic' })
  },

  onViewMoreTap() {
    log(3, '成功', '点击查看更多', '', '跳转停车场列表')
    wx.switchTab({ url: '/pages/parking-lots/parking-lots' })
  },

  onImageError(e) {
    var index = e.currentTarget.dataset.index
    var displayList = this.data.displayList.slice()
    if (index >= 0 && index < displayList.length) {
      var fallbackImages = ['/images/parking_building.jpg', '/images/parking_underground.jpg', '/images/parking_outdoor.jpg']
      displayList[index].bannerImage = fallbackImages[index % fallbackImages.length]
      this.setData({ displayList: displayList })
      log(2, '失败', '图片加载', 'index=' + index, '已切换到备用图片')
    }
  },

  _connectWS: function () {
    var that = this
    if (!wsManager.isConnected()) {
      wsManager.connect()
    }
    wsManager.off('parking_lot_update')
    wsManager.off('space_update')
    wsManager.on('parking_lot_update', function (data) {
      console.log('[成功][阶段2][WebSocket停车场更新] 时间：' + Date.now() + ' | 参数：lotId=' + data.lotId + ' | 结果：自动刷新停车场列表')
      that.loadParkingLots()
    })
    wsManager.on('space_update', function (data) {
      console.log('[成功][阶段2][WebSocket车位更新] 时间：' + Date.now() + ' | 参数：spaceId=' + data.spaceId + ' | 结果：自动刷新停车场列表')
      that.loadParkingLots()
    })
  },

  _disconnectWS: function () {
    wsManager.off('parking_lot_update')
    wsManager.off('space_update')
  },

  updateTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 })
    }
  }
})
