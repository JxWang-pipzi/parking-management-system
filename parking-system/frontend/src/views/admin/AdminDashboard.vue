<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <div class="header-welcome">
        <div class="welcome-icon">👋</div>
        <div class="welcome-text">
          <h1 class="dashboard-title">管理概览</h1>
          <p class="dashboard-subtitle">欢迎回来，{{ userStore.user?.name || '管理员' }}！以下是系统的运行状态</p>
        </div>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-card-purple">
        <div class="stat-card-header">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="stat-trend positive" v-if="stats.userGrowth > 0">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/>
            </svg>
            <span>+{{ stats.userGrowth }}%</span>
          </div>
        </div>
        <h3 class="stat-value">{{ stats.users }}</h3>
        <p class="stat-label">注册用户</p>
      </div>
      
      <div class="stat-card stat-card-pink">
        <div class="stat-card-header">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <div class="stat-trend positive" v-if="stats.parkingGrowth > 0">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/>
            </svg>
            <span>+{{ stats.parkingGrowth }}%</span>
          </div>
        </div>
        <h3 class="stat-value">{{ stats.parkingLots }}</h3>
        <p class="stat-label">停车场</p>
      </div>
      
      <div class="stat-card stat-card-blue">
        <div class="stat-card-header">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
          </div>
          <div class="stat-trend positive" v-if="stats.spacesGrowth > 0">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/>
            </svg>
            <span>+{{ stats.spacesGrowth }}%</span>
          </div>
        </div>
        <h3 class="stat-value">{{ stats.parkingSpaces }}</h3>
        <p class="stat-label">总车位数</p>
      </div>
      
      <div class="stat-card stat-card-green">
        <div class="stat-card-header">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
          </div>
          <div class="stat-trend positive" v-if="stats.orderGrowth > 0">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/>
            </svg>
            <span>+{{ stats.orderGrowth }}%</span>
          </div>
        </div>
        <h3 class="stat-value">{{ stats.orders }}</h3>
        <p class="stat-label">今日订单</p>
      </div>
    </div>
    
    <!-- 系统状态 -->
    <div class="system-status">
      <h2 class="section-title">系统状态</h2>
      <div class="status-grid">
        <div class="status-item">
          <div class="status-icon" :class="systemStatus.backend ? 'online' : 'offline'">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="status-content">
            <h4 class="status-title">后端服务</h4>
            <p class="status-description">{{ systemStatus.backend ? '响应正常' : '连接异常' }}</p>
          </div>
          <div class="status-badge" :class="systemStatus.backend ? 'success' : 'error'">{{ systemStatus.backend ? '正常' : '异常' }}</div>
        </div>
        <div class="status-item">
          <div class="status-icon" :class="systemStatus.database ? 'online' : 'offline'">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="status-content">
            <h4 class="status-title">数据库</h4>
            <p class="status-description">{{ systemStatus.database ? '连接正常' : '连接失败' }}</p>
          </div>
          <div class="status-badge" :class="systemStatus.database ? 'success' : 'error'">{{ systemStatus.database ? '正常' : '异常' }}</div>
        </div>
        <div class="status-item">
          <div class="status-icon" :class="systemStatus.websocket ? 'online' : 'offline'">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="status-content">
            <h4 class="status-title">WebSocket</h4>
            <p class="status-description">{{ systemStatus.websocket ? '连接正常' : '未连接' }}</p>
          </div>
          <div class="status-badge" :class="systemStatus.websocket ? 'success' : 'error'">{{ systemStatus.websocket ? '正常' : '异常' }}</div>
        </div>
        <div class="status-item">
          <div class="status-icon" :class="systemStatus.vehicleModule ? 'online' : 'offline'">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="status-content">
            <h4 class="status-title">车辆管理</h4>
            <p class="status-description">{{ systemStatus.vehicleModule ? '服务可用' : '服务异常' }}</p>
          </div>
          <div class="status-badge" :class="systemStatus.vehicleModule ? 'success' : 'error'">{{ systemStatus.vehicleModule ? '正常' : '异常' }}</div>
        </div>
      </div>
    </div>
    
    <!-- 最近订单 -->
    <div class="recent-orders">
      <div class="section-header">
        <h2 class="section-title">最近订单</h2>
        <router-link to="/admin/orders" class="view-all-link">
          查看全部
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14"/>
            <path d="M12 5l7 7-7 7"/>
          </svg>
        </router-link>
      </div>
      <div class="orders-table-container">
        <el-table :data="recentOrders" style="width: 100%" class="orders-table">
          <el-table-column prop="id" label="订单号" width="180" />
          <el-table-column prop="parkingLotName" label="停车场" />
          <el-table-column prop="userId" label="用户" width="120" />
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="scope">
              <span class="amount-text">¥{{ scope.row.amount != null ? Number(scope.row.amount).toFixed(2) : '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <span :class="['status-tag', getStatusClass(scope.row.status)]">
                {{ getStatusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '../../store/user'
import { useParkingStore } from '../../store/parking'
import { useOrderStore } from '../../store/order'
import apiClient from '../../utils/api'
import wsManager from '../../utils/websocket'
import { ElNotification } from 'element-plus'

const userStore = useUserStore()
const parkingStore = useParkingStore()
const orderStore = useOrderStore()

const stats = ref({
  users: 0,
  parkingLots: 0,
  parkingSpaces: 0,
  orders: 0,
  userGrowth: 0,
  parkingGrowth: 0,
  spacesGrowth: 0,
  orderGrowth: 0
})

const recentOrders = ref([])

const systemStatus = ref({
  backend: false,
  database: false,
  websocket: false,
  vehicleModule: false
})

const checkSystemStatus = async () => {
  let backendOk = false
  let databaseOk = false
  let vehicleModuleOk = false

  try {
    const res = await apiClient.get('/parking-lots', { timeout: 5000 })
    backendOk = res.data && res.data.code === 200
    databaseOk = backendOk
  } catch (e) {
    console.log(`[失败][阶段1][后端检测] 时间：${Date.now()} | 原因：${e.message}`)
  }

  try {
    const res = await apiClient.get('/vehicle-records', { timeout: 3000 })
    vehicleModuleOk = res.data && res.data.code === 200
  } catch (e) {
    vehicleModuleOk = false
  }

  systemStatus.value = {
    backend: backendOk,
    database: databaseOk,
    websocket: wsManager.connected,
    vehicleModule: vehicleModuleOk
  }
  console.log(`[成功][阶段1][系统状态检测] 时间：${Date.now()} | 结果：${JSON.stringify(systemStatus.value)}`)
}

const fetchStats = async () => {
  try {
    let ordersCount = 0
    let usersCount = 0
    let spacesCount = 0

    try {
      const ordersResponse = await apiClient.get('/order-management/list', {
        params: { page: 1, size: 100 }
      })
      if (ordersResponse.data.code === 200) {
        const orders = ordersResponse.data.data.records || ordersResponse.data.data || []
        const ordersArray = Array.isArray(orders) ? orders : []
        orderStore.orders = ordersArray
        ordersCount = ordersArray.filter(o => {
          const today = new Date()
          const dateField = o.createdAt || o.createTime || o.orderTime
          if (!dateField) return false
          const orderDate = new Date(dateField)
          return orderDate.toDateString() === today.toDateString()
        }).length
        recentOrders.value = ordersArray
          .sort((a, b) => {
            const dateA = new Date(a.createdAt || a.createTime || 0)
            const dateB = new Date(b.createdAt || b.createTime || 0)
            return dateB - dateA
          })
          .slice(0, 5)
      }
    } catch (e) {
      console.error('[失败][阶段2][获取订单数据] 时间:', Date.now(), '| 原因:', e.message)
    }

    try {
      const usersResponse = await apiClient.get('/users', {
        params: { page: 1, size: 1 }
      })
      if (usersResponse.data.code === 200) {
        usersCount = usersResponse.data.data.total || usersResponse.data.data.length || 0
      }
    } catch (e) {
      console.error('[失败][阶段2][获取用户数据] 时间:', Date.now(), '| 原因:', e.message)
    }

    await parkingStore.getParkingLots()

    try {
      const spacesResponse = await apiClient.get('/parking-spaces', {
        params: { page: 1, size: 1 }
      })
      if (spacesResponse.data.code === 200) {
        spacesCount = spacesResponse.data.data.total || spacesResponse.data.data.length || 0
      }
    } catch (e) {
      console.error('[失败][阶段2][获取车位数据] 时间:', Date.now(), '| 原因:', e.message)
    }

    stats.value = {
      users: usersCount,
      parkingLots: parkingStore.parkingLots.length,
      parkingSpaces: spacesCount,
      orders: ordersCount,
      userGrowth: 0,
      parkingGrowth: 0,
      spacesGrowth: 0,
      orderGrowth: 0
    }

    console.log('[成功][阶段2][获取统计数据] 时间:', Date.now(), '| 参数: Dashboard | 结果:', JSON.stringify(stats.value))
  } catch (error) {
    console.error('[失败][阶段2][获取统计数据] 时间:', Date.now(), '| 原因:', error.message)
    stats.value = {
      users: 0,
      parkingLots: 0,
      parkingSpaces: 0,
      orders: 0,
      userGrowth: 0,
      parkingGrowth: 0,
      spacesGrowth: 0,
      orderGrowth: 0
    }
    recentOrders.value = []
  }
}

const onSystemMessage = (data) => {
  const typeMap = {
    'ORDER_CREATED': '新订单',
    'ORDER_PAID': '订单支付',
    'ORDER_CANCELLED': '订单取消'
  }
  const title = typeMap[data.type] || '系统通知'
  const typeNotif = data.type === 'ORDER_CANCELLED' ? 'warning' : 'success'
  ElNotification({
    title,
    message: `订单 #${data.orderId || ''} - ${title}`,
    type: typeNotif,
    duration: 4000,
    position: 'top-right'
  })
  fetchStats()
}

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'completed', 2: 'cancelled', 3: 'active' }
  return map[status] ?? 'pending'
}

const getStatusLabel = (status) => {
  const map = { 0: '待支付', 1: '已完成', 2: '已取消', 3: '停车中' }
  return map[status] ?? '未知'
}

onMounted(() => {
  fetchStats()
  checkSystemStatus()
  const token = localStorage.getItem('token')
  if (token) {
    wsManager.connect(token)
  }
  wsManager.on('system', onSystemMessage)
})

onUnmounted(() => {
  wsManager.off('system', onSystemMessage)
})
</script>

<style scoped>
.admin-dashboard {
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', sans-serif;
}

.dashboard-header {
  margin-bottom: 32px;
}

.header-welcome {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-icon {
  font-size: 2.5rem;
}

.welcome-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dashboard-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  line-height: 1.2;
}

.dashboard-subtitle {
  color: #64748b;
  font-size: 0.9375rem;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #f1f5f9;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.stat-card-purple::before {
  background: linear-gradient(90deg, #818cf8 0%, #a78bfa 100%);
}

.stat-card-pink::before {
  background: linear-gradient(90deg, #f472b6 0%, #fb7185 100%);
}

.stat-card-blue::before {
  background: linear-gradient(90deg, #38bdf8 0%, #60a5fa 100%);
}

.stat-card-green::before {
  background: linear-gradient(90deg, #34d399 0%, #10b981 100%);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.08);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card-purple,
.stat-card-pink,
.stat-card-blue,
.stat-card-green {
  background: #ffffff;
}

.stat-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #64748b;
}

.stat-card-purple .stat-icon {
  background: #e0e7ff;
  color: #4f46e5;
}

.stat-card-pink .stat-icon {
  background: #fce7f3;
  color: #db2777;
}

.stat-card-blue .stat-icon {
  background: #e0f2fe;
  color: #2563eb;
}

.stat-card-green .stat-icon {
  background: #d1fae5;
  color: #059669;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
}

.stat-trend.positive {
  color: #059669;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px 0;
  line-height: 1.1;
}

.stat-label {
  color: #475569;
  font-size: 0.875rem;
  margin: 0;
  font-weight: 500;
}

.system-status {
  margin-bottom: 32px;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 20px 0;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.status-item {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #f1f5f9;
  transition: all 0.2s ease;
}

.status-item:hover {
  box-shadow: 0 4px 12px -4px rgba(0, 0, 0, 0.05);
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-icon.online {
  background: #d1fae5;
  color: #059669;
}

.status-icon.offline {
  background: #fee2e2;
  color: #dc2626;
}

.status-content {
  flex: 1;
}

.status-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 2px 0;
}

.status-description {
  color: #64748b;
  font-size: 0.8125rem;
  margin: 0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-badge.success {
  background: #d1fae5;
  color: #059669;
}

.status-badge.error {
  background: #fee2e2;
  color: #dc2626;
}

.recent-orders {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #f1f5f9;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.view-all-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #10b981;
  font-weight: 600;
  font-size: 0.875rem;
  text-decoration: none;
  transition: all 0.2s ease;
  padding: 6px 12px;
  border-radius: 10px;
}

.view-all-link:hover {
  color: #059669;
  background: #f0fdf4;
}

.orders-table-container {
  overflow-x: auto;
}

.orders-table {
  border-radius: 12px;
  overflow: hidden;
}

.orders-table :deep(.el-table__header-wrapper th) {
  background: #f8fafc;
  font-weight: 600;
  color: #64748b;
  font-size: 0.8125rem;
  border-bottom: 1px solid #e2e8f0;
}

.orders-table :deep(.el-table__body-wrapper tr) {
  transition: background 0.2s ease;
}

.orders-table :deep(.el-table__row:hover) {
  background: #f8fafc !important;
}

.amount-text {
  font-weight: 600;
  color: #0f172a;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-tag.completed {
  background: #d1fae5;
  color: #059669;
}

.status-tag.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-tag.cancelled {
  background: #f1f5f9;
  color: #64748b;
}

.status-tag.in_progress {
  background: #dbeafe;
  color: #2563eb;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .status-grid {
    grid-template-columns: 1fr;
  }
  
  .dashboard-title {
    font-size: 1.375rem;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .header-welcome {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>