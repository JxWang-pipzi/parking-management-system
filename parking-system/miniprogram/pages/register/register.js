const app = getApp()
const { post } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, isValidPhone, safeNavigateBack } = require('../../utils/util')

Page({
  data: {
    phone: '',
    password: '',
    confirmPassword: ''
  },

  onLoad() {
    console.log('[成功][阶段1][注册页加载] 时间：' + Date.now() + ' | 结果：注册页加载完成')
    if (app.isLoggedIn()) {
      wx.reLaunch({
        url: '/pages/index/index'
      })
    }
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  navigateToLogin() {
    safeNavigateBack({ fallbackUrl: '/pages/login/login' })
  },

  async handleRegister() {
    const { phone, password, confirmPassword } = this.data

    if (!phone) {
      showError('请输入手机号')
      return
    }
    if (!isValidPhone(phone)) {
      showError('手机号格式不正确')
      return
    }
    if (!password) {
      showError('请输入密码')
      return
    }
    if (password.length < 6) {
      showError('密码长度至少6位')
      return
    }
    if (!confirmPassword) {
      showError('请确认密码')
      return
    }
    if (password !== confirmPassword) {
      showError('两次密码输入不一致')
      return
    }

    showLoading('注册中...')
    try {
      await post('/users/register', {
        username: phone,
        password: password
      })
      hideLoading()
      showSuccess('注册成功，请登录')
      setTimeout(() => {
        safeNavigateBack({ fallbackUrl: '/pages/login/login' })
      }, 1000)
    } catch (error) {
      hideLoading()
      showError(error.message || '注册失败')
    }
  }
})
