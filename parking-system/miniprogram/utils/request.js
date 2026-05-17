const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    const token = app.globalData.token || wx.getStorageSync('token')
    const header = {
      'Content-Type': options.contentType || 'application/json',
      ...options.header
    }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    const startTime = Date.now()
    console.log('[成功][阶段1][请求发起] 时间：' + startTime + ' | 参数：' + options.url + ' | 方法：' + (options.method || 'GET'))

    wx.request({
      url: app.getBaseUrl() + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: options.timeout || 15000,
      success(res) {
        const elapsed = Date.now() - startTime
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            console.log('[成功][阶段4][请求成功] 时间：' + Date.now() + ' | 参数：' + options.url + ' | 结果：code=200 | 耗时：' + elapsed + 'ms')
            resolve(res.data)
          } else {
            console.log('[失败][阶段4][请求业务错误] 时间：' + Date.now() + ' | 原因：' + (res.data.message || '未知错误') + ' | 参数：' + options.url)
            if (res.data.message) {
              wx.showToast({ title: res.data.message, icon: 'none' })
            }
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          console.log('[失败][阶段4][请求认证失败] 时间：' + Date.now() + ' | 参数：' + options.url)
          app.clearUserInfo()
          wx.reLaunch({ url: '/pages/login/login' })
          reject(res.data)
        } else {
          console.log('[失败][阶段4][请求HTTP错误] 时间：' + Date.now() + ' | 原因：statusCode=' + res.statusCode + ' | 参数：' + options.url)
          wx.showToast({ title: '网络请求失败', icon: 'none' })
          reject(res)
        }
      },
      fail(err) {
        const elapsed = Date.now() - startTime
        console.log('[失败][阶段2][请求网络异常] 时间：' + Date.now() + ' | 原因：' + err.errMsg + ' | 参数：' + options.url + ' | 耗时：' + elapsed + 'ms')
        wx.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

function get(url, data) {
  return request({ url, method: 'GET', data })
}

function post(url, data, contentType) {
  return request({ url, method: 'POST', data, contentType: contentType || 'application/json' })
}

function postForm(url, data) {
  return request({ url, method: 'POST', data, contentType: 'application/x-www-form-urlencoded' })
}

function put(url, data) {
  return request({ url, method: 'PUT', data })
}

function del(url, data) {
  return request({ url, method: 'DELETE', data })
}

module.exports = {
  request,
  get,
  post,
  postForm,
  put,
  del
}
