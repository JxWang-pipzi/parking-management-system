<template>
  <div class="figma-home-container">
    <header class="figma-header">
      <div class="figma-container">
        <div class="figma-header-content">
          <div class="figma-logo" @click="navigateTo('/')">
            <div class="logo-icon">
              <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="6" width="34" height="28" rx="4" stroke="#10b981" stroke-width="2.5"/>
                <line x1="12" y1="14" x2="12" y2="26" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
                <line x1="20" y1="14" x2="20" y2="26" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
                <line x1="28" y1="14" x2="28" y2="26" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
                <circle cx="12" cy="10" r="2" fill="#10b981"/>
                <circle cx="20" cy="10" r="2" fill="#10b981"/>
                <circle cx="28" cy="10" r="2" fill="#10b981"/>
              </svg>
            </div>
            <span class="logo-text">智慧停车</span>
          </div>
          
          <nav class="figma-nav">
            <router-link to="/" class="figma-nav-item" :class="{ active: $route.path === '/' }">
              首页
            </router-link>
            <router-link to="/parking-lots" class="figma-nav-item" :class="{ active: $route.path === '/parking-lots' || $route.path.startsWith('/parking-lot/') }">
              停车场
            </router-link>
            <router-link to="/orders" class="figma-nav-item" :class="{ active: $route.path === '/orders' }">
              订单
            </router-link>
            <router-link to="/profile" class="figma-nav-item" :class="{ active: $route.path === '/profile' }">
              个人中心
            </router-link>
            <router-link to="/admin" class="figma-nav-item" :class="{ active: $route.path.startsWith('/admin') }" v-if="isAdmin">
              管理后台
            </router-link>
          </nav>
          
          <div class="figma-header-actions">
            <template v-if="isLoggedIn">
              <div class="figma-user-info">
                <div class="figma-user-avatar">{{ userName.charAt(0).toUpperCase() }}</div>
                <span class="user-name">{{ userName }}</span>
              </div>
              <button class="figma-btn figma-btn-secondary" @click="handleLogout">
                注销
              </button>
            </template>
            <template v-else>
              <button class="figma-btn figma-btn-primary" @click="navigateTo('/login')">
                登录
              </button>
            </template>
          </div>
        </div>
      </div>
    </header>
    
    <main class="figma-main">
      <section class="figma-hero">
        <div class="figma-hero-bg">
          <div class="figma-hero-blob blob-1"></div>
          <div class="figma-hero-blob blob-2"></div>
          <div class="figma-hero-blob blob-3"></div>
          <div class="figma-hero-blob blob-4"></div>
        </div>
        <div class="figma-hero-glow"></div>
        <div class="figma-container">
          <div class="figma-hero-content">
            <div class="figma-hero-text">
              <div class="figma-hero-badge">
                <span class="badge-dot"></span>
                <span>智能停车系统</span>
                <span class="badge-pulse"></span>
              </div>
              <h1 class="figma-hero-title">
                让停车<br/>
                <span class="figma-gradient-text">更简单</span>
              </h1>
              <p class="figma-hero-subtitle">
                实时查看车位状态，在线预约支付，<br/>
                让您的停车体验更轻松、更便捷
              </p>
              <div class="figma-hero-actions">
                <button class="figma-btn figma-btn-primary figma-btn-lg" @click="navigateTo('/parking-lots')">
                  <span>查找停车场</span>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="5" y1="12" x2="19" y2="12"/>
                    <polyline points="12 5 19 12 12 19"/>
                  </svg>
                </button>
                <button class="figma-btn figma-btn-outline figma-btn-lg" @click="navigateTo('/register')">
                  立即注册
                </button>
              </div>
              <div class="figma-hero-stats">
                <div class="hero-stat">
                  <span class="hero-stat-number">300+</span>
                  <span class="hero-stat-label">总车位</span>
                </div>
                <div class="hero-stat-divider"></div>
                <div class="hero-stat">
                  <span class="hero-stat-number">230</span>
                  <span class="hero-stat-label">可用</span>
                </div>
                <div class="hero-stat-divider"></div>
                <div class="hero-stat">
                  <span class="hero-stat-number">3</span>
                  <span class="hero-stat-label">停车场</span>
                </div>
              </div>
            </div>
            <div class="figma-hero-image">
              <div class="figma-hero-image-wrapper">
                <svg class="hero-banner-img" viewBox="0 0 640 360" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect width="640" height="360" rx="16" fill="#f0fdf4"/>
                  <rect x="40" y="30" width="160" height="100" rx="8" fill="#e2e8f0" stroke="#cbd5e1" stroke-width="2"/>
                  <rect x="50" y="40" width="60" height="40" rx="4" fill="#f8fafc" stroke="#94a3b8" stroke-width="1"/>
                  <rect x="120" y="40" width="60" height="40" rx="4" fill="#f8fafc" stroke="#94a3b8" stroke-width="1"/>
                  <rect x="50" y="90" width="130" height="30" rx="4" fill="#f8fafc" stroke="#94a3b8" stroke-width="1"/>
                  <text x="120" y="110" text-anchor="middle" font-size="11" fill="#64748b" font-weight="600">P</text>
                  <rect x="240" y="30" width="360" height="300" rx="8" fill="#d1fae5" stroke="#a7f3d0" stroke-width="2"/>
                  <line x1="240" y1="80" x2="600" y2="80" stroke="#ffffff" stroke-width="3"/>
                  <line x1="240" y1="130" x2="600" y2="130" stroke="#ffffff" stroke-width="3"/>
                  <line x1="240" y1="180" x2="600" y2="180" stroke="#ffffff" stroke-width="3"/>
                  <line x1="240" y1="230" x2="600" y2="230" stroke="#ffffff" stroke-width="3"/>
                  <line x1="240" y1="280" x2="600" y2="280" stroke="#ffffff" stroke-width="3"/>
                  <line x1="320" y1="30" x2="320" y2="330" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
                  <line x1="420" y1="30" x2="420" y2="330" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
                  <line x1="520" y1="30" x2="520" y2="330" stroke="#ffffff" stroke-width="2" stroke-dasharray="8 4"/>
                  <rect x="252" y="40" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="332" y="40" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="432" y="40" width="56" height="32" rx="4" fill="#ef4444"/>
                  <rect x="532" y="40" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="252" y="90" width="56" height="32" rx="4" fill="#ef4444"/>
                  <rect x="332" y="90" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="432" y="90" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="532" y="90" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="252" y="140" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="332" y="140" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="432" y="140" width="56" height="32" rx="4" fill="#ef4444"/>
                  <rect x="532" y="140" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="252" y="190" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="332" y="190" width="56" height="32" rx="4" fill="#ef4444"/>
                  <rect x="432" y="190" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="532" y="190" width="56" height="32" rx="4" fill="#ef4444"/>
                  <rect x="252" y="240" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="332" y="240" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="432" y="240" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="532" y="240" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="252" y="290" width="56" height="32" rx="4" fill="#ef4444"/>
                  <rect x="332" y="290" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="432" y="290" width="56" height="32" rx="4" fill="#10b981"/>
                  <rect x="532" y="290" width="56" height="32" rx="4" fill="#10b981"/>
                  <circle cx="80" cy="200" r="12" fill="#3b82f6"/>
                  <rect x="74" y="212" width="12" height="20" rx="4" fill="#3b82f6"/>
                  <circle cx="140" cy="220" r="10" fill="#f59e0b"/>
                  <rect x="135" y="230" width="10" height="16" rx="3" fill="#f59e0b"/>
                  <rect x="40" y="280" width="160" height="50" rx="8" fill="#bbf7d0" stroke="#86efac" stroke-width="1.5"/>
                  <text x="120" y="310" text-anchor="middle" font-size="12" fill="#16a34a" font-weight="600">智慧停车场</text>
                </svg>
                <div class="floating-card card-1">
                  <div class="floating-card-icon" style="background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                      <circle cx="12" cy="10" r="3"/>
                    </svg>
                  </div>
                  <span>中央停车场</span>
                  <span class="floating-card-badge">有车位</span>
                </div>
                <div class="floating-card card-2">
                  <div class="floating-card-icon" style="background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                      <line x1="16" y1="2" x2="16" y2="6"/>
                      <line x1="8" y1="2" x2="8" y2="6"/>
                      <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                  </div>
                  <span>¥10/小时</span>
                </div>
                <div class="floating-card card-3">
                  <div class="floating-card-icon" style="background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ec4899" stroke-width="2">
                      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                    </svg>
                  </div>
                  <span>4.9分</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="figma-quick-entry">
        <div class="figma-container">
          <div class="quick-entry-grid">
            <div class="quick-entry-item" @click="navigateTo('/parking-lots')">
              <div class="quick-entry-icon">
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect width="64" height="64" rx="16" fill="#ecfdf5"/>
                  <circle cx="28" cy="28" r="14" stroke="#10b981" stroke-width="3" fill="none"/>
                  <line x1="38" y1="38" x2="50" y2="50" stroke="#10b981" stroke-width="3" stroke-linecap="round"/>
                  <text x="28" y="33" text-anchor="middle" font-size="14" fill="#10b981" font-weight="700">P</text>
                </svg>
              </div>
              <span class="quick-entry-label">找车位</span>
            </div>
            <div class="quick-entry-item" @click="navigateTo('/orders')">
              <div class="quick-entry-icon">
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect width="64" height="64" rx="16" fill="#ecfdf5"/>
                  <rect x="16" y="14" width="32" height="40" rx="4" stroke="#10b981" stroke-width="2.5" fill="none"/>
                  <line x1="22" y1="24" x2="42" y2="24" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
                  <line x1="22" y1="32" x2="42" y2="32" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
                  <line x1="22" y1="40" x2="36" y2="40" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
                  <text x="32" y="50" text-anchor="middle" font-size="8" fill="#10b981" font-weight="700">¥</text>
                </svg>
              </div>
              <span class="quick-entry-label">停车缴费</span>
            </div>
            <div class="quick-entry-item">
              <div class="quick-entry-icon">
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect width="64" height="64" rx="16" fill="#ecfdf5"/>
                  <path d="M32 14C26.5 14 22 18.5 22 24C22 32 32 44 32 44C32 44 42 32 42 24C42 18.5 37.5 14 32 14Z" stroke="#10b981" stroke-width="2.5" fill="none"/>
                  <circle cx="32" cy="24" r="4" fill="#10b981"/>
                  <path d="M20 46L26 40" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
                  <path d="M44 46L38 40" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
                  <line x1="18" y1="50" x2="46" y2="50" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <span class="quick-entry-label">导航</span>
            </div>
            <div class="quick-entry-item">
              <div class="quick-entry-icon">
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect width="64" height="64" rx="16" fill="#ecfdf5"/>
                  <path d="M36 16L28 34H34L30 50L44 28H37L42 16H36Z" fill="#10b981" stroke="#10b981" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
              </div>
              <span class="quick-entry-label">充电桩</span>
            </div>
          </div>
        </div>
      </section>

      <section class="figma-features">
        <div class="figma-container">
          <div class="figma-section-header">
            <div class="figma-section-badge">核心功能</div>
            <h2 class="figma-section-title">为什么选择我们</h2>
            <p class="figma-section-subtitle">我们提供全方位的智能停车解决方案</p>
          </div>
          <div class="figma-feature-grid">
            <div class="figma-feature-card">
              <div class="figma-feature-icon-wrapper">
                <div class="figma-feature-icon">
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                </div>
                <div class="feature-icon-glow"></div>
              </div>
              <h3 class="figma-feature-title">实时车位监控</h3>
              <p class="figma-feature-description">
                实时查看各停车场的车位占用情况，提前规划您的行程，避免找不到车位的尴尬
              </p>
            </div>
            <div class="figma-feature-card">
              <div class="figma-feature-icon-wrapper">
                <div class="figma-feature-icon" style="background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); color: #3b82f6;">
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                </div>
                <div class="feature-icon-glow" style="background: linear-gradient(135deg, rgba(59, 130, 246, 0.3) 0%, rgba(59, 130, 246, 0.1) 100%);"></div>
              </div>
              <h3 class="figma-feature-title">在线预约支付</h3>
              <p class="figma-feature-description">
                一键预约车位，支持多种支付方式，轻松完成停车费用结算，省时省力
              </p>
            </div>
            <div class="figma-feature-card">
              <div class="figma-feature-icon-wrapper">
                <div class="figma-feature-icon" style="background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); color: #f59e0b;">
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                    <path d="M2 17l10 5 10-5"/>
                    <path d="M2 12l10 5 10-5"/>
                  </svg>
                </div>
                <div class="feature-icon-glow" style="background: linear-gradient(135deg, rgba(245, 158, 11, 0.3) 0%, rgba(245, 158, 11, 0.1) 100%);"></div>
              </div>
              <h3 class="figma-feature-title">智能导航</h3>
              <p class="figma-feature-description">
                内置导航功能，精准引导您到达选定的停车场，再也不用担心找不到停车场
              </p>
            </div>
            <div class="figma-feature-card">
              <div class="figma-feature-icon-wrapper">
                <div class="figma-feature-icon" style="background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%); color: #ec4899;">
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
                  </svg>
                </div>
                <div class="feature-icon-glow" style="background: linear-gradient(135deg, rgba(236, 72, 153, 0.3) 0%, rgba(236, 72, 153, 0.1) 100%);"></div>
              </div>
              <h3 class="figma-feature-title">数据分析</h3>
              <p class="figma-feature-description">
                详细的停车数据分析，帮助您了解停车习惯并优化出行，让停车更智能
              </p>
            </div>
          </div>
        </div>
      </section>
      
      <section class="figma-parking">
        <div class="figma-container">
          <div class="figma-section-header">
            <div class="figma-section-header-content">
              <div class="figma-section-badge">精选推荐</div>
              <h2 class="figma-section-title">热门停车场</h2>
              <p class="figma-section-subtitle">精选优质停车场，为您提供便捷服务</p>
            </div>
            <button class="figma-btn figma-btn-outline" @click="navigateTo('/parking-lots')">
              查看全部
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </button>
          </div>
          <div class="figma-parking-grid">
            <div v-for="(lot, index) in parkingLots" :key="lot.id" class="figma-parking-card" :style="{ animationDelay: index * 0.1 + 's' }">
              <div class="figma-parking-image">
                <img v-if="lot.image" :src="lot.image" :alt="lot.name" />
                <div v-else class="figma-parking-image-placeholder">
                  <svg viewBox="0 0 400 200" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect width="400" height="200" fill="#f0fdf4"/>
                    <rect x="80" y="40" width="240" height="120" rx="8" fill="#d1fae5" stroke="#a7f3d0" stroke-width="2"/>
                    <line x1="80" y1="80" x2="320" y2="80" stroke="#ffffff" stroke-width="2"/>
                    <line x1="80" y1="120" x2="320" y2="120" stroke="#ffffff" stroke-width="2"/>
                    <line x1="160" y1="40" x2="160" y2="160" stroke="#ffffff" stroke-width="1.5" stroke-dasharray="6 3"/>
                    <line x1="240" y1="40" x2="240" y2="160" stroke="#ffffff" stroke-width="1.5" stroke-dasharray="6 3"/>
                    <rect x="92" y="50" width="56" height="22" rx="3" fill="#10b981"/>
                    <rect x="172" y="50" width="56" height="22" rx="3" fill="#10b981"/>
                    <rect x="252" y="50" width="56" height="22" rx="3" fill="#ef4444"/>
                    <rect x="92" y="90" width="56" height="22" rx="3" fill="#10b981"/>
                    <rect x="172" y="90" width="56" height="22" rx="3" fill="#ef4444"/>
                    <rect x="252" y="90" width="56" height="22" rx="3" fill="#10b981"/>
                    <rect x="92" y="130" width="56" height="22" rx="3" fill="#ef4444"/>
                    <rect x="172" y="130" width="56" height="22" rx="3" fill="#10b981"/>
                    <rect x="252" y="130" width="56" height="22" rx="3" fill="#10b981"/>
                    <text x="200" y="190" text-anchor="middle" font-size="12" fill="#64748b" font-weight="500">暂无图片</text>
                  </svg>
                </div>
                <div class="parking-card-overlay">
                  <div class="parking-card-actions">
                    <button class="parking-card-btn">快速预约</button>
                  </div>
                </div>
              </div>
              <div class="figma-parking-content">
                <div class="figma-parking-header">
                  <span class="figma-badge" :class="lot.availableSpaces > 0 ? 'figma-badge-success' : 'figma-badge-danger'">
                    {{ lot.availableSpaces > 0 ? '有车位' : '已满' }}
                  </span>
                  <span class="figma-parking-rate">¥{{ lot.hourlyRate }}/小时</span>
                </div>
                <h3 class="figma-parking-name">{{ lot.name }}</h3>
                <p class="figma-parking-address">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                  {{ lot.address }}
                </p>
                <div class="figma-parking-stats">
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
                <div class="figma-parking-footer">
                  <button class="figma-btn figma-btn-secondary" @click="navigateToParkingLotDetail(lot.id)">
                    查看详情
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    
    <footer class="figma-footer">
      <div class="figma-container">
        <div class="figma-footer-content">
          <div class="figma-footer-section">
            <div class="figma-footer-logo">
              <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="6" width="34" height="28" rx="4" stroke="#10b981" stroke-width="2.5"/>
                <line x1="12" y1="14" x2="12" y2="26" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
                <line x1="20" y1="14" x2="20" y2="26" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
                <line x1="28" y1="14" x2="28" y2="26" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
                <circle cx="12" cy="10" r="2" fill="#10b981"/>
                <circle cx="20" cy="10" r="2" fill="#10b981"/>
                <circle cx="28" cy="10" r="2" fill="#10b981"/>
              </svg>
              <span>智慧停车</span>
            </div>
            <p class="figma-footer-description">
              让停车更简单，让出行更便捷<br/>
              我们致力于提供最优质的智能停车服务
            </p>
          </div>
          <div class="figma-footer-section">
            <h4>快速链接</h4>
            <ul class="figma-footer-links">
              <li><a href="#">首页</a></li>
              <li><a href="#">停车场</a></li>
              <li><a href="#">关于我们</a></li>
              <li><a href="#">帮助中心</a></li>
            </ul>
          </div>
          <div class="figma-footer-section">
            <h4>联系我们</h4>
            <ul class="figma-footer-links">
              <li>客服热线：400-888-8888</li>
              <li>邮箱：support@parking.com</li>
              <li>工作时间：9:00 - 21:00</li>
            </ul>
          </div>
        </div>
        <div class="figma-footer-bottom">
          <p>&copy; 2026 智慧停车系统</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useParkingStore } from '../store/parking'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const parkingStore = useParkingStore()
const userStore = useUserStore()

const parkingLots = ref([
  {
    id: 1,
    name: '中央停车场',
    address: '北京市朝阳区建国路88号',
    totalSpaces: 100,
    availableSpaces: 80,
    hourlyRate: 10.00,
    latitude: 39.9042,
    longitude: 116.4074,
    status: 1,
    image: ''
  },
  {
    id: 2,
    name: '东方广场停车场',
    address: '北京市东城区东长安街1号',
    totalSpaces: 80,
    availableSpaces: 60,
    hourlyRate: 15.00,
    latitude: 39.9139,
    longitude: 116.4103,
    status: 1,
    image: ''
  },
  {
    id: 3,
    name: '国贸停车场',
    address: '北京市朝阳区建国门外大街1号',
    totalSpaces: 120,
    availableSpaces: 90,
    hourlyRate: 12.00,
    latitude: 39.9075,
    longitude: 116.4668,
    status: 1,
    image: ''
  }
])

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userName = computed(() => userStore.user?.name || '用户')
const isAdmin = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return user.role === 1
  } catch {
    return false
  }
})

onMounted(async () => {
  try {
    await parkingStore.getParkingLots()
    if (parkingStore.parkingLots.length > 0) {
      parkingLots.value = parkingStore.parkingLots.slice(0, 3)
    }
  } catch (error) {
    console.log('使用模拟停车场数据')
  }
})

const navigateTo = (path) => {
  router.push(path)
}

const navigateToParkingLotDetail = (id) => {
  router.push(`/parking-lot/${id}`)
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已注销登录')
  router.push('/')
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.figma-home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', sans-serif;
  overflow-x: hidden;
}

.figma-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(32px);
  -webkit-backdrop-filter: blur(32px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  padding: 0 48px;
  box-shadow: 0 4px 24px -8px rgba(0, 0, 0, 0.06);
}

.figma-container {
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}

.figma-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
}

.figma-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.figma-logo:hover {
  transform: scale(1.03);
}

.logo-icon {
  width: 42px;
  height: 42px;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.figma-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.figma-nav-item {
  padding: 10px 18px;
  color: #64748b;
  font-weight: 500;
  font-size: 0.9375rem;
  border-radius: 12px;
  transition: all 0.25s ease;
  text-decoration: none;
}

.figma-nav-item:hover {
  color: #10b981;
  background: #f1f5f9;
  transform: translateY(-1px);
}

.figma-nav-item.active {
  color: #10b981;
  font-weight: 600;
  background: linear-gradient(135deg, #EDF4F1 0%, #d1fae5 100%);
  box-shadow: 0 2px 8px -4px rgba(16, 185, 129, 0.2);
}

.figma-header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.figma-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.figma-user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
  box-shadow: 0 4px 12px -4px rgba(16, 185, 129, 0.3);
}

.user-name {
  color: #0f172a;
  font-weight: 500;
  font-size: 0.9375rem;
}

.figma-btn {
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
  text-decoration: none;
  font-family: inherit;
}

.figma-btn-primary {
  background: linear-gradient(135deg, #10b981 0%, #059669 50%, #047857 100%);
  color: white;
  box-shadow: 0 4px 20px -6px rgba(16, 185, 129, 0.5);
  position: relative;
  overflow: hidden;
}

.figma-btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.figma-btn-primary:hover::before {
  left: 100%;
}

.figma-btn-primary:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 12px 32px -8px rgba(16, 185, 129, 0.5);
}

.figma-btn-primary:active {
  transform: translateY(-1px) scale(0.98);
}

.figma-btn-secondary {
  background: #f8fafc;
  color: #0f172a;
  border: 1.5px solid #e2e8f0;
}

.figma-btn-secondary:hover {
  background: #f1f5f9;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px -6px rgba(0, 0, 0, 0.1);
  border-color: #cbd5e1;
}

.figma-btn-outline {
  background: transparent;
  color: #10b981;
  border: 2px solid #10b981;
  padding: 10px 22px;
  font-size: 0.875rem;
  font-weight: 600;
}

.figma-btn-outline:hover {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 10px 24px -6px rgba(16, 185, 129, 0.4);
}

.figma-btn-lg {
  padding: 16px 36px;
  font-size: 1rem;
  height: 56px;
}

.figma-main {
  flex: 1;
}

.figma-hero {
  position: relative;
  padding: 140px 48px 120px;
  overflow: hidden;
}

.figma-hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.figma-hero-glow {
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 400px;
  background: radial-gradient(ellipse at center, rgba(16, 185, 129, 0.15) 0%, transparent 70%);
  z-index: 0;
  pointer-events: none;
}

.figma-hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.5;
  animation: float 25s ease-in-out infinite;
}

.blob-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  bottom: -100px;
  right: -100px;
  animation-delay: -7s;
}

.blob-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  top: 40%;
  left: 40%;
  transform: translate(-50%, -50%);
  animation-delay: -14s;
}

.blob-4 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  top: 20%;
  right: 30%;
  animation-delay: -20s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1) rotate(0deg);
  }
  25% {
    transform: translate(50px, -40px) scale(1.1) rotate(5deg);
  }
  50% {
    transform: translate(-30px, 60px) scale(0.9) rotate(-5deg);
  }
  75% {
    transform: translate(40px, 30px) scale(1.05) rotate(3deg);
  }
}

.figma-hero-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 100px;
  align-items: center;
}

.figma-hero-text {
  max-width: 580px;
}

.figma-hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px;
  background: linear-gradient(135deg, #EDF4F1 0%, #d1fae5 100%);
  border-radius: 100px;
  margin-bottom: 28px;
  font-size: 0.875rem;
  color: #10b981;
  font-weight: 600;
  box-shadow: 0 4px 12px -4px rgba(16, 185, 129, 0.2);
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

.figma-hero-title {
  font-size: 4.5rem;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 24px;
  color: #0f172a;
  letter-spacing: -0.05em;
}

.figma-gradient-text {
  background: linear-gradient(135deg, #10b981 0%, #059669 30%, #047857 60%, #065f46 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.figma-hero-subtitle {
  font-size: 1.25rem;
  color: #64748b;
  margin-bottom: 40px;
  line-height: 1.8;
}

.figma-hero-actions {
  display: flex;
  gap: 18px;
  margin-bottom: 56px;
}

.figma-hero-stats {
  display: flex;
  align-items: center;
  gap: 32px;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-stat-number {
  font-size: 2rem;
  font-weight: 800;
  color: #0f172a;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-stat-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

.hero-stat-divider {
  width: 1.5px;
  height: 48px;
  background: linear-gradient(180deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.figma-hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.figma-hero-image-wrapper {
  position: relative;
  width: 100%;
}

.figma-hero-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 28px;
  padding: 24px;
  box-shadow: 0 30px 60px -15px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.hero-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  margin-bottom: 24px;
}

.hero-card-dots {
  display: flex;
  gap: 8px;
}

.hero-card-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.status-icon {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.red {
  background: #ef4444;
}

.dot.yellow {
  background: #f59e0b;
}

.dot.green {
  background: #10b981;
}

.hero-card-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.parking-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.parking-spot {
  aspect-ratio: 4/3;
  border-radius: 12px;
  border: 2px solid;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.parking-spot:hover {
  transform: scale(1.05);
}

.spot-icon {
  font-size: 1.25rem;
  font-weight: 700;
  opacity: 0;
  transform: scale(0.5);
  transition: all 0.3s ease;
}

.parking-spot:hover .spot-icon {
  opacity: 1;
  transform: scale(1);
}

.parking-spot.available {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-color: #10b981;
  box-shadow: 0 4px 12px -4px rgba(16, 185, 129, 0.3);
}

.parking-spot.available .spot-icon {
  color: #10b981;
}

.parking-spot.occupied {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  border-color: #ef4444;
  box-shadow: 0 4px 12px -4px rgba(239, 68, 68, 0.3);
}

.parking-spot.occupied .spot-icon {
  color: #ef4444;
}

.parking-info {
  display: flex;
  gap: 14px;
}

.info-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 14px;
  font-size: 0.875rem;
  color: #0f172a;
  font-weight: 600;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px -4px rgba(0, 0, 0, 0.1);
}

.floating-card {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 18px;
  box-shadow: 0 16px 40px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.9);
  font-size: 0.875rem;
  font-weight: 600;
  color: #0f172a;
}

.card-1 {
  top: -30px;
  left: -50px;
  animation: floatCard 7s ease-in-out infinite;
}

.card-2 {
  bottom: -30px;
  right: -30px;
  animation: floatCard 7s ease-in-out infinite;
  animation-delay: -2.5s;
}

.card-3 {
  top: 50%;
  right: -60px;
  transform: translateY(-50%);
  animation: floatCard 7s ease-in-out infinite;
  animation-delay: -5s;
}

@keyframes floatCard {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-16px);
  }
}

.floating-card-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px -4px rgba(0, 0, 0, 0.1);
}

.floating-card-badge {
  padding: 5px 12px;
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #10b981;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 700;
}

.figma-features {
  padding: 120px 48px;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.figma-section-header {
  text-align: center;
  margin-bottom: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.figma-section-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  background: linear-gradient(135deg, #EDF4F1 0%, #d1fae5 100%);
  color: #10b981;
  border-radius: 100px;
  font-size: 0.8125rem;
  font-weight: 600;
  margin-bottom: 20px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.figma-section-header-content {
  text-align: center;
  flex: 1;
}

.figma-section-title {
  font-size: 3rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 16px;
  letter-spacing: -0.04em;
}

.figma-section-subtitle {
  color: #64748b;
  font-size: 1.125rem;
  max-width: 600px;
}

.figma-feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28px;
}

.figma-feature-card {
  padding: 36px;
  background: white;
  border-radius: 24px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.figma-feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #10b981 0%, #059669 100%);
  transform: scaleX(0);
  transition: transform 0.4s ease;
}

.figma-feature-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 24px 48px -12px rgba(0, 0, 0, 0.12);
  border-color: #d1fae5;
}

.figma-feature-card:hover::before {
  transform: scaleX(1);
}

.figma-feature-icon-wrapper {
  margin-bottom: 28px;
  position: relative;
}

.figma-feature-icon {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  background: linear-gradient(135deg, #EDF4F1 0%, #d1fae5 100%);
  color: #10b981;
  position: relative;
  z-index: 1;
  box-shadow: 0 8px 24px -8px rgba(16, 185, 129, 0.3);
}

.feature-icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.3) 0%, rgba(16, 185, 129, 0.05) 100%);
  border-radius: 50%;
  z-index: 0;
  filter: blur(20px);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.figma-feature-card:hover .feature-icon-glow {
  opacity: 1;
}

.figma-feature-title {
  font-size: 1.375rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 14px;
}

.figma-feature-description {
  color: #64748b;
  font-size: 0.9375rem;
  line-height: 1.8;
}

.figma-parking {
  padding: 120px 48px;
  background: white;
}

.figma-parking .figma-section-header {
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-end;
  text-align: left;
}

.figma-parking .figma-section-header-content {
  text-align: left;
  align-items: flex-start;
}

.figma-parking-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 36px;
}

.figma-parking-card {
  background: white;
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

.figma-parking-card:hover {
  transform: translateY(-16px) scale(1.02);
  box-shadow: 0 32px 64px -16px rgba(0, 0, 0, 0.15);
  border-color: #10b981;
}

.figma-parking-image {
  width: 100%;
  position: relative;
  overflow: hidden;
}

.figma-parking-image-placeholder {
  width: 100%;
  height: 200px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0fdf4;
}

.figma-parking-image-placeholder svg {
  width: 100%;
  height: 100%;
  display: block;
}

.parking-card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(15, 23, 42, 0.8) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 24px;
}

.figma-parking-card:hover .parking-card-overlay {
  opacity: 1;
}

.parking-card-actions {
  width: 100%;
}

.parking-card-btn {
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 14px;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.parking-card-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 20px -6px rgba(16, 185, 129, 0.5);
}

.figma-parking-content {
  padding: 32px;
}

.figma-parking-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.figma-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 16px;
  border-radius: 100px;
  font-size: 0.8125rem;
  font-weight: 700;
  letter-spacing: 0.025em;
}

.figma-badge-success {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #10b981;
  box-shadow: 0 2px 8px -4px rgba(16, 185, 129, 0.3);
}

.figma-badge-danger {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #ef4444;
  box-shadow: 0 2px 8px -4px rgba(239, 68, 68, 0.3);
}

.figma-parking-rate {
  font-weight: 700;
  color: #10b981;
  font-size: 1.25rem;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.figma-parking-name {
  font-size: 1.375rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
}

.figma-parking-address {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 0.875rem;
  margin-bottom: 24px;
}

.figma-parking-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-top: 1px solid #f1f5f9;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
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

.stat-value {
  font-size: 1.625rem;
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
  background: linear-gradient(180deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.figma-parking-footer {
  display: flex;
  justify-content: flex-start;
}

.figma-footer {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  color: #0f172a;
  margin-top: 100px;
  border-top: 2px solid #e2e8f0;
}

.figma-footer-content {
  padding: 96px 48px;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 72px;
}

.figma-footer-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  font-weight: 700;
  font-size: 1.25rem;
  color: #0f172a;
}

.figma-footer-logo svg {
  width: 44px;
  height: 44px;
}

.figma-footer-description {
  color: #64748b;
  font-size: 0.9375rem;
  line-height: 1.9;
}

.figma-footer-section h4 {
  color: #0f172a;
  font-size: 1.0625rem;
  font-weight: 700;
  margin-bottom: 24px;
}

.figma-footer-links {
  list-style: none;
}

.figma-footer-links li {
  margin-bottom: 14px;
}

.figma-footer-links a,
.figma-footer-links li {
  color: #64748b;
  font-size: 0.9375rem;
  transition: color 0.2s ease;
  text-decoration: none;
}

.figma-footer-links a:hover {
  color: #10b981;
}

.figma-footer-bottom {
  border-top: 1px solid #e2e8f0;
  padding: 24px 48px;
  text-align: center;
  color: #94a3b8;
  font-size: 0.875rem;
}

@media (max-width: 1024px) {
  .figma-header {
    padding: 0 32px;
  }
  
  .figma-hero {
    padding: 100px 32px;
  }
  
  .figma-hero-content {
    grid-template-columns: 1fr;
  }
  
  .figma-hero-image {
    order: -1;
  }
  
  .figma-features,
  .figma-parking {
    padding: 80px 32px;
  }
  
  .figma-feature-grid,
  .figma-parking-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .figma-footer-content {
    grid-template-columns: 1fr 1fr;
  }
  
  .figma-nav {
    display: none;
  }
  
  .figma-hero-title {
    font-size: 3.5rem;
  }
  
  .figma-section-title {
    font-size: 2.5rem;
  }
}

@media (max-width: 768px) {
  .figma-header {
    padding: 0 20px;
  }
  
  .figma-hero {
    padding: 80px 20px;
  }
  
  .figma-hero-title {
    font-size: 2.75rem;
  }
  
  .figma-features,
  .figma-parking {
    padding: 60px 20px;
  }
  
  .figma-section-title {
    font-size: 2rem;
  }
  
  .figma-feature-grid,
  .figma-parking-grid,
  .figma-footer-content {
    grid-template-columns: 1fr;
  }
  
  .figma-footer-content {
    text-align: center;
  }
  
  .figma-hero-actions {
    flex-direction: column;
  }
  
  .figma-hero-actions .figma-btn {
    width: 100%;
  }
  
  .figma-hero-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .floating-card {
    display: none;
  }
}

.figma-quick-entry {
  padding: 0 48px;
  margin-top: -40px;
  position: relative;
  z-index: 10;
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  background: #ffffff;
  border-radius: 24px;
  padding: 32px 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;
}

.quick-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.25s ease;
}

.quick-entry-item:hover {
  transform: translateY(-4px);
}

.quick-entry-icon {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.15);
}

.quick-entry-icon svg {
  width: 100%;
  height: 100%;
  display: block;
}

.quick-entry-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #0f172a;
}

.figma-hero-image-wrapper .hero-banner-img {
  width: 100%;
  border-radius: 28px;
  box-shadow: 0 30px 60px -15px rgba(0, 0, 0, 0.15);
  display: block;
}

.figma-parking-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

@media (max-width: 768px) {
  .figma-quick-entry {
    padding: 0 20px;
    margin-top: -24px;
  }

  .quick-entry-grid {
    gap: 12px;
    padding: 20px 16px;
  }

  .quick-entry-icon {
    width: 48px;
    height: 48px;
    border-radius: 14px;
  }

  .quick-entry-label {
    font-size: 0.75rem;
  }
}
</style>