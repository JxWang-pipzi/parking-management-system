// utils/util.js
// 工具函数

/**
 * 格式化日期
 * @param {Date|string|number} date 日期对象或时间戳
 * @param {string} format 格式化模板
 * @returns {string} 格式化后的日期字符串
 */
const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''
  
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 计算两点之间的距离（单位：米）
 * @param {number} lat1 纬度1
 * @param {number} lng1 经度1
 * @param {number} lat2 纬度2
 * @param {number} lng2 经度2
 * @returns {number} 距离（米）
 */
const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const rad = (d) => d * Math.PI / 180.0
  const EARTH_RADIUS = 6378137 // 地球半径（米）
  
  const radLat1 = rad(lat1)
  const radLat2 = rad(lat2)
  const radLng1 = rad(lng1)
  const radLng2 = rad(lng2)
  
  const a = radLat1 - radLat2
  const b = radLng1 - radLng2
  
  let distance = 2 * Math.asin(Math.sqrt(
    Math.pow(Math.sin(a / 2), 2) +
    Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)
  ))
  
  distance = distance * EARTH_RADIUS
  return Math.round(distance)
}

/**
 * 格式化距离
 * @param {number} distance 距离（米）
 * @returns {string} 格式化后的距离字符串
 */
const formatDistance = (distance) => {
  if (distance < 1000) {
    return `${distance}米`
  } else {
    return `${(distance / 1000).toFixed(1)}公里`
  }
}

/**
 * 格式化金额
 * @param {number} amount 金额
 * @returns {string} 格式化后的金额字符串
 */
const formatMoney = (amount) => {
  return `¥${amount.toFixed(2)}`
}

/**
 * 格式化时长
 * @param {number} minutes 分钟数
 * @returns {string} 格式化后的时长字符串
 */
const formatDuration = (minutes) => {
  if (minutes < 60) {
    return `${minutes}分钟`
  } else {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  }
}

/**
 * 防抖函数
 * @param {Function} fn 需要防抖的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
const debounce = (fn, delay = 300) => {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn 需要节流的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
const throttle = (fn, delay = 300) => {
  let lastTime = 0
  return function(...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

/**
 * 显示加载提示
 * @param {string} title 提示文字
 */
const showLoading = (title = '加载中...') => {
  wx.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
const hideLoading = () => {
  wx.hideLoading()
}

/**
 * 显示成功提示
 * @param {string} title 提示文字
 */
const showSuccess = (title) => {
  wx.showToast({
    title,
    icon: 'success',
    duration: 2000
  })
}

/**
 * 显示错误提示
 * @param {string} title 提示文字
 */
const showError = (title) => {
  wx.showToast({
    title,
    icon: 'error',
    duration: 2000
  })
}

/**
 * 显示确认对话框
 * @param {string} content 对话框内容
 * @param {string} title 对话框标题
 * @returns {Promise}
 */
const showConfirm = (content, title = '提示') => {
  return new Promise((resolve, reject) => {
    wx.showModal({
      title,
      content,
      success(res) {
        if (res.confirm) {
          resolve(true)
        } else {
          resolve(false)
        }
      },
      fail() {
        reject(new Error('显示对话框失败'))
      }
    })
  })
}

const TAB_BAR_PAGES = [
  '/pages/index/index',
  '/pages/orders/orders',
  '/pages/parking-lots/parking-lots',
  '/pages/profile/profile'
]

const normalizePageUrl = (url) => {
  if (!url) return ''
  const pureUrl = String(url).split('?')[0]
  return pureUrl.startsWith('/') ? pureUrl : `/${pureUrl}`
}

const safeNavigateBack = ({ delta = 1, fallbackUrl = '/pages/index/index', fallbackType = 'switchTab' } = {}) => {
  const pages = getCurrentPages()
  const normalizedFallbackUrl = normalizePageUrl(fallbackUrl)
  const currentPage = pages[pages.length - 1]
  const currentRoute = currentPage && currentPage.route ? normalizePageUrl(currentPage.route) : ''

  if (pages.length > delta) {
    const targetPage = pages[pages.length - 1 - delta]
    const targetRoute = targetPage && targetPage.route ? normalizePageUrl(targetPage.route) : ''
    if (targetRoute && !TAB_BAR_PAGES.includes(targetRoute)) {
      wx.navigateBack({ delta })
      return
    }
  }

  if (normalizedFallbackUrl && TAB_BAR_PAGES.includes(normalizedFallbackUrl)) {
    if (currentRoute !== normalizedFallbackUrl) {
      wx.switchTab({ url: normalizedFallbackUrl })
    }
    return
  }

  if (fallbackType === 'reLaunch') {
    wx.reLaunch({ url: normalizedFallbackUrl })
    return
  }

  wx.navigateTo({ url: normalizedFallbackUrl })
}

/**
 * 检查手机号格式
 * @param {string} phone 手机号
 * @returns {boolean}
 */
const isValidPhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 检查车牌号格式
 * @param {string} plateNumber 车牌号
 * @returns {boolean}
 */
const isValidPlateNumber = (plateNumber) => {
  return /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5,6}$/.test(plateNumber)
}

module.exports = {
  formatDate,
  calculateDistance,
  formatDistance,
  formatMoney,
  formatDuration,
  debounce,
  throttle,
  showLoading,
  hideLoading,
  showSuccess,
  showError,
  showConfirm,
  safeNavigateBack,
  isValidPhone,
  isValidPlateNumber
}
