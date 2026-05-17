<template>
  <div class="parking-lot-detail-page">
    <button class="back-button" @click="goBack">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="19" y1="12" x2="5" y2="12"/>
        <polyline points="12 19 5 12 12 5"/>
      </svg>
      返回
    </button>

    <div class="detail-banner" v-if="parkingLot">
      <img :src="detailBannerImage" alt="" class="detail-banner-img" />
      <div class="detail-banner-overlay">
        <div class="detail-banner-info">
          <span class="detail-banner-status" :class="parkingLot.availableSpaces > 0 ? 'available' : 'full'">
            {{ parkingLot.availableSpaces > 0 ? '有车位' : '已满' }}
          </span>
          <span class="detail-banner-rate">¥{{ parkingLot.hourlyRate }}/小时</span>
        </div>
      </div>
    </div>

    <div class="page-header">
      <h1 class="page-title">{{ parkingLot?.name || '停车场详情' }}</h1>
      <p class="page-subtitle">查看停车场详细信息和车位状态</p>
    </div>

    <div class="detail-content">
      <div class="info-card">
        <div class="card-header">
          <div class="parking-illustration">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="4" y="8" width="40" height="32" rx="4" fill="#dcfce7" stroke="#10b981" stroke-width="1.5"/>
              <rect x="10" y="14" width="12" height="8" rx="2" fill="#d1fae5" stroke="#10b981" stroke-width="1"/>
              <rect x="26" y="14" width="12" height="8" rx="2" fill="#fee2e2" stroke="#ef4444" stroke-width="1"/>
              <rect x="10" y="26" width="12" height="8" rx="2" fill="#d1fae5" stroke="#10b981" stroke-width="1"/>
              <rect x="26" y="26" width="12" height="8" rx="2" fill="#d1fae5" stroke="#10b981" stroke-width="1"/>
            </svg>
          </div>
          <h2>停车场信息</h2>
          <div class="parking-status" :class="parkingLot?.availableSpaces > 0 ? 'available' : 'full'">
            {{ parkingLot?.availableSpaces > 0 ? '有车位' : '已满' }}
          </div>
        </div>
        <div class="card-body">
          <div class="info-grid">
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
              <div class="info-text">
                <span class="info-label">地址</span>
                <span class="info-value">{{ parkingLot?.address || '-' }}</span>
              </div>
            </div>
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <div class="info-text">
                <span class="info-label">收费标准</span>
                <span class="info-value">¥{{ parkingLot?.hourlyRate || 0 }}/小时</span>
              </div>
            </div>
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <line x1="3" y1="9" x2="21" y2="9"/>
                <line x1="9" y1="21" x2="9" y2="9"/>
              </svg>
              <div class="info-text">
                <span class="info-label">总车位</span>
                <span class="info-value">{{ parkingLot?.totalSpaces || 0 }}</span>
              </div>
            </div>
            <div class="info-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <div class="info-text">
                <span class="info-label">可用车位</span>
                <span class="info-value available">{{ parkingLot?.availableSpaces || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="spaces-card">
        <div class="card-header">
          <h2>车位状态</h2>
          <div class="legend">
            <span class="legend-item"><span class="legend-dot free"></span>空闲</span>
            <span class="legend-item"><span class="legend-dot occupied"></span>占用</span>
            <span class="legend-item"><span class="legend-dot reserved"></span>预约</span>
          </div>
        </div>
        <div class="card-body">
          <div class="parking-spaces">
            <div v-for="space in parkingSpaces" :key="space.id" class="parking-space" :class="getSpaceStatusClass(space.status)">
              <div class="space-number">{{ space.spaceNumber }}</div>
              <div class="space-type">{{ getSpaceTypeText(space.type) }}</div>
              <div class="space-status">{{ getSpaceStatusText(space.status) }}</div>
              <button v-if="space.status === 0" class="btn btn-primary btn-sm" @click="reserveSpace(space.id)">
                预约
              </button>
            </div>
          </div>
          <div v-if="parkingSpaces.length === 0" class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
            <h3>暂无车位信息</h3>
            <p>该停车场暂无车位数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useParkingStore } from '../store/parking'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const parkingStore = useParkingStore()
const userStore = useUserStore()

const parkingLot = ref(null)
const parkingSpaces = ref([])

const detailBannerImage = computed(() => {
  const id = parseInt(route.params.id) || 1
  const images = [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20parking%20garage%20interior%20bright%20spacious%20clean&image_size=landscape_16_9',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=outdoor%20parking%20lot%20aerial%20view%20green%20trees&image_size=landscape_16_9',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20parking%20building%20modern%20glass%20facade%20night&image_size=landscape_16_9'
  ]
  return images[(id - 1) % images.length]
})

onMounted(async () => {
  const id = parseInt(route.params.id)
  await parkingStore.fetchParkingLotById(id)
  parkingLot.value = parkingStore.currentParkingLot
  await parkingStore.getParkingSpacesByLot(id)
  parkingSpaces.value = parkingStore.parkingSpaces
})

const goBack = () => {
  router.back()
}

const getSpaceStatusClass = (status) => {
  switch (status) {
    case 0: return 'space-free'
    case 1: return 'space-occupied'
    case 2: return 'space-reserved'
    default: return ''
  }
}

const getSpaceTypeText = (type) => {
  switch (type) {
    case 0: return '普通车位'
    case 1: return '残疾人车位'
    case 2: return 'VIP车位'
    default: return '未知'
  }
}

const getSpaceStatusText = (status) => {
  switch (status) {
    case 0: return '空闲'
    case 1: return '占用'
    case 2: return '预约'
    default: return '未知'
  }
}

const reserveSpace = async (spaceId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  const success = await parkingStore.reserveSpace(spaceId)
  if (success) {
    ElMessage.success('预约成功')
    await parkingStore.getParkingSpacesByLot(parkingLot.value.id)
    parkingSpaces.value = parkingStore.parkingSpaces
  } else {
    ElMessage.error('预约失败，车位可能已被占用')
  }
}
</script>

<style scoped>
.parking-lot-detail-page {
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

.detail-content {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.info-card,
.spaces-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.card-header {
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
}

.parking-illustration {
  display: flex;
  align-items: center;
  margin-right: 12px;
}

.card-body {
  padding: var(--spacing-xl);
}

.parking-status {
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.parking-status.available {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
}

.parking-status.full {
  background: rgba(239, 68, 68, 0.1);
  color: var(--danger-color);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.info-item svg {
  color: var(--text-muted);
  flex-shrink: 0;
  margin-top: 2px;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-value {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.info-value.available {
  color: var(--success-color);
}

.legend {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
}

.legend-dot.free {
  background: var(--success-color);
}

.legend-dot.occupied {
  background: var(--danger-color);
}

.legend-dot.reserved {
  background: var(--accent-color);
}

.parking-spaces {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--spacing-md);
}

.parking-space {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  text-align: center;
  transition: all var(--transition-base);
}

.parking-space:hover {
  box-shadow: var(--shadow-md);
}

.space-free {
  border-color: var(--success-color);
  background: rgba(16, 185, 129, 0.05);
}

.space-occupied {
  border-color: var(--danger-color);
  background: rgba(239, 68, 68, 0.05);
}

.space-reserved {
  border-color: var(--accent-color);
  background: rgba(245, 158, 11, 0.05);
}

.space-number {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.space-type {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
}

.space-status {
  font-size: 0.8125rem;
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
}

.space-free .space-status {
  color: var(--success-color);
}

.space-occupied .space-status {
  color: var(--danger-color);
}

.space-reserved .space-status {
  color: var(--accent-color);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  font-weight: 500;
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

.btn-sm {
  padding: var(--spacing-xs) var(--spacing-md);
  font-size: 0.8125rem;
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

.detail-banner {
  position: relative;
  width: calc(100% + 2 * var(--spacing-xl));
  margin-left: calc(-1 * var(--spacing-xl));
  height: 200px;
  overflow: hidden;
  margin-bottom: var(--spacing-lg);
}

.detail-banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-banner-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: flex-end;
  padding: 20px;
}

.detail-banner-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-banner-status {
  padding: 6px 16px;
  border-radius: 100px;
  font-size: 0.8125rem;
  font-weight: 700;
  background: rgba(16, 185, 129, 0.9);
  color: #ffffff;
}

.detail-banner-status.full {
  background: rgba(239, 68, 68, 0.9);
}

.detail-banner-rate {
  color: #ffffff;
  font-size: 1.25rem;
  font-weight: 700;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

@media (max-width: 768px) {
  .parking-lot-detail-page {
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

  .info-grid {
    grid-template-columns: 1fr;
  }

  .parking-spaces {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}
</style>
