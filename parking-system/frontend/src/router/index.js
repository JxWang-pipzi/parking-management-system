import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home-Figma.vue'),
    meta: { showTabBar: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login-Figma.vue'),
    meta: {
      requiresGuest: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      requiresGuest: true
    }
  },
  {
    path: '/parking-lots',
    name: 'ParkingLots',
    component: () => import('../views/ParkingLots.vue'),
    meta: { showTabBar: true }
  },
  {
    path: '/parking-lot/:id',
    name: 'ParkingLotDetail',
    component: () => import('../views/ParkingLotDetail.vue')
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: {
      requiresAuth: true,
      showTabBar: true
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: {
      requiresAuth: true,
      showTabBar: true
    }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue')
      },
      {
        path: 'parking-lots',
        name: 'AdminParkingLots',
        component: () => import('../views/admin/ParkingLots.vue')
      },
      {
        path: 'parking-spaces',
        name: 'AdminParkingSpaces',
        component: () => import('../views/admin/ParkingSpaces.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/Orders.vue')
      },
      {
        path: 'reservations',
        name: 'AdminReservations',
        component: () => import('../views/admin/Reservations.vue')
      },
      {
        path: 'payments',
        name: 'AdminPayments',
        component: () => import('../views/admin/Payments.vue')
      },
      {
        path: 'sensors',
        name: 'AdminSensors',
        component: () => import('../views/admin/Sensors.vue')
      },
      {
        path: 'vehicle-records',
        name: 'AdminVehicleRecords',
        component: () => import('../views/admin/VehicleRecords.vue')
      },
      {
        path: 'monitor',
        name: 'AdminMonitor',
        component: () => import('../views/admin/Monitor.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function isTokenExpired(token) {
  try {
    const base64Url = token.split('.')[1]
    if (!base64Url) return true
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(decodeURIComponent(atob(base64).split('').map(c =>
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join('')))
    return payload.exp * 1000 < Date.now()
  } catch (e) {
    return true
  }
}

function clearAuthAndRedirect(to, next) {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  next({
    path: '/login',
    query: { redirect: to.fullPath }
  })
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token || isTokenExpired(token)) {
      clearAuthAndRedirect(to, next)
    } else {
      if (to.matched.some(record => record.meta.requiresAdmin)) {
        try {
          const userStr = localStorage.getItem('user')
          const user = userStr ? JSON.parse(userStr) : null
          
          if (user && user.role === 1) {
            next()
          } else {
            next('/')
          }
        } catch (e) {
          next('/')
        }
      } else {
        next()
      }
    }
  } else if (to.matched.some(record => record.meta.requiresGuest) && token && !isTokenExpired(token)) {
    next('/')
  } else {
    next()
  }
})

export default router