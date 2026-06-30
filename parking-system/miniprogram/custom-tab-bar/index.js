Component({
  data: {
    selected: 0,
    color: '#8F96A3',
    selectedColor: '#07C160',
    list: [
      {
        pagePath: '/pages/index/index',
        text: '首页',
        iconPath: '/images/tabbar-home.svg',
        selectedIconPath: '/images/tabbar-home-active.svg'
      },
      {
        pagePath: '/pages/orders/orders',
        text: '订单',
        iconPath: '/images/tabbar-order.svg',
        selectedIconPath: '/images/tabbar-order-active.svg'
      },
      {
        pagePath: '/pages/parking-lots/parking-lots',
        text: '发现',
        iconPath: '/images/tabbar-discover.svg',
        selectedIconPath: '/images/tabbar-discover-active.svg'
      },
      {
        pagePath: '/pages/profile/profile',
        text: '我的',
        iconPath: '/images/tabbar-profile.svg',
        selectedIconPath: '/images/tabbar-profile-active.svg'
      }
    ]
  },
  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset
      const url = data.path
      const index = data.index
      if (typeof index === 'number') {
        this.setData({ selected: index })
      }
      wx.switchTab({ url })
    },
    onScanTap() {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      if (currentPage && typeof currentPage.onScanTap === 'function') {
        currentPage.onScanTap()
        return
      }
      if (currentPage && typeof currentPage.handleScanPay === 'function') {
        currentPage.handleScanPay()
        return
      }
      wx.scanCode({
        onlyFromCamera: false,
        scanType: ['qrCode', 'barCode'],
        success(res) {
          wx.navigateTo({ url: '/pages/parking-detail/parking-detail?code=' + res.result })
        }
      })
    }
  }
})
