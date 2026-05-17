<template>
  <div class="monitor-dashboard">
    <div class="dashboard-header">
      <div class="header-title-section">
        <h1 class="dashboard-title">智慧停车数据监控中心</h1>
        <p class="dashboard-subtitle">实时监控停车场运营状态与数据指标</p>
      </div>
      <div class="header-stats">
        <div class="header-stat">
          <span class="stat-value">{{ realtimeStats.totalSpaces }}</span>
          <span class="stat-label">总车位</span>
        </div>
        <div class="header-stat">
          <span class="stat-value available">{{ realtimeStats.availableSpaces }}</span>
          <span class="stat-label">空闲车位</span>
        </div>
        <div class="header-stat">
          <span class="stat-value">{{ realtimeStats.utilizationRate }}%</span>
          <span class="stat-label">使用率</span>
        </div>
        <div class="header-stat">
          <span class="stat-value warning">{{ realtimeStats.totalOrders }}</span>
          <span class="stat-label">今日订单</span>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="center-panel">
        <div class="panel large">
          <div class="panel-header">
            <h3 class="panel-title">实时车位状态</h3>
            <span class="panel-refresh" @click="loadDashboardData">刷新</span>
          </div>
          <div class="parking-lots-grid">
            <div v-for="lot in parkingLots" :key="lot.id" class="parking-lot-card">
              <div class="lot-header">
                <span class="lot-name">{{ lot.name }}</span>
                <el-tag :type="lot.status === 1 ? 'success' : 'danger'" size="small" class="lot-status-tag">
                  {{ lot.status === 1 ? '营业中' : '已关闭' }}
                </el-tag>
              </div>
              <div class="lot-stats">
                <div class="lot-stat">
                  <span class="lot-stat-value">{{ lot.totalSpaces }}</span>
                  <span class="lot-stat-label">总车位</span>
                </div>
                <div class="lot-stat">
                  <span class="lot-stat-value available">{{ lot.availableSpaces }}</span>
                  <span class="lot-stat-label">空闲</span>
                </div>
                <div class="lot-stat">
                  <span class="lot-stat-value">{{ lot.totalSpaces - lot.availableSpaces }}</span>
                  <span class="lot-stat-label">占用</span>
                </div>
              </div>
              <div class="lot-usage">
                <div class="usage-label">使用率</div>
                <div class="usage-bar">
                  <div class="usage-fill" :style="{ width: usagePercent(lot) + '%' }" :class="usageClass(lot)"></div>
                </div>
                <div class="usage-value">{{ usagePercent(lot).toFixed(1) }}%</div>
              </div>
            </div>
          </div>
          <div v-if="parkingLots.length === 0 && !loading" class="empty-state">
            <span>暂无停车场数据</span>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="panel">
          <div class="panel-header">
            <h3 class="panel-title">热门停车场排行</h3>
          </div>
          <div class="hot-areas-list">
            <div v-for="(area, index) in hotAreas" :key="area.parkingLotId || area.id" class="hot-area-item">
              <div class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</div>
              <div class="area-info">
                <span class="area-name">{{ area.name }}</span>
                <span class="area-spaces">空闲: {{ area.availableSpaces }}位</span>
              </div>
              <div class="hotness-score">
                <span class="score-value">{{ area.hotnessScore ? area.hotnessScore.toFixed(1) : '-' }}</span>
                <span class="score-label">热度</span>
              </div>
            </div>
          </div>
          <div v-if="hotAreas.length === 0 && !loading" class="empty-state small">
            <span>暂无排行数据</span>
          </div>
        </div>

        <div class="panel">
          <div class="panel-header">
            <h3 class="panel-title">运营告警</h3>
          </div>
          <div class="alert-list">
            <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="alert.level">
              <div class="alert-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                  <line x1="12" y1="9" x2="12" y2="13"/>
                  <line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
              </div>
              <div class="alert-content">
                <span class="alert-title">{{ alert.title }}</span>
                <span class="alert-time">{{ alert.time }}</span>
              </div>
            </div>
            <div v-if="alerts.length === 0" class="no-alert">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <span>系统运行正常，暂无告警</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import apiClient from '../../utils/api'
import wsManager from '../../utils/websocket'

const loading = ref(false)
const realtimeStats = ref({
  totalSpaces: 0,
  availableSpaces: 0,
  utilizationRate: 0,
  totalOrders: 0
})

const parkingLots = ref([])
const hotAreas = ref([])
const alerts = ref([])

let refreshTimer = null

const usagePercent = (lot) => {
  if (!lot.totalSpaces || lot.totalSpaces === 0) return 0
  return (lot.totalSpaces - lot.availableSpaces) / lot.totalSpaces * 100
}

const usageClass = (lot) => {
  const pct = usagePercent(lot)
  if (pct >= 90) return 'critical'
  if (pct >= 70) return 'high'
  return 'normal'
}

const generateAlertsFromData = (lots, ordersData) => {
  const alertList = []
  let alertId = 1

  for (const lot of lots) {
    if (lot.totalSpaces > 0) {
      const usagePct = (lot.totalSpaces - lot.availableSpaces) / lot.totalSpaces * 100
      if (usagePct >= 95) {
        alertList.push({
          id: alertId++,
          title: `${lot.name} 车位已满 (${lot.availableSpaces}/${lot.totalSpaces})`,
          level: 'error',
          time: '实时'
        })
      } else if (usagePct >= 80) {
        alertList.push({
          id: alertId++,
          title: `${lot.name} 车位紧张 (剩余${lot.availableSpaces}位)`,
          level: 'warning',
          time: '实时'
        })
      }
    }
    if (lot.status !== 1) {
      alertList.push({
        id: alertId++,
        title: `${lot.name} 已关闭`,
        level: 'info',
        time: '实时'
      })
    }
  }

  if (ordersData && Array.isArray(ordersData)) {
    const pendingOrders = ordersData.filter(o => o.status === 0)
    if (pendingOrders.length > 5) {
      alertList.push({
        id: alertId++,
        title: `${pendingOrders.length}笔订单待支付`,
        level: 'warning',
        time: '实时'
      })
    }
  }

  return alertList
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    const parkingRes = await apiClient.get('/parking-lots')

    if (parkingRes.data.code === 200) {
      parkingLots.value = Array.isArray(parkingRes.data.data) ? parkingRes.data.data : []

      const total = parkingLots.value.reduce((sum, lot) => sum + (lot.totalSpaces || 0), 0)
      const available = parkingLots.value.reduce((sum, lot) => sum + (lot.availableSpaces || 0), 0)

      realtimeStats.value = {
        totalSpaces: total,
        availableSpaces: available,
        utilizationRate: total > 0 ? ((total - available) / total * 100).toFixed(1) : 0,
        totalOrders: realtimeStats.value.totalOrders
      }

      hotAreas.value = [...parkingLots.value]
        .sort((a, b) => {
          const usageA = a.totalSpaces > 0 ? (a.totalSpaces - a.availableSpaces) / a.totalSpaces : 0
          const usageB = b.totalSpaces > 0 ? (b.totalSpaces - b.availableSpaces) / b.totalSpaces : 0
          return usageB - usageA
        })
        .slice(0, 5)
        .map(lot => ({
          parkingLotId: lot.id,
          name: lot.name,
          availableSpaces: lot.availableSpaces,
          hotnessScore: lot.totalSpaces > 0
            ? ((lot.totalSpaces - lot.availableSpaces) / lot.totalSpaces * 100)
            : 0
        }))

      alerts.value = generateAlertsFromData(parkingLots.value, null)
    }

    try {
      const ordersRes = await apiClient.get('/order-management/list', { params: { page: 1, pageSize: 100 } })
      if (ordersRes.data.code === 200) {
        const rawData = ordersRes.data.data
        let list = []
        if (Array.isArray(rawData)) {
          list = rawData
        } else if (rawData && Array.isArray(rawData.records)) {
          list = rawData.records
        } else if (rawData && Array.isArray(rawData.list)) {
          list = rawData.list
        }
        const today = new Date()
        const todayStr = today.toDateString()
        const todayOrders = list.filter(o => {
          const dateField = o.createdAt || o.createTime || o.orderTime
          return dateField && new Date(dateField).toDateString() === todayStr
        })
        realtimeStats.value.totalOrders = todayOrders.length
        alerts.value = generateAlertsFromData(parkingLots.value, list)
      }
    } catch (e) {
      console.log('[失败][阶段2][加载订单数据] 原因：' + e.message)
    }

    console.log(`[成功][阶段2][监控数据加载] 时间：${Date.now()} | 结果：停车场${parkingLots.value.length}个，告警${alerts.value.length}条`)
  } catch (error) {
    console.log(`[失败][阶段2][监控数据加载] 时间：${Date.now()} | 原因：${error.message}`)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
  refreshTimer = setInterval(loadDashboardData, 30000)

  const token = localStorage.getItem('token')
  if (token) {
    wsManager.connect(token)
  }
  wsManager.on('system', onSystemMessage)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  wsManager.off('system', onSystemMessage)
})

const onSystemMessage = (data) => {
  if (!data || !data.type) return
  const orderTypes = ['ORDER_CREATED', 'ORDER_PAID', 'ORDER_COMPLETED', 'ORDER_CANCELLED']
  if (orderTypes.includes(data.type)) {
    console.log(`[成功][阶段2][监控WS推送] 时间：${Date.now()} | 类型：${data.type} | 结果：刷新监控数据`)
    loadDashboardData()
  }
  if (data.type === 'ORDER_CREATED') {
    ElNotification({
      title: '新订单',
      message: `订单 #${data.orderId || ''} 已创建`,
      type: 'info',
      duration: 3000,
      position: 'top-right'
    })
  }
}
</script>

<style scoped>
.monitor-dashboard {
  min-height: 100%;
  padding: 24px;
  background: #f8fafc;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 24px 28px;
  background: white;
  border-radius: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
}

.header-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dashboard-title {
  font-size: 1.625rem;
  font-weight: 700;
  margin: 0;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.dashboard-subtitle {
  font-size: 0.9375rem;
  color: #64748b;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 48px;
}

.header-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-stat .stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1;
}

.header-stat .stat-value.available {
  color: #10b981;
}

.header-stat .stat-value.warning {
  color: #f59e0b;
}

.header-stat .stat-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
  margin-top: 6px;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
}

.panel {
  background: white;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
}

.panel.large {
  margin-bottom: 0;
}

.panel-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 1.0625rem;
  font-weight: 600;
  margin: 0;
  color: #0f172a;
}

.panel-refresh {
  font-size: 0.8125rem;
  color: #10b981;
  cursor: pointer;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.panel-refresh:hover {
  background: #f0fdf4;
}

.parking-lots-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.parking-lot-card {
  background: #f8fafc;
  border-radius: 18px;
  padding: 20px;
  border: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.parking-lot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px -8px rgba(0, 0, 0, 0.12);
  border-color: #e2e8f0;
}

.lot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.lot-name {
  font-size: 1.0625rem;
  font-weight: 600;
  color: #0f172a;
}

.lot-status-tag {
  font-weight: 500;
}

.lot-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 18px;
  padding: 14px 0;
  background: white;
  border-radius: 12px;
}

.lot-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.lot-stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1;
}

.lot-stat-value.available {
  color: #10b981;
}

.lot-stat-label {
  font-size: 0.8125rem;
  color: #64748b;
  font-weight: 500;
}

.lot-usage {
  display: flex;
  align-items: center;
  gap: 14px;
}

.usage-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
  width: 50px;
}

.usage-bar {
  flex: 1;
  height: 10px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.usage-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.6s ease;
}

.usage-fill.normal {
  background: #10b981;
}

.usage-fill.high {
  background: #f59e0b;
}

.usage-fill.critical {
  background: #ef4444;
}

.usage-value {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #0f172a;
  width: 50px;
  text-align: right;
}

.hot-areas-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-area-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 14px;
  transition: all 0.3s ease;
}

.hot-area-item:hover {
  background: #f1f5f9;
  transform: translateX(4px);
}

.rank {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9375rem;
  font-weight: 700;
  color: #64748b;
  flex-shrink: 0;
}

.rank.top {
  background: #f59e0b;
  color: white;
  box-shadow: 0 4px 12px -4px rgba(245, 158, 11, 0.4);
}

.area-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.area-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #0f172a;
}

.area-spaces {
  font-size: 0.8125rem;
  color: #64748b;
}

.hotness-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.score-value {
  font-size: 1.125rem;
  font-weight: 700;
  color: #f59e0b;
  line-height: 1;
}

.score-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 14px;
  border-left: 3px solid;
  transition: all 0.3s ease;
}

.alert-item:hover {
  background: #f1f5f9;
  transform: translateX(4px);
}

.alert-item.warning {
  border-color: #f59e0b;
  background: rgba(245, 158, 11, 0.06);
}

.alert-item.error {
  border-color: #ef4444;
  background: rgba(239, 68, 68, 0.06);
}

.alert-item.info {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.06);
}

.alert-icon {
  color: #64748b;
  flex-shrink: 0;
}

.alert-item.warning .alert-icon {
  color: #f59e0b;
}

.alert-item.error .alert-icon {
  color: #ef4444;
}

.alert-item.info .alert-icon {
  color: #3b82f6;
}

.alert-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.alert-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #0f172a;
}

.alert-time {
  font-size: 0.8125rem;
  color: #64748b;
}

.no-alert {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #64748b;
  gap: 12px;
}

.no-alert svg {
  color: #10b981;
}

.no-alert span {
  font-size: 0.9375rem;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #94a3b8;
  font-size: 0.9375rem;
}

.empty-state.small {
  padding: 30px 20px;
}

@media (max-width: 1200px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }

  .parking-lots-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .header-stats {
    width: 100%;
    justify-content: space-around;
  }
}
</style>
