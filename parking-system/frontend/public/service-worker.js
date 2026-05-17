const CACHE_NAME = 'smart-parking-v1'
const STATIC_CACHE_NAME = 'smart-parking-static-v1'
const DYNAMIC_CACHE_NAME = 'smart-parking-dynamic-v1'

// 需要缓存的静态资源
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/vite.svg'
]

// 安装事件 - 缓存静态资源
self.addEventListener('install', (event) => {
  console.log('[Service Worker] 安装中...')
  event.waitUntil(
    caches.open(STATIC_CACHE_NAME)
      .then((cache) => {
        console.log('[Service Worker] 缓存静态资源')
        return cache.addAll(STATIC_ASSETS)
      })
      .then(() => {
        console.log('[Service Worker] 安装完成')
        return self.skipWaiting()
      })
      .catch((error) => {
        console.error('[Service Worker] 安装失败:', error)
      })
  )
})

// 激活事件 - 清理旧缓存
self.addEventListener('activate', (event) => {
  console.log('[Service Worker] 激活中...')
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames
            .filter((cacheName) => {
              return cacheName !== STATIC_CACHE_NAME && 
                     cacheName !== DYNAMIC_CACHE_NAME
            })
            .map((cacheName) => {
              console.log('[Service Worker] 删除旧缓存:', cacheName)
              return caches.delete(cacheName)
            })
        )
      })
      .then(() => {
        console.log('[Service Worker] 激活完成')
        return self.clients.claim()
      })
  )
})

// 请求拦截 - 缓存优先策略
self.addEventListener('fetch', (event) => {
  const { request } = event
  const url = new URL(request.url)

  // 只处理GET请求
  if (request.method !== 'GET') {
    return
  }

  // API请求使用网络优先策略
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(networkFirst(request))
    return
  }

  // 静态资源使用缓存优先策略
  event.respondWith(cacheFirst(request))
})

// 缓存优先策略
async function cacheFirst(request) {
  try {
    const cachedResponse = await caches.match(request)
    if (cachedResponse) {
      return cachedResponse
    }

    const networkResponse = await fetch(request)
    
    // 只缓存成功的响应
    if (networkResponse.ok) {
      const cache = await caches.open(DYNAMIC_CACHE_NAME)
      cache.put(request, networkResponse.clone())
    }
    
    return networkResponse
  } catch (error) {
    console.error('[Service Worker] 缓存优先策略失败:', error)
    
    // 如果网络请求失败，尝试返回离线页面
    if (request.destination === 'document') {
      const cachedResponse = await caches.match('/index.html')
      if (cachedResponse) {
        return cachedResponse
      }
    }
    
    return new Response('离线状态', {
      status: 503,
      statusText: 'Service Unavailable'
    })
  }
}

// 网络优先策略
async function networkFirst(request) {
  try {
    const networkResponse = await fetch(request)
    
    // 缓存成功的API响应
    if (networkResponse.ok) {
      const cache = await caches.open(DYNAMIC_CACHE_NAME)
      cache.put(request, networkResponse.clone())
    }
    
    return networkResponse
  } catch (error) {
    console.log('[Service Worker] 网络请求失败，尝试从缓存获取:', request.url)
    
    const cachedResponse = await caches.match(request)
    if (cachedResponse) {
      return cachedResponse
    }
    
    return new Response(JSON.stringify({
      code: 503,
      message: '网络连接失败，请检查您的网络设置'
    }), {
      status: 503,
      statusText: 'Service Unavailable',
      headers: {
        'Content-Type': 'application/json'
      }
    })
  }
}

// 后台同步
self.addEventListener('sync', (event) => {
  console.log('[Service Worker] 后台同步:', event.tag)
  
  if (event.tag === 'sync-orders') {
    event.waitUntil(syncOrders())
  }
})

// 同步订单数据
async function syncOrders() {
  try {
    // 这里可以实现订单数据的后台同步逻辑
    console.log('[Service Worker] 同步订单数据')
  } catch (error) {
    console.error('[Service Worker] 同步订单失败:', error)
  }
}

// 推送通知
self.addEventListener('push', (event) => {
  console.log('[Service Worker] 收到推送通知')
  
  const options = {
    body: event.data ? event.data.text() : '您有新的消息',
    icon: '/icons/icon-192x192.png',
    badge: '/icons/icon-72x72.png',
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1
    },
    actions: [
      {
        action: 'explore',
        title: '查看详情',
        icon: '/icons/checkmark.png'
      },
      {
        action: 'close',
        title: '关闭',
        icon: '/icons/xmark.png'
      }
    ]
  }
  
  event.waitUntil(
    self.registration.showNotification('智慧停车', options)
  )
})

// 通知点击事件
self.addEventListener('notificationclick', (event) => {
  console.log('[Service Worker] 通知被点击:', event.action)
  
  event.notification.close()
  
  if (event.action === 'explore') {
    event.waitUntil(
      clients.openWindow('/')
    )
  }
})

// 消息事件
self.addEventListener('message', (event) => {
  console.log('[Service Worker] 收到消息:', event.data)
  
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting()
  }
})
