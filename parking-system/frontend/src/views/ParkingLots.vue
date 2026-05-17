<template>
  <div class="parking-lots-page">
    <div class="page-header">
      <div class="page-header-badge">
        <span class="badge-dot"></span>
        <span>停车场查询</span>
        <span class="badge-pulse"></span>
      </div>
      <h1 class="page-title">停车场列表</h1>
      <p class="page-subtitle">查找并预约附近的停车场</p>
    </div>

    <div class="search-section">
      <div class="search-wrapper">
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          v-model="searchQuery"
          placeholder="搜索停车场名称或地址..."
          class="search-input"
        />
      </div>
    </div>

    <div class="parking-lots-banner">
      <svg class="banner-svg" viewBox="0 0 1200 220" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect width="1200" height="220" rx="24" fill="#f0fdf4"/>
        <rect x="40" y="20" width="1120" height="180" rx="16" fill="#d1fae5" stroke="#a7f3d0" stroke-width="2"/>
        <line x1="40" y1="65" x2="1160" y2="65" stroke="#ffffff" stroke-width="3"/>
        <line x1="40" y1="110" x2="1160" y2="110" stroke="#ffffff" stroke-width="3"/>
        <line x1="40" y1="155" x2="1160" y2="155" stroke="#ffffff" stroke-width="3"/>
        <line x1="200" y1="20" x2="200" y2="200" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
        <line x1="400" y1="20" x2="400" y2="200" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
        <line x1="600" y1="20" x2="600" y2="200" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
        <line x1="800" y1="20" x2="800" y2="200" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
        <line x1="1000" y1="20" x2="1000" y2="200" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
        <rect x="60" y="30" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="220" y="30" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="420" y="30" width="120" height="26" rx="5" fill="#ef4444"/>
        <rect x="620" y="30" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="820" y="30" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="1020" y="30" width="120" height="26" rx="5" fill="#ef4444"/>
        <rect x="60" y="75" width="120" height="26" rx="5" fill="#ef4444"/>
        <rect x="220" y="75" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="420" y="75" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="620" y="75" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="820" y="75" width="120" height="26" rx="5" fill="#ef4444"/>
        <rect x="1020" y="75" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="60" y="120" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="220" y="120" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="420" y="120" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="620" y="120" width="120" height="26" rx="5" fill="#ef4444"/>
        <rect x="820" y="120" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="1020" y="120" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="60" y="165" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="220" y="165" width="120" height="26" rx="5" fill="#ef4444"/>
        <rect x="420" y="165" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="620" y="165" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="820" y="165" width="120" height="26" rx="5" fill="#10b981"/>
        <rect x="1020" y="165" width="120" height="26" rx="5" fill="#ef4444"/>
        <circle cx="120" cy="43" r="6" fill="#ffffff" opacity="0.7"/>
        <text x="120" y="47" text-anchor="middle" font-size="8" fill="#ffffff" font-weight="700">P</text>
        <circle cx="680" cy="133" r="6" fill="#ffffff" opacity="0.7"/>
        <text x="680" y="137" text-anchor="middle" font-size="8" fill="#ffffff" font-weight="700">P</text>
      </svg>
      <div class="parking-lots-banner-overlay">
        <h3>附近停车场</h3>
        <p>{{ locationLabel }}</p>
      </div>
    </div>

    <div class="parking-lots-grid">
      <div v-for="(lot, index) in filteredParkingLots" :key="lot.id" class="parking-card" :style="{ animationDelay: index * 0.1 + 's' }">
        <div class="parking-card-image">
          <div class="parking-card-image-placeholder">
            <svg viewBox="0 0 400 180" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="400" height="180" fill="#f0fdf4"/>
              <rect x="80" y="30" width="240" height="120" rx="8" fill="#d1fae5" stroke="#a7f3d0" stroke-width="2"/>
              <line x1="80" y1="70" x2="320" y2="70" stroke="#ffffff" stroke-width="2"/>
              <line x1="80" y1="110" x2="320" y2="110" stroke="#ffffff" stroke-width="2"/>
              <line x1="160" y1="30" x2="160" y2="150" stroke="#ffffff" stroke-width="1.5" stroke-dasharray="6 3"/>
              <line x1="240" y1="30" x2="240" y2="150" stroke="#ffffff" stroke-width="1.5" stroke-dasharray="6 3"/>
              <rect x="92" y="40" width="56" height="22" rx="3" fill="#10b981"/>
              <rect x="172" y="40" width="56" height="22" rx="3" fill="#10b981"/>
              <rect x="252" y="40" width="56" height="22" rx="3" fill="#ef4444"/>
              <rect x="92" y="80" width="56" height="22" rx="3" fill="#ef4444"/>
              <rect x="172" y="80" width="56" height="22" rx="3" fill="#10b981"/>
              <rect x="252" y="80" width="56" height="22" rx="3" fill="#10b981"/>
              <rect x="92" y="120" width="56" height="22" rx="3" fill="#10b981"/>
              <rect x="172" y="120" width="56" height="22" rx="3" fill="#10b981"/>
              <rect x="252" y="120" width="56" height="22" rx="3" fill="#ef4444"/>
              <text x="200" y="170" text-anchor="middle" font-size="11" fill="#64748b" font-weight="500">暂无图片</text>
            </svg>
          </div>
          <div class="parking-card-overlay">
            <div class="parking-card-actions">
              <button class="parking-card-btn" @click="navigateToParkingLotDetail(lot.id)">快速预约</button>
            </div>
          </div>
        </div>
        <div class="parking-card-content">
          <div class="parking-card-header">
            <span class="parking-badge" :class="lot.availableSpaces > 0 ? 'badge-success' : 'badge-danger'">
              {{ lot.availableSpaces > 0 ? '有车位' : '已满' }}
            </span>
            <span class="parking-rate">¥{{ lot.hourlyRate }}/小时</span>
          </div>
          <h3 class="parking-name">{{ lot.name }}</h3>
          <div class="route-meta">
            <span class="route-pill">{{ lot.routeDistanceText || formatRouteDistance(lot) }}</span>
            <span class="route-pill route-pill-walk">{{ lot.routeDurationText || '步行时间待估算' }}</span>
          </div>
          <p class="parking-location">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            {{ lot.address }}
          </p>
          <div class="parking-stats">
            <div class="stat-item">
              <span class="stat-value">{{ lot.totalSpaces }}</span>
              <span class="stat-label">总车位</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value" :class="lot.availableSpaces > 0 ? 'text-success' : 'text-danger'">
                {{ lot.availableSpaces }}
              </span>
              <span class="stat-label">可用</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-rating">
                <span class="rating-star">★</span>
                <span class="rating-value">4.9</span>
              </div>
              <span class="stat-label">评分</span>
            </div>
          </div>
          <div class="parking-card-footer">
            <button class="btn btn-primary" @click="navigateToParkingLotDetail(lot.id)">
              查看详情
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredParkingLots.length === 0" class="empty-state">
      <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect width="64" height="64" rx="16" fill="#ecfdf5"/>
        <circle cx="28" cy="28" r="14" stroke="#10b981" stroke-width="3" fill="none"/>
        <line x1="38" y1="38" x2="50" y2="50" stroke="#10b981" stroke-width="3" stroke-linecap="round"/>
        <line x1="22" y1="28" x2="34" y2="28" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <h3>暂无停车场</h3>
      <p>没有找到匹配的停车场</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useParkingStore } from '../store/parking'
import wsManager from '../utils/websocket'
import apiClient from '../utils/api'

const router = useRouter()
const parkingStore = useParkingStore()

const searchQuery = ref('')
const locationLabel = ref('正在定位并计算步行路线...')
const currentLocation = ref(null)

const filteredParkingLots = computed(() => {
  if (!searchQuery.value) {
    return parkingStore.parkingLots
  }
  const query = searchQuery.value.toLowerCase()
  return parkingStore.parkingLots.filter(lot => {
    return lot.name.toLowerCase().includes(query) || lot.address.toLowerCase().includes(query)
  })
})

onMounted(async () => {
  await loadParkingLotsWithLocation()
  _connectWS()
})

onUnmounted(() => {
  _disconnectWS()
})

function _connectWS() {
  const token = localStorage.getItem('token')
  if (token && !wsManager.connected) {
    wsManager.connect(token)
  }
  wsManager.on('system', handleSystemMessage)
}

function _disconnectWS() {
  wsManager.off('system', handleSystemMessage)
}

function handleSystemMessage(data) {
  if (data && (data.type === 'ORDER_CREATED' || data.type === 'ORDER_PAID' || data.type === 'ORDER_COMPLETED' || data.type === 'ORDER_CANCELLED')) {
    console.log('[成功][阶段2][WebSocket停车场更新] 时间：' + Date.now() + ' | 结果：自动刷新停车场列表')
    loadParkingLotsWithLocation()
  }
}

const navigateToParkingLotDetail = (id) => {
  router.push(`/parking-lot/${id}`)
}

async function loadParkingLotsWithLocation() {
  const location = await resolveCurrentLocation()
  currentLocation.value = location
  if (location) {
    await updateLocationLabel(location)
    await parkingStore.getParkingLots(location)
    return
  }
  locationLabel.value = '未开启定位，已展示停车场基础信息'
  await parkingStore.getParkingLots()
}

function resolveCurrentLocation() {
  return new Promise((resolve) => {
    if (!navigator.geolocation) {
      resolve(null)
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        })
      },
      () => resolve({
        latitude: 30.5728,
        longitude: 104.0668
      }),
      {
        enableHighAccuracy: true,
        timeout: 8000,
        maximumAge: 60000
      }
    )
  })
}

async function updateLocationLabel(location) {
  try {
    const response = await apiClient.get('/geo/location-summary', { params: location })
    locationLabel.value = response.data?.data?.displayName || '当前位置'
  } catch (error) {
    console.error('获取位置文案失败:', error)
    locationLabel.value = '当前位置'
  }
}

function formatRouteDistance(lot) {
  if (lot?.routeDistanceMeters) {
    if (lot.routeDistanceMeters < 1000) {
      return `${lot.routeDistanceMeters}米`
    }
    return `${(lot.routeDistanceMeters / 1000).toFixed(1)}公里`
  }
  return '未知距离'
}
</script>

<style scoped>
.parking-lots-page {
  min-height: 100vh;
  background: #ffffff;
  padding: 48px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', sans-serif;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.page-header-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px;
  background: #d1fae5;
  border-radius: 100px;
  margin-bottom: 20px;
  font-size: 0.875rem;
  color: #10b981;
  font-weight: 600;
  position: relative;
}

.badge-dot {
  width: 10px;
  height: 10px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

.badge-pulse {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  width: 10px;
  height: 10px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.3);
  }
}

@keyframes pulse-ring {
  0% {
    transform: translateY(-50%) scale(0.5);
    opacity: 1;
  }
  100% {
    transform: translateY(-50%) scale(3);
    opacity: 0;
  }
}

.page-title {
  font-size: 3rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 12px;
  letter-spacing: -0.04em;
}

.page-subtitle {
  color: #64748b;
  font-size: 1.125rem;
}

.search-section {
  max-width: 600px;
  margin: 0 auto 48px;
}

.search-wrapper {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.search-input {
  width: 100%;
  padding: 16px 18px 16px 50px;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  font-size: 1rem;
  color: #0f172a;
  transition: all 0.3s ease;
  font-family: inherit;
}

.search-input::placeholder {
  color: #94a3b8;
}

.search-input:focus {
  outline: none;
  border-color: #10b981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
}

.parking-lots-banner {
  position: relative;
  max-width: 1200px;
  margin: 0 auto 48px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(16, 185, 129, 0.12);
}

.banner-svg {
  width: 100%;
  display: block;
}

.parking-lots-banner-overlay {
  position: absolute;
  inset: 0;
  background: rgba(16, 185, 129, 0.35);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 40px;
}

.parking-lots-banner-overlay h3 {
  color: #ffffff;
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 6px;
}

.parking-lots-banner-overlay p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1rem;
  margin: 0;
}

.parking-lots-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 36px;
}

.parking-card {
  background: #ffffff;
  border-radius: 24px;
  overflow: hidden;
  border: 2px solid #e2e8f0;
  box-shadow: 0 4px 16px -4px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  opacity: 0;
  animation: fadeInUp 0.6s ease forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.parking-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 24px 48px -12px rgba(16, 185, 129, 0.2);
  border-color: #10b981;
}

.parking-card-image {
  width: 100%;
  position: relative;
  overflow: hidden;
}

.parking-card-image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0fdf4;
}

.parking-card-image-placeholder svg {
  width: 100%;
  height: 100%;
  display: block;
}

.parking-card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.7);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 24px;
}

.parking-card:hover .parking-card-overlay {
  opacity: 1;
}

.parking-card-actions {
  width: 100%;
}

.parking-card-btn {
  width: 100%;
  padding: 14px 24px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 14px;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: inherit;
}

.parking-card-btn:hover {
  background: #059669;
  transform: scale(1.02);
  box-shadow: 0 8px 20px -6px rgba(16, 185, 129, 0.5);
}

.parking-card-content {
  padding: 28px;
}

.parking-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.parking-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 16px;
  border-radius: 100px;
  font-size: 0.8125rem;
  font-weight: 700;
  letter-spacing: 0.025em;
}

.badge-success {
  background: #d1fae5;
  color: #10b981;
  box-shadow: 0 2px 8px -4px rgba(16, 185, 129, 0.3);
}

.badge-danger {
  background: #fee2e2;
  color: #ef4444;
  box-shadow: 0 2px 8px -4px rgba(239, 68, 68, 0.3);
}

.parking-rate {
  font-weight: 700;
  color: #10b981;
  font-size: 1.25rem;
}

.parking-name {
  font-size: 1.375rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
}

.parking-location {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 0.875rem;
  margin-bottom: 20px;
}

.route-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.route-pill {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #047857;
  font-size: 0.8125rem;
  font-weight: 700;
}

.route-pill-walk {
  background: #eff6ff;
  color: #1d4ed8;
}

.parking-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-top: 1px solid #f1f5f9;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #0f172a;
}

.stat-value.text-success {
  color: #10b981;
}

.stat-value.text-danger {
  color: #ef4444;
}

.stat-label {
  font-size: 0.8125rem;
  color: #64748b;
  font-weight: 500;
}

.stat-divider {
  width: 1.5px;
  height: 40px;
  background: #e2e8f0;
}

.stat-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rating-star {
  color: #f59e0b;
  font-size: 1.125rem;
}

.rating-value {
  font-weight: 700;
  color: #0f172a;
  font-size: 1.5rem;
}

.parking-card-footer {
  display: flex;
  justify-content: flex-start;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 0.9375rem;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: inherit;
}

.btn-primary {
  background: #10b981;
  color: white;
  box-shadow: 0 4px 20px -6px rgba(16, 185, 129, 0.5);
  position: relative;
  overflow: hidden;
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  transition: left 0.6s ease;
}

.btn-primary:hover::before {
  left: 100%;
}

.btn-primary:hover {
  background: #059669;
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 12px 32px -8px rgba(16, 185, 129, 0.5);
}

.btn-primary:active {
  transform: translateY(-1px) scale(0.98);
}

.empty-state {
  text-align: center;
  padding: 80px 48px;
  color: #64748b;
}

.empty-state svg {
  margin: 0 auto 24px;
}

.empty-state h3 {
  font-size: 1.375rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
}

.empty-state p {
  margin: 0;
  font-size: 0.9375rem;
}

@media (max-width: 1024px) {
  .parking-lots-page {
    padding: 32px;
  }

  .page-title {
    font-size: 2.5rem;
  }

  .parking-lots-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .parking-lots-page {
    padding: 20px;
  }

  .page-title {
    font-size: 2rem;
  }

  .parking-lots-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .parking-lots-banner-overlay h3 {
    font-size: 1.25rem;
  }

  .parking-lots-banner-overlay p {
    font-size: 0.875rem;
  }
}
</style>
