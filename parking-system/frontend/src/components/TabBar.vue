<template>
  <div class="tab-bar">
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="tab-item"
      :class="{ active: isActive(tab) }"
    >
      <div class="tab-icon-wrapper">
        <svg
          v-if="tab.name === 'home'"
          width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
        >
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        <svg
          v-else-if="tab.name === 'parking'"
          width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
        >
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <path d="M9 8h4a3 3 0 0 1 0 6H9V8z"/>
          <line x1="9" y1="14" x2="9" y2="18"/>
        </svg>
        <svg
          v-else-if="tab.name === 'orders'"
          width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
        >
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
        </svg>
        <svg
          v-else-if="tab.name === 'profile'"
          width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
        >
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
      </div>
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  { name: 'home', label: '首页', path: '/' },
  { name: 'parking', label: '停车场', path: '/parking-lots' },
  { name: 'orders', label: '订单', path: '/orders' },
  { name: 'profile', label: '我的', path: '/profile' }
]

const isActive = (tab) => {
  if (tab.path === '/') return route.path === '/'
  if (tab.name === 'parking') return route.path === '/parking-lots' || route.path.startsWith('/parking-lot/')
  return route.path === tab.path
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-around;
  border-top: 1px solid #f1f5f9;
  z-index: 1000;
  padding-bottom: env(safe-area-inset-bottom, 0px);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  flex: 1;
  height: 100%;
  text-decoration: none;
  color: #94a3b8;
  transition: color 0.25s ease;
  -webkit-tap-highlight-color: transparent;
}

.tab-item.active {
  color: #10b981;
}

.tab-icon-wrapper {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: background 0.25s ease;
}

.tab-item.active .tab-icon-wrapper {
  background: rgba(16, 185, 129, 0.1);
}

.tab-label {
  font-size: 11px;
  font-weight: 500;
  line-height: 1;
}
</style>
