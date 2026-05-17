<template>
  <div class="admin-container">
    <div class="admin-layout">
      <!-- Sidebar -->
      <aside class="admin-sidebar">
        <div class="sidebar-header">
          <div class="sidebar-logo">
            <div class="logo-icon">
              <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="6" width="34" height="28" rx="3" stroke="currentColor" stroke-width="2"/>
                <line x1="12" y1="12" x2="12" y2="28" stroke="currentColor" stroke-width="2"/>
                <line x1="20" y1="12" x2="20" y2="28" stroke="currentColor" stroke-width="2"/>
                <line x1="28" y1="12" x2="28" y2="28" stroke="currentColor" stroke-width="2"/>
                <circle cx="12" cy="9" r="1.5" fill="currentColor"/>
                <circle cx="20" cy="9" r="1.5" fill="currentColor"/>
                <circle cx="28" cy="9" r="1.5" fill="currentColor"/>
              </svg>
            </div>
            <div class="logo-text-group">
              <span class="logo-text">智慧停车</span>
              <span class="sidebar-badge">管理后台</span>
            </div>
          </div>
        </div>
        
        <nav class="sidebar-nav">
          <router-link 
            to="/admin" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="7"/>
                <rect x="14" y="3" width="7" height="7"/>
                <rect x="14" y="14" width="7" height="7"/>
                <rect x="3" y="14" width="7" height="7"/>
              </svg>
            </div>
            <span>管理概览</span>
          </router-link>
          
          <router-link 
            to="/admin/monitor" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/monitor' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                <line x1="8" y1="21" x2="16" y2="21"/>
                <line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
            </div>
            <span>数据监控</span>
          </router-link>
          
          <router-link 
            to="/admin/users" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/users' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <span>用户管理</span>
          </router-link>
          
          <router-link 
            to="/admin/parking-lots" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/parking-lots' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
            </div>
            <span>停车场管理</span>
          </router-link>
          
          <router-link 
            to="/admin/parking-spaces" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/parking-spaces' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <line x1="3" y1="9" x2="21" y2="9"/>
                <line x1="9" y1="21" x2="9" y2="9"/>
              </svg>
            </div>
            <span>车位管理</span>
          </router-link>
          
          <router-link 
            to="/admin/orders" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/orders' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
            </div>
            <span>订单管理</span>
          </router-link>
          
          <router-link 
            to="/admin/reservations" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/reservations' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
            </div>
            <span>预约管理</span>
          </router-link>

          <router-link 
            to="/admin/payments" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/payments' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                <line x1="1" y1="10" x2="23" y2="10"></line>
              </svg>
            </div>
            <span>支付管理</span>
          </router-link>

          <router-link 
            to="/admin/sensors" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/sensors' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            <span>传感器管理</span>
          </router-link>

          <router-link 
            to="/admin/vehicle-records" 
            class="nav-item" 
            :class="{ active: activeMenu === '/admin/vehicle-records' }"
          >
            <div class="nav-icon-wrapper">
              <svg class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="7" width="18" height="13" rx="2"/>
                <path d="M7 13h3v4H7z"/>
                <circle cx="17" cy="16" r="1.5"/>
                <path d="M5 7V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v2"/>
              </svg>
            </div>
            <span>车辆进出</span>
          </router-link>
        </nav>
        
        <div class="sidebar-footer">
          <router-link to="/" class="back-link">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"/>
              <polyline points="12 19 5 12 12 5"/>
            </svg>
            返回首页
          </router-link>
        </div>
      </aside>
      
      <!-- Main Content -->
      <div class="admin-main">
        <!-- Header -->
        <header class="admin-header">
          <div class="header-left">
            <div class="breadcrumb">
              <span class="breadcrumb-item">管理后台</span>
              <span class="breadcrumb-separator">/</span>
              <span class="breadcrumb-item active">{{ pageTitle }}</span>
            </div>
            <h2 class="page-title">{{ pageTitle }}</h2>
          </div>
          <div class="header-right">
            <div class="header-actions">
              <button class="header-btn" title="通知">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
              </button>
              <button class="header-btn" title="设置">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="3"/>
                  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0-2.83l-.06-.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1 1.51z"/>
                </svg>
              </button>
            </div>
            <div class="user-info">
              <div class="user-avatar">{{ avatarChar }}</div>
              <div class="user-details">
                <span class="user-name">{{ displayName }}</span>
                <span class="user-role">{{ displayRole }}</span>
              </div>
            </div>
            <button class="logout-btn" @click="handleLogout">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              退出登录
            </button>
          </div>
        </header>
        
        <!-- Content -->
        <main class="admin-content">
          <router-view :key="routerViewKey"></router-view>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const routerViewKey = ref(Date.now())

watch(() => route.fullPath, (newPath) => {
  routerViewKey.value = Date.now()
})

const avatarChar = computed(() => {
  const name = userStore.user?.name
  if (name && name.length > 0) {
    return name.charAt(0).toUpperCase()
  }
  return 'A'
})

const displayName = computed(() => {
  return userStore.user?.name || '管理员'
})

const displayRole = computed(() => {
  const role = userStore.user?.role
  if (role === 1) return '系统管理员'
  if (role === 0) return '普通用户'
  return '未知角色'
})

const activeMenu = computed(() => {
  return route.path
})

const pageTitle = computed(() => {
  const titles = {
    '/admin': '管理概览',
    '/admin/monitor': '数据监控',
    '/admin/users': '用户管理',
    '/admin/parking-lots': '停车场管理',
    '/admin/parking-spaces': '车位管理',
    '/admin/sensors': '传感器管理',
    '/admin/orders': '订单管理',
    '/admin/reservations': '预约管理',
    '/admin/payments': '支付管理'
  }
  return titles[route.path] || '管理后台'
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', sans-serif;
}

.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 260px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  border-right: 1px solid #e2e8f0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.logo-icon svg {
  width: 28px;
  height: 28px;
}

.logo-text-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text {
  font-size: 1.125rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.sidebar-badge {
  display: inline-block;
  padding: 2px 8px;
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #059669;
  font-size: 0.6875rem;
  font-weight: 600;
  border-radius: 9999px;
  width: fit-content;
  letter-spacing: 0.025em;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 4px 0;
  color: #64748b;
  font-weight: 500;
  font-size: 0.9375rem;
  border-radius: 12px;
  transition: all 0.2s ease;
  position: relative;
  text-decoration: none;
}

.nav-item:hover {
  color: #0f172a;
  background: #f1f5f9;
}

.nav-item.active {
  color: #10b981;
  background: linear-gradient(135deg, #f0fdf4 0%, #d1fae5 100%);
}

.nav-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.nav-item.active .nav-icon-wrapper {
  background: #10b981;
  color: white;
}

.nav-item:not(.active):hover .nav-icon-wrapper {
  background: #e2e8f0;
  color: #0f172a;
}

.nav-icon {
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-weight: 500;
  font-size: 0.875rem;
  padding: 10px 14px;
  border-radius: 10px;
  transition: all 0.2s ease;
  text-decoration: none;
}

.back-link:hover {
  color: #10b981;
  background: #f1f5f9;
}

.admin-main {
  flex: 1;
  min-width: 0;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-header {
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8125rem;
  color: #94a3b8;
}

.breadcrumb-item {
  transition: color 0.2s ease;
}

.breadcrumb-item.active {
  color: #0f172a;
  font-weight: 500;
}

.breadcrumb-separator {
  color: #e2e8f0;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  line-height: 1.2;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 16px;
  border-right: 1px solid #e2e8f0;
}

.header-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: #f1f5f9;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-btn:hover {
  background: #e2e8f0;
  color: #0f172a;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 9999px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9375rem;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #0f172a;
  font-size: 0.9375rem;
  line-height: 1.2;
}

.user-role {
  font-size: 0.75rem;
  color: #64748b;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background: #fee2e2;
  color: #dc2626;
  border-color: #fecaca;
}

.admin-content {
  flex: 1;
  min-width: 0;
  padding: 32px;
  background: #f8fafc;
  overflow-x: hidden;
  overflow-y: auto;
}
</style>