<template>
  <div class="orders-page">
    <button class="back-button" @click="goBack">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="19" y1="12" x2="5" y2="12"/>
        <polyline points="12 19 5 12 12 5"/>
      </svg>
      返回
    </button>
    
    <div class="orders-banner">
      <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=parking%20receipt%20bill%20illustration%20flat%20design%20green%20minimal&image_size=landscape_16_9" alt="" class="orders-banner-img" />
      <div class="orders-banner-overlay"></div>
    </div>

    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
      <p class="page-subtitle">查看和管理您的停车订单</p>
    </div>
    
    <div v-if="!isLoggedIn" class="login-prompt">
      <svg width="140" height="140" viewBox="0 0 140 140" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="70" cy="70" r="66" fill="#f0fdf4" stroke="#d1fae5" stroke-width="2"/>
        <rect x="40" y="30" width="60" height="80" rx="8" fill="#dcfce7" stroke="#10b981" stroke-width="1.5"/>
        <line x1="52" y1="50" x2="88" y2="50" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
        <line x1="52" y1="62" x2="82" y2="62" stroke="#86efac" stroke-width="2" stroke-linecap="round"/>
        <line x1="52" y1="74" x2="76" y2="74" stroke="#86efac" stroke-width="2" stroke-linecap="round"/>
        <circle cx="70" cy="92" r="5" fill="#10b981"/>
      </svg>
      <h3>请先登录</h3>
      <p>登录后即可查看您的停车订单</p>
      <button class="btn btn-primary" @click="router.push('/login')">去登录</button>
    </div>

    <template v-else>
    <div class="tabs-wrapper">
      <button 
        v-for="tab in tabs" 
        :key="tab.value"
        class="tab-button"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
      </button>
    </div>
    
    <div class="orders-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-id">订单号: {{ order.id }}</div>
          <div class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </div>
        </div>
        <div class="order-body">
          <div class="order-info">
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
              <span>{{ order.parkingLotName }}</span>
            </div>
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              <span>{{ formatDate(order.startTime) }}</span>
            </div>
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>{{ order.endTime ? formatDate(order.endTime) : '进行中' }}</span>
            </div>
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                <line x1="1" y1="10" x2="23" y2="10"/>
              </svg>
              <span>{{ order.plateNumber }}</span>
            </div>
          </div>
          <div class="order-amount">
            <div class="amount-label">订单金额</div>
            <div class="amount-value">¥{{ (order.amount ?? 0).toFixed(2) }}</div>
          </div>
        </div>
        <div class="order-footer">
          <button v-if="order.status === 0" class="btn btn-primary" @click="payOrder(order.id)">
            立即支付
          </button>
          <button v-if="order.status === 0" class="btn btn-secondary" @click="cancelOrder(order.id)">
            取消订单
          </button>
          <button v-if="order.status === 3" class="btn btn-primary" @click="completeOrder(order.id)">
            确认出场
          </button>
          <button class="btn btn-outline" @click="viewOrderDetail(order.id)">
            查看详情
          </button>
        </div>
      </div>
    </div>
    
    <div v-if="filteredOrders.length === 0" class="empty-state">
      <svg width="120" height="120" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-bottom: 16px;">
        <circle cx="60" cy="60" r="56" fill="#f0fdf4" stroke="#d1fae5" stroke-width="2"/>
        <rect x="35" y="30" width="50" height="65" rx="6" fill="#dcfce7" stroke="#10b981" stroke-width="1.5"/>
        <line x1="45" y1="45" x2="75" y2="45" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
        <line x1="45" y1="55" x2="70" y2="55" stroke="#86efac" stroke-width="2" stroke-linecap="round"/>
        <line x1="45" y1="65" x2="65" y2="65" stroke="#86efac" stroke-width="2" stroke-linecap="round"/>
        <circle cx="60" cy="80" r="4" fill="#10b981"/>
      </svg>
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
        <polyline points="14 2 14 8 20 8"/>
        <line x1="16" y1="13" x2="8" y2="13"/>
        <line x1="16" y1="17" x2="8" y2="17"/>
        <polyline points="10 9 9 9 8 9"/>
      </svg>
      <h3>暂无订单</h3>
      <p>您还没有相关的停车订单</p>
    </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '../store/order'
import { useUserStore } from '../store/user'
import wsManager from '../utils/websocket'
import { ElMessage } from 'element-plus'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)

const activeTab = ref('all')

const tabs = computed(() => [
  { label: '全部', value: 'all', count: orderStore.orders.length },
  { label: '待支付', value: 'pending', count: orderStore.pendingOrders.length },
  { label: '已完成', value: 'completed', count: orderStore.completedOrders.length },
  { label: '已取消', value: 'cancelled', count: orderStore.cancelledOrders.length }
])

const filteredOrders = computed(() => {
  switch (activeTab.value) {
    case 'pending':
      return orderStore.pendingOrders
    case 'completed':
      return orderStore.completedOrders
    case 'cancelled':
      return orderStore.cancelledOrders
    default:
      return orderStore.orders
  }
})

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await orderStore.getOrders()
    _connectWS()
  }
})

onUnmounted(() => {
  _disconnectWS()
})

function _connectWS() {
  const token = localStorage.getItem('token') || userStore.token
  if (token && !wsManager.connected) {
    wsManager.connect(token)
  }
  wsManager.on('system', handleSystemMessage)
}

function _disconnectWS() {
  wsManager.off('system', handleSystemMessage)
}

function handleSystemMessage(data) {
  if (data && data.type && data.type.includes('ORDER')) {
    console.log('[成功][阶段2][WebSocket订单更新] 时间：' + Date.now() + ' | 结果：自动刷新订单列表')
    orderStore.getOrders()
  }
}

const goBack = () => {
  router.back()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'pending'
    case 1: return 'completed'
    case 2: return 'cancelled'
    case 3: return 'active'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待支付'
    case 1: return '已完成'
    case 2: return '已取消'
    case 3: return '停车中'
    default: return '未知'
  }
}

const payOrder = async (orderId) => {
  const success = await orderStore.payOrder(orderId, 1)
  if (success) {
    ElMessage.success('支付成功')
    await orderStore.getOrders()
  } else {
    ElMessage.error('支付失败')
  }
}

const cancelOrder = async (orderId) => {
  const success = await orderStore.cancelOrder(orderId)
  if (success) {
    ElMessage.success('取消成功')
    await orderStore.getOrders()
  } else {
    ElMessage.error('取消失败')
  }
}

const completeOrder = async (orderId) => {
  const success = await orderStore.completeOrder(orderId)
  if (success) {
    ElMessage.success('出场成功，车位已释放')
    await orderStore.getOrders()
  } else {
    ElMessage.error('出场失败')
  }
}

const viewOrderDetail = (orderId) => {
  console.log('查看订单详情:', orderId)
}
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding: var(--spacing-xl);
  padding-top: 80px;
}

.back-button {
  position: fixed;
  top: var(--spacing-xl);
  left: var(--spacing-xl);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-md);
}

.back-button:hover {
  background: var(--bg-tertiary);
  transform: translateX(-4px);
}

.page-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  letter-spacing: -0.02em;
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 1.125rem;
}

.tabs-wrapper {
  max-width: 800px;
  margin: 0 auto var(--spacing-xl);
  display: flex;
  gap: var(--spacing-sm);
  background: var(--bg-primary);
  padding: var(--spacing-sm);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.tab-button {
  flex: 1;
  padding: var(--spacing-sm) var(--spacing-md);
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all var(--transition-base);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
}

.tab-button:hover {
  background: var(--bg-tertiary);
}

.tab-button.active {
  background: var(--bg-gradient);
  color: white;
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
}

.tab-button:not(.active) .tab-count {
  background: var(--bg-tertiary);
}

.orders-list {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.order-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.order-header {
  padding: var(--spacing-md) var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-light);
}

.order-id {
  font-weight: 600;
  color: var(--text-primary);
}

.order-status {
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.order-status.pending {
  background: rgba(245, 158, 11, 0.1);
  color: var(--accent-color);
}

.order-status.completed {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
}

.order-status.cancelled {
  background: rgba(107, 114, 128, 0.1);
  color: var(--text-muted);
}

.order-status.active {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.order-body {
  padding: var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-lg);
}

.order-info {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.info-item svg {
  color: var(--text-muted);
  flex-shrink: 0;
}

.order-amount {
  text-align: right;
}

.amount-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
}

.amount-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
}

.order-footer {
  padding: 0 var(--spacing-lg) var(--spacing-lg);
  display: flex;
  gap: var(--spacing-sm);
  justify-content: flex-end;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-lg);
  font-weight: 500;
  font-size: 0.875rem;
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-primary {
  background: var(--bg-gradient);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

.btn-secondary {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: var(--border-color);
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.btn-outline:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--text-secondary);
}

.empty-state svg {
  margin: 0 auto var(--spacing-lg);
  color: var(--text-muted);
}

.empty-state h3 {
  font-size: 1.25rem;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.empty-state p {
  margin: 0;
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  text-align: center;
}

.login-prompt h3 {
  font-size: 1.25rem;
  color: var(--text-primary);
  margin: 24px 0 8px;
}

.login-prompt p {
  color: var(--text-secondary);
  font-size: 0.9375rem;
  margin-bottom: 32px;
}

.login-prompt .btn {
  padding: 12px 32px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.25s ease;
}

.login-prompt .btn:hover {
  background: #059669;
  transform: translateY(-2px);
}

.orders-banner {
  position: relative;
  width: calc(100% + 2 * var(--spacing-xl));
  margin-left: calc(-1 * var(--spacing-xl));
  height: 160px;
  overflow: hidden;
  margin-bottom: var(--spacing-lg);
}

.orders-banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.orders-banner-overlay {
  position: absolute;
  inset: 0;
  background: rgba(16, 185, 129, 0.3);
}

@media (max-width: 768px) {
  .orders-page {
    padding: var(--spacing-md);
    padding-top: 80px;
  }
  
  .back-button {
    top: var(--spacing-md);
    left: var(--spacing-md);
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .tabs-wrapper {
    flex-wrap: wrap;
  }
  
  .tab-button {
    flex: 1 1 45%;
  }
  
  .order-body {
    flex-direction: column;
  }
  
  .order-info {
    grid-template-columns: 1fr;
  }
  
  .order-amount {
    text-align: left;
    padding-top: var(--spacing-md);
    border-top: 1px solid var(--border-light);
  }
}
</style>
