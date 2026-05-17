const app = getApp()
const { post, put } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, isValidPlateNumber, safeNavigateBack } = require('../../utils/util')

Page({
  data: {
    statusBarHeight: 20,
    editId: null,
    isEdit: false,
    plateNumber: '',
    brand: '',
    selectedColor: '',
    isCustomColor: false,
    isDefault: true,
    submitting: false,
    colors: [
      { value: '白色', bg: '#FFFFFF', border: '#e5e7eb' },
      { value: '黑色', bg: '#1F2937' },
      { value: '银色', bg: '#C0C0C0' },
      { value: '灰色', bg: '#6B7280' },
      { value: '红色', bg: '#EF4444' },
      { value: '蓝色', bg: '#3B82F6' },
      { value: '绿色', bg: '#10B981' },
      { value: '黄色', bg: '#F59E0B' }
    ]
  },

  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync()
    const editId = options && options.id ? Number(options.id) : null
    const isEdit = !!editId
    this.setData({
      statusBarHeight: sysInfo.statusBarHeight || 20,
      editId: editId,
      isEdit: isEdit,
      plateNumber: options && options.plate ? decodeURIComponent(options.plate).toUpperCase() : '',
      brand: options && options.brand ? decodeURIComponent(options.brand) : '',
      selectedColor: options && options.color ? decodeURIComponent(options.color) : '',
      isDefault: options && options.isDefault ? Number(options.isDefault) === 1 : true
    })
    this.touchStartX = 0
    this.touchStartY = 0
    this.isEdgePan = false
    console.log('[成功][阶段1][添加车辆页加载] 时间：' + Date.now() + ' | 参数：isEdit=' + isEdit + ',id=' + (editId || ''))
  },

  goBack() {
    safeNavigateBack({ fallbackUrl: '/pages/profile/profile' })
  },

  onTouchStart(e) {
    const touch = e.touches && e.touches[0]
    if (!touch) return
    this.touchStartX = touch.clientX
    this.touchStartY = touch.clientY
    this.isEdgePan = touch.clientX <= 36
  },

  onTouchEnd(e) {
    if (!this.isEdgePan || this.data.submitting) {
      this.isEdgePan = false
      return
    }
    const touch = e.changedTouches && e.changedTouches[0]
    if (!touch) return
    const deltaX = touch.clientX - this.touchStartX
    const deltaY = Math.abs(touch.clientY - this.touchStartY)
    this.isEdgePan = false
    if (deltaX > 90 && deltaY < 60) {
      console.log('[成功][阶段3][右滑返回] 时间：' + Date.now() + ' | 结果：触发返回')
      safeNavigateBack({ fallbackUrl: '/pages/profile/profile' })
    }
  },

  onPlateInput(e) {
    this.setData({ plateNumber: e.detail.value.toUpperCase() })
  },

  onBrandInput(e) {
    this.setData({ brand: e.detail.value })
  },

  onColorTap(e) {
    this.setData({ selectedColor: e.currentTarget.dataset.value, isCustomColor: false })
  },

  onCustomColorTap() {
    this.setData({ isCustomColor: !this.data.isCustomColor, selectedColor: '' })
  },

  onDefaultToggle() {
    this.setData({ isDefault: !this.data.isDefault })
  },

  async handleSubmit() {
    var that = this
    var plate = (this.data.plateNumber || '').trim()
    var isEdit = this.data.isEdit
    var editId = this.data.editId
    if (!plate) {
      showError('请输入车牌号')
      return
    }
    if (!isValidPlateNumber(plate)) {
      showError('车牌号格式不正确，如：京A12345')
      return
    }

    this.setData({ submitting: true })
    showLoading(isEdit ? '保存中...' : '提交中...')

    try {
      var payload = {
        plateNumber: plate,
        brand: this.data.brand || null,
        color: this.data.selectedColor || null,
        isDefault: this.data.isDefault ? 1 : 0
      }
      if (isEdit && editId) {
        await put('/vehicles/' + editId, payload)
      } else {
        await post('/vehicles', payload)
      }
      hideLoading()
      showSuccess(isEdit ? '保存成功' : '添加成功')
      console.log('[成功][阶段4][' + (isEdit ? '编辑车辆' : '添加车辆') + '] 时间：' + Date.now() + ' | 参数：plateNumber=' + plate + ' | 结果：成功')
      setTimeout(function () {
        var pages = getCurrentPages()
        if (pages.length >= 2) {
          var prevPage = pages[pages.length - 2]
          if (prevPage && typeof prevPage.loadVehicles === 'function') {
            prevPage.loadVehicles()
          }
        }
        safeNavigateBack({ fallbackUrl: '/pages/profile/profile' })
      }, 800)
    } catch (error) {
      hideLoading()
      that.setData({ submitting: false })
      showError(error.message || (isEdit ? '保存失败' : '添加失败'))
      console.log('[失败][阶段4][' + (isEdit ? '编辑车辆' : '添加车辆') + '] 时间：' + Date.now() + ' | 原因：' + (error.message || '未知错误'))
    }
  }
})
