const app = getApp()
const { get, put, del } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, showConfirm } = require('../../utils/util')

Page({
  data: {
    vehicles: [],
    loading: false
  },

  onLoad() {
    console.log('[成功][阶段1][我的车辆页加载] 时间：' + Date.now())
  },

  onShow() {
    if (!app.isLoggedIn()) {
      console.log('[失败][阶段1][我的车辆页] 时间：' + Date.now() + ' | 原因：用户未登录 | 参数：无')
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    this.loadVehicles()
  },

  async loadVehicles() {
    this.setData({ loading: true })
    try {
      const res = await get('/vehicles')
      var list = (res.data || []).map(function (v) {
        var plate = v.plateNumber || ''
        if (plate.length > 2) {
          plate = plate.substring(0, 1) + ' · ' + plate.substring(1)
        }
        return Object.assign({}, v, { displayPlate: plate })
      })
      this.setData({ vehicles: list, loading: false })
      console.log('[成功][阶段2][加载车辆列表] 时间：' + Date.now() + ' | 结果：共' + list.length + '辆')
    } catch (error) {
      this.setData({ vehicles: [], loading: false })
      console.log('[失败][阶段2][加载车辆列表] 原因：' + (error.message || '请求失败'))
    }
  },

  goAddVehicle() {
    wx.navigateTo({ url: '/pages/add-vehicle/add-vehicle' })
  },

  onEditVehicle(e) {
    var item = e.currentTarget.dataset.item
    console.log('[成功][阶段3][编辑车辆] plateNumber=' + item.plateNumber)
    wx.navigateTo({
      url: '/pages/add-vehicle/add-vehicle?id=' + encodeURIComponent(item.id || '') +
           '&plate=' + encodeURIComponent(item.plateNumber || '') +
           '&brand=' + encodeURIComponent(item.brand || '') +
           '&color=' + encodeURIComponent(item.color || '') +
           '&isDefault=' + encodeURIComponent(item.isDefault || 0)
    })
  },

  async onDeleteVehicle(e) {
    var id = e.currentTarget.dataset.id
    var plate = e.currentTarget.dataset.plate
    const confirmed = await showConfirm('确认删除「' + plate + '」？删除后不可恢复')
    if (!confirmed) return
    try {
      await del('/vehicles/' + id)
      showSuccess('删除成功')
      this.loadVehicles()
    } catch (error) {
      showError(error.message || '删除失败')
    }
  },

  async onSetDefault(e) {
    var id = e.currentTarget.dataset.id
    try {
      await put('/vehicles/' + id + '/default', {})
      showSuccess('已设为默认')
      this.loadVehicles()
    } catch (error) {
      showError(error.message || '操作失败')
    }
  }
})
