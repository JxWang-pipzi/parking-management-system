const { get } = require('../../utils/request')
const wsManager = require('../../utils/websocket')

const NEARBY_RADIUS_METERS = 5000

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
  const banners = ['/images/parking_building.jpg', '/images/parking_underground.jpg', '/images/parking_outdoor.jpg']
  return (rawList || []).map(lot => {
    const validDistance = !!(lot.routeDistanceMeters || lot.routeDistanceText)
    var straightDistance = calcStraightDistanceMeters(location, lot)
    const distance = lot.routeDistanceMeters || (typeof lot.distance === 'number' ? lot.distance : straightDistance)
    var finalValidDistance = validDistance
    var distText = lot.routeDistanceText || (validDistance ? formatDistance(distance) : '未知距离')
    if (!finalValidDistance && straightDistance) {
      finalValidDistance = true
      distText = formatDistance(straightDistance)
    }
    const avail = lot.availableSpaces || 0
    const total = lot.totalSpaces || 1
    const ratio = avail / total
    let statusType = 'plenty'
    let statusText = '空位充足'
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
    for (var i = 0; i < name.length; i++) { hash = ((hash << 5) - hash) + name.charCodeAt(i); hash = hash & hash }
    var bannerImage = banners[Math.abs(hash) % banners.length]
    return {
      ...lot,
      distance: distText,
      distanceValue: distance || Number.MAX_SAFE_INTEGER,
      validDistance: finalValidDistance,
      statusType: statusType,
      statusText: statusText,
      walkTime: lot.routeDurationText || '',
      bannerImage: bannerImage
    }
  }).filter(lot => !strictRadius || (lot.validDistance && lot.distanceValue <= NEARBY_RADIUS_METERS))
}

Page({
  data: {
    statusBarHeight: 20,
    keyword: '',
    filterType: 'all',
    parkingLots: [],
    filteredList: [],
    loading: false,
    refreshing: false,
    location: null
  },

  onLoad() {
    console.log('[成功][阶段1][页面加载] 时间：' + Date.now() + ' | 参数：无 | 结果：页面初始化')
    const sysInfo = wx.getSystemInfoSync()
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
    this.getCurrentLocation()
  },

  onShow() {
    this.updateTabBar()
    if (this.data.location) {
      this.fetchParkingLots(false)
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
    console.log('[成功][阶段3][下拉刷新] 时间：' + Date.now() + ' | 参数：无 | 结果：触发刷新')
    this.fetchParkingLots(true).finally(function () {
      wx.stopPullDownRefresh()
    })
  },

  onRefresh() {
    this.setData({ refreshing: true })
    this.fetchParkingLots(true).finally(() => {
      this.setData({ refreshing: false })
    })
  },

  getCurrentLocation() {
    console.log('[成功][阶段1][获取位置] 时间：' + Date.now() + ' | 参数：无 | 结果：开始定位')
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        console.log('[成功][阶段1][获取位置] 时间：' + Date.now() + ' | 参数：无 | 结果：lat=' + res.latitude + ',lng=' + res.longitude)
        this.setData({
          location: {
            latitude: res.latitude,
            longitude: res.longitude
          }
        })
        this.fetchParkingLots()
      },
      fail: () => {
        console.log('[失败][阶段1][获取位置] 时间：' + Date.now() + ' | 原因：定位失败，使用默认位置 | 参数：无')
        this.setData({
          location: {
            latitude: 30.5728,
            longitude: 104.0668,
            isFallback: true
          }
        })
        this.fetchParkingLots()
      }
    })
  },

  async fetchParkingLots(showLoading = true) {
    const { location } = this.data
    if (!location) return

    if (showLoading) {
      this.setData({ loading: true })
    }
    console.log('[成功][阶段2][请求停车场] 时间：' + Date.now() + ' | 参数：/parking-lots | 结果：开始请求')

    try {
      const res = await get('/parking-lots/nearby', {
        latitude: location.latitude,
        longitude: location.longitude,
        radius: NEARBY_RADIUS_METERS
      })

      var parkingLots = normalizeParkingLots(res.data || [], location, { strictRadius: !location.isFallback })
      if (parkingLots.length === 0) {
        console.log('[成功][阶段2][附近停车场为空] 时间：' + Date.now() + ' | 参数：radius=' + NEARBY_RADIUS_METERS + ' | 结果：回退旧列表接口')
        var fallbackRes = await get('/parking-lots', {
          latitude: location.latitude,
          longitude: location.longitude
        })
        parkingLots = normalizeParkingLots(fallbackRes.data || [], location, { strictRadius: false })
      }

      console.log('[成功][阶段2][请求停车场] 时间：' + Date.now() + ' | 参数：/parking-lots | 结果：获取' + parkingLots.length + '条数据')
      this.setData({ parkingLots, loading: false })
      this.applyFilter()
    } catch (error) {
      console.log('[失败][阶段2][请求停车场] 时间：' + Date.now() + ' | 原因：' + (error.message || '请求失败') + ' | 参数：/parking-lots')
      this.setData({ parkingLots: [], filteredList: [], loading: false })
      wx.showToast({ title: '加载停车场失败', icon: 'none' })
    }
  },

  applyFilter() {
    const { parkingLots, keyword, filterType } = this.data
    let list = [...parkingLots]

    if (keyword) {
      const kw = keyword.toLowerCase()
      list = list.filter(item =>
        (item.name && item.name.toLowerCase().includes(kw)) ||
        (item.address && item.address.toLowerCase().includes(kw))
      )
      console.log('[成功][阶段3][搜索过滤] 时间：' + Date.now() + ' | 参数：keyword=' + keyword + ' | 结果：匹配' + list.length + '条')
    }

    switch (filterType) {
      case 'available':
        list = list.filter(item => item.availableSpaces > 0)
        console.log('[成功][阶段3][筛选有空位] 时间：' + Date.now() + ' | 参数：filterType=available | 结果：' + list.length + '条')
        break
      case 'distance':
        list.sort((a, b) => (a.distanceValue || 0) - (b.distanceValue || 0))
        console.log('[成功][阶段3][距离排序] 时间：' + Date.now() + ' | 参数：filterType=distance | 结果：排序完成')
        break
      case 'price':
        list.sort((a, b) => (a.hourlyRate || 0) - (b.hourlyRate || 0))
        console.log('[成功][阶段3][价格排序] 时间：' + Date.now() + ' | 参数：filterType=price | 结果：排序完成')
        break
      default:
        list.sort((a, b) => (a.distanceValue || 0) - (b.distanceValue || 0))
        break
    }

    this.setData({ filteredList: list })
  },

  onSearchInput(e) {
    const keyword = e.detail.value
    this.setData({ keyword })
    this.applyFilter()
  },

  clearSearch() {
    console.log('[成功][阶段3][清除搜索] 时间：' + Date.now() + ' | 参数：无 | 结果：keyword已清空')
    this.setData({ keyword: '' })
    this.applyFilter()
  },

  onFilterTap(e) {
    const type = e.currentTarget.dataset.type
    console.log('[成功][阶段3][切换筛选] 时间：' + Date.now() + ' | 参数：filterType=' + type + ' | 结果：筛选已切换')
    this.setData({ filterType: type })
    this.applyFilter()
  },

  navigateToDetail(e) {
    const id = e.currentTarget.dataset.id
    console.log('[成功][阶段2][跳转详情] 时间：' + Date.now() + ' | 参数：id=' + id + ' | 结果：跳转至详情页')
    wx.navigateTo({
      url: '/pages/parking-detail/parking-detail?id=' + id + '&mode=basic'
    })
  },

  onImageError(e) {
    var index = e.currentTarget.dataset.index
    var filteredList = this.data.filteredList.slice()
    if (index >= 0 && index < filteredList.length) {
      var fallbackImages = ['/images/parking_building.jpg', '/images/parking_underground.jpg', '/images/parking_outdoor.jpg']
      filteredList[index].bannerImage = fallbackImages[index % fallbackImages.length]
      this.setData({ filteredList: filteredList })
    }
  },

  _connectWS() {
    var that = this
    if (!wsManager.isConnected()) {
      wsManager.connect()
    }
    wsManager.off('parking_lot_update')
    wsManager.off('space_update')
    wsManager.on('parking_lot_update', function () {
      console.log('[成功][阶段3][发现页WebSocket刷新] 时间：' + Date.now() + ' | 结果：收到停车场更新')
      that.fetchParkingLots(false)
    })
    wsManager.on('space_update', function () {
      console.log('[成功][阶段3][发现页WebSocket刷新] 时间：' + Date.now() + ' | 结果：收到车位更新')
      that.fetchParkingLots(false)
    })
  },

  _disconnectWS() {
    wsManager.off('parking_lot_update')
    wsManager.off('space_update')
  },

  updateTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 })
    }
  }
})
