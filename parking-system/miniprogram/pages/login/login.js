const app = getApp()
const { post } = require('../../utils/request')
const { showLoading, hideLoading, showSuccess, showError, isValidPhone } = require('../../utils/util')

Page({
  data: {
    phone: '',
    password: ''
  },

  onLoad(options) {
    console.log('[成功][阶段1][页面加载] 时间：' + Date.now() + ' | 参数：' + JSON.stringify(options) + ' | 结果：登录页加载完成')
    if (app.isLoggedIn()) {
      console.log('[成功][阶段1][登录检查] 时间：' + Date.now() + ' | 参数：无 | 结果：已登录，跳转首页')
      wx.reLaunch({
        url: '/pages/index/index'
      })
    }
  },

  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value
    })
  },

  onPasswordInput(e) {
    this.setData({
      password: e.detail.value
    })
  },

  navigateToRegister() {
    console.log('[成功][阶段3][跳转注册页] 时间：' + Date.now() + ' | 参数：/pages/register/register')
    wx.navigateTo({
      url: '/pages/register/register'
    })
  },

  async onWechatLogin(e) {
    if (this.wechatLoggingIn) return
    this.wechatLoggingIn = true
    var loadingShown = false

    var profile = null
    try {
      profile = await wx.getUserProfile({
        desc: '用于展示您的微信昵称和头像'
      })
    } catch (profileError) {
      this.wechatLoggingIn = false
      console.log('[失败][阶段2][微信资料授权] 时间：' + Date.now() + ' | 原因：' + (profileError.errMsg || '用户取消授权'))
      showError('请允许获取微信昵称和头像')
      return
    }

    console.log('[成功][阶段2][微信登录] 时间：' + Date.now() + ' | 参数：getUserProfile=ok | 结果：授权成功，开始登录')
    showLoading('登录中...')
    loadingShown = true

    try {
      const loginRes = await new Promise(function (resolve, reject) {
        wx.login({
          success: resolve,
          fail: reject
        })
      })
      const code = loginRes.code
      const res = await post('/users/wechat-login', {
        code: code,
        nickname: profile.userInfo.nickName,
        avatarUrl: profile.userInfo.avatarUrl,
        gender: profile.userInfo.gender,
        city: profile.userInfo.city,
        province: profile.userInfo.province
      })

      app.setUserInfo(res.data.user, res.data.token)
      console.log('[成功][阶段4][微信登录] 时间：' + Date.now() + ' | 参数：code=' + code + ' | 结果：登录成功，userId=' + (res.data.user.id || 'unknown'))
      showSuccess('登录成功')

      setTimeout(() => {
        wx.reLaunch({
          url: '/pages/index/index'
        })
      }, 1500)
    } catch (error) {
      console.log('[失败][阶段4][微信登录] 时间：' + Date.now() + ' | 原因：' + (error.message || '未知错误') + ' | 参数：wechat-login')
      showError(error.message || '微信登录失败')
    } finally {
      if (loadingShown) {
        hideLoading()
      }
      this.wechatLoggingIn = false
    }
  },

  async handleLogin() {
    const { phone, password } = this.data

    if (!phone) {
      console.log('[失败][阶段2][输入校验] 时间：' + Date.now() + ' | 原因：手机号为空 | 参数：phone=空')
      showError('请输入手机号')
      return
    }

    if (!isValidPhone(phone)) {
      console.log('[失败][阶段2][输入校验] 时间：' + Date.now() + ' | 原因：手机号格式不正确 | 参数：phone=' + phone.substring(0, 3) + '****')
      showError('手机号格式不正确')
      return
    }

    if (!password) {
      console.log('[失败][阶段2][输入校验] 时间：' + Date.now() + ' | 原因：密码为空 | 参数：phone=' + phone.substring(0, 3) + '****')
      showError('请输入密码')
      return
    }

    if (password.length < 6) {
      console.log('[失败][阶段2][输入校验] 时间：' + Date.now() + ' | 原因：密码长度不足6位 | 参数：phone=' + phone.substring(0, 3) + '****')
      showError('密码长度至少6位')
      return
    }

    console.log('[成功][阶段2][输入校验] 时间：' + Date.now() + ' | 参数：phone=' + phone.substring(0, 3) + '**** | 结果：校验通过')
    showLoading('登录中...')

    try {
      const res = await post('/users/login', {
        username: phone,
        password: password
      })

      app.setUserInfo(res.data.user, res.data.token)
      console.log('[成功][阶段4][账号登录] 时间：' + Date.now() + ' | 参数：username=' + phone.substring(0, 3) + '**** | 结果：登录成功，userId=' + (res.data.user.id || 'unknown'))

      hideLoading()
      showSuccess('登录成功')

      setTimeout(() => {
        wx.reLaunch({
          url: '/pages/index/index'
        })
      }, 1500)
    } catch (error) {
      console.log('[失败][阶段4][账号登录] 时间：' + Date.now() + ' | 原因：' + (error.message || '未知错误') + ' | 参数：username=' + phone.substring(0, 3) + '****')
      hideLoading()
      showError(error.message || '登录失败')
    }
  }
})
