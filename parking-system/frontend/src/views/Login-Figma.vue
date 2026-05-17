<template>
  <div class="figma-login-container">
    <div class="figma-login-bg">
      <div class="figma-login-blob blob-1"></div>
      <div class="figma-login-blob blob-2"></div>
      <div class="figma-login-blob blob-3"></div>
      <div class="figma-login-blob blob-4"></div>
    </div>
    
    <div class="figma-login-content">
      <div class="figma-login-left">
        <div class="figma-brand-section">
          <div class="figma-logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="4" y="8" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2.5"/>
              <line x1="14" y1="16" x2="14" y2="32" stroke="currentColor" stroke-width="2.5"/>
              <line x1="24" y1="16" x2="24" y2="32" stroke="currentColor" stroke-width="2.5"/>
              <line x1="34" y1="16" x2="34" y2="32" stroke="currentColor" stroke-width="2.5"/>
              <circle cx="14" cy="12" r="2" fill="currentColor"/>
              <circle cx="24" cy="12" r="2" fill="currentColor"/>
              <circle cx="34" cy="12" r="2" fill="currentColor"/>
            </svg>
          </div>
          <h1 class="figma-brand-title">智慧停车</h1>
          <p class="figma-brand-subtitle">让停车更简单，让出行更便捷</p>
        </div>
        
        <div class="figma-feature-cards">
          <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #EDF4F1 0%, #d1fae5 100%); color: #10b981;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </div>
            <div class="feature-content">
              <h3>实时车位监控</h3>
              <p>实时查看车位状态</p>
            </div>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); color: #3b82f6;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
            </div>
            <div class="feature-content">
              <h3>在线预约支付</h3>
              <p>一键预约轻松支付</p>
            </div>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); color: #f59e0b;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
            </div>
            <div class="feature-content">
              <h3>智能导航</h3>
              <p>精准引导到达停车场</p>
            </div>
          </div>
        </div>
        
        <div class="figma-stats-display">
          <div class="stat-item">
            <span class="stat-number">300+</span>
            <span class="stat-label">总车位</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">230</span>
            <span class="stat-label">可用</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">3</span>
            <span class="stat-label">停车场</span>
          </div>
        </div>
        
        <div class="figma-trust-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <span>安全加密 · 隐私保护</span>
        </div>
      </div>
      
      <div class="figma-login-right">
        <button class="figma-back-button" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          返回首页
        </button>
        
        <div class="figma-login-card">
          <div class="figma-login-header">
            <div class="login-avatar">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <h2>欢迎回来</h2>
            <p>请登录您的账号继续使用</p>
          </div>
          
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="figma-login-form">
            <el-form-item prop="username">
              <div class="figma-input-wrapper">
                <div class="figma-input-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <el-input 
                  v-model="loginForm.username" 
                  placeholder="手机号/用户名"
                  size="large"
                  class="figma-input"
                ></el-input>
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="figma-input-wrapper">
                <div class="figma-input-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                </div>
                <el-input 
                  v-model="loginForm.password" 
                  type="password" 
                  placeholder="密码"
                  size="large"
                  class="figma-input"
                  show-password
                ></el-input>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleLogin" 
                class="figma-login-button"
                size="large"
                :loading="loading"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="figma-login-footer">
            <span class="figma-footer-text">还没有账号？</span>
            <el-button link @click="navigateToRegister" class="figma-register-link">立即注册</el-button>
          </div>
          
          <div class="figma-divider">
            <span>或者</span>
          </div>
          
          <div class="figma-quick-actions">
            <button class="quick-action-btn" @click="handleWechatLogin" :disabled="wechatLoading">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              {{ wechatLoading ? '登录中...' : '微信登录' }}
            </button>
            <button class="quick-action-btn" @click="ElMessage.info('手机号登录功能开发中')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              手机号登录
            </button>
          </div>
        </div>
        
        <div class="figma-right-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const wechatLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入手机号/用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const goBack = () => {
  router.push('/')
}

const handleLogin = async () => {
  if (loginFormRef.value) {
    await loginFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          const success = await userStore.login(loginForm.username, loginForm.password)
          if (success) {
            ElMessage.success('登录成功')
            if (userStore.isAdmin) {
              router.push('/admin')
            } else {
              router.push('/')
            }
          } else {
            ElMessage.error('用户名或密码错误')
          }
        } finally {
          loading.value = false
        }
      }
    })
  }
}

const navigateToRegister = () => {
  router.push('/register')
}

const handleWechatLogin = async () => {
  wechatLoading.value = true
  try {
    const success = await userStore.wechatLogin()
    if (success) {
      ElMessage.success('微信登录成功')
      router.push('/')
    }
  } finally {
    wechatLoading.value = false
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.figma-login-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0fdf4 0%, #f8fafc 50%, #f0f9ff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', sans-serif;
  overflow: hidden;
}

.figma-login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.figma-login-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
  animation: float 30s ease-in-out infinite;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  top: -150px;
  left: -100px;
  animation-delay: 0s;
}

.blob-2 {
  width: 450px;
  height: 450px;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  bottom: -120px;
  right: -80px;
  animation-delay: -10s;
}

.blob-3 {
  width: 380px;
  height: 380px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -20s;
}

.blob-4 {
  width: 320px;
  height: 320px;
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  top: 20%;
  right: 20%;
  animation-delay: -15s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(20px, -20px) scale(1.05);
  }
  50% {
    transform: translate(-10px, 10px) scale(1.1);
  }
  75% {
    transform: translate(15px, 15px) scale(1.05);
  }
}

.figma-login-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  max-width: 1400px;
  width: 100%;
  margin: 40px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(60px);
  -webkit-backdrop-filter: blur(60px);
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 8px 32px -12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.figma-login-left {
  padding: 72px 80px;
  background: linear-gradient(145deg, rgba(220, 252, 231, 0.98) 0%, rgba(187, 247, 208, 0.95) 50%, rgba(220, 252, 231, 0.98) 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-right: 3px solid rgba(16, 185, 129, 0.25);
  position: relative;
  box-shadow: 
    inset -6px 0 30px -10px rgba(16, 185, 129, 0.15),
    0 0 40px -15px rgba(16, 185, 129, 0.1);
}

.figma-login-left::before {
  content: '';
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(16, 185, 129, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(34, 197, 94, 0.06) 0%, transparent 50%);
  pointer-events: none;
}

.figma-login-right {
  padding: 72px 72px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(255, 255, 255, 1) 50%, rgba(248, 250, 252, 0.98) 100%);
  position: relative;
  box-shadow: 
    inset 6px 0 30px -10px rgba(148, 163, 184, 0.12),
    0 0 40px -15px rgba(148, 163, 184, 0.1);
}

.figma-login-right::before {
  content: '';
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 80% 20%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 20% 80%, rgba(147, 197, 253, 0.04) 0%, transparent 50%);
  pointer-events: none;
}

.figma-brand-section {
  margin-bottom: 48px;
}

.figma-logo-icon {
  width: 72px;
  height: 72px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #EDF4F1 0%, #d1fae5 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10b981;
  box-shadow: 0 6px 20px -6px rgba(16, 185, 129, 0.25);
}

.figma-brand-title {
  font-size: 2rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
  letter-spacing: -0.02em;
}

.figma-brand-subtitle {
  font-size: 1rem;
  color: #64748b;
  font-weight: 400;
  line-height: 1.6;
}

.figma-feature-cards {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 40px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 18px;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(241, 245, 249, 0.9);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px -8px rgba(0, 0, 0, 0.12);
  border-color: rgba(226, 232, 240, 1);
  background: rgba(255, 255, 255, 0.95);
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-content h3 {
  font-size: 1.0625rem;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 3px;
}

.feature-content p {
  font-size: 0.875rem;
  color: #64748b;
}

.figma-stats-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 28px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 18px;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(241, 245, 249, 0.9);
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: center;
}

.stat-number {
  font-size: 1.625rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: linear-gradient(180deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.figma-trust-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 20px;
  background: linear-gradient(135deg, rgba(220, 252, 231, 0.6) 0%, rgba(209, 250, 229, 0.4) 100%);
  border-radius: 14px;
  color: #059669;
  font-size: 0.875rem;
  font-weight: 500;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.figma-back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: rgba(248, 250, 252, 0.8);
  border: 1px solid rgba(226, 232, 240, 0.8);
  color: #64748b;
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 36px;
  border-radius: 14px;
  width: fit-content;
  backdrop-filter: blur(10px);
}

.figma-back-button:hover {
  color: #10b981;
  background: rgba(237, 244, 241, 0.9);
  border-color: rgba(16, 185, 129, 0.3);
  transform: translateX(-4px);
}

.figma-login-card {
  width: 100%;
  position: relative;
  z-index: 2;
}

.figma-login-header {
  margin-bottom: 40px;
}

.login-avatar {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #EDF4F1 0%, #d1fae5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10b981;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px -6px rgba(16, 185, 129, 0.25);
}

.figma-login-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.figma-login-header p {
  color: #64748b;
  font-size: 0.9375rem;
}

.figma-login-form {
  margin-bottom: 32px;
  width: 100%;
}

.figma-login-form :deep(.el-form-item) {
  width: 100%;
  margin-bottom: 22px;
}

.figma-login-form :deep(.el-form-item__content) {
  width: 100%;
}

.figma-input-wrapper {
  position: relative;
  width: 100%;
}

.figma-input-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  z-index: 10;
  transition: color 0.3s ease;
}

.figma-input-wrapper:focus-within .figma-input-icon {
  color: #10b981;
}

.figma-input {
  width: 100%;
}

.figma-input :deep(.el-input__wrapper) {
  width: 100%;
  padding: 15px 18px 15px 54px;
  border-radius: 16px;
  border: 1.5px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.8);
  box-shadow: none;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.figma-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(203, 213, 225, 1);
  background: rgba(255, 255, 255, 0.9);
}

.figma-input :deep(.el-input__wrapper.is-focus) {
  border-color: #10b981;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.12);
}

.figma-login-button {
  width: 100%;
  margin-top: 6px;
  padding: 15px 32px;
  font-size: 1rem;
  font-weight: 600;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  box-shadow: 0 6px 20px -6px rgba(16, 185, 129, 0.35);
  transition: all 0.3s ease;
}

.figma-login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px -8px rgba(16, 185, 129, 0.45);
}

.figma-login-button:active {
  transform: translateY(0);
}

.figma-login-footer {
  text-align: center;
  margin-top: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.figma-footer-text {
  color: #64748b;
  font-size: 0.9375rem;
}

.figma-register-link {
  color: #10b981;
  font-weight: 600;
  padding: 6px 10px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.figma-register-link:hover {
  color: #059669;
  background: rgba(237, 244, 241, 0.8);
}

.figma-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 36px 0;
  color: #94a3b8;
  font-size: 0.875rem;
  position: relative;
}

.figma-divider::before,
.figma-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.figma-divider span {
  padding: 0 18px;
}

.figma-quick-actions {
  display: flex;
  gap: 14px;
}

.quick-action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 18px;
  background: rgba(248, 250, 252, 0.8);
  border: 1.5px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  color: #64748b;
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.quick-action-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(203, 213, 225, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px -6px rgba(0, 0, 0, 0.1);
}

.figma-right-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.circle-1 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  top: -60px;
  right: -80px;
}

.circle-2 {
  width: 140px;
  height: 140px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  bottom: -40px;
  right: -40px;
}

@media (max-width: 1024px) {
  .figma-login-content {
    grid-template-columns: 1fr 1fr;
    margin: 32px;
  }
  
  .figma-login-left,
  .figma-login-right {
    padding: 48px 40px;
  }
}

@media (max-width: 900px) {
  .figma-login-content {
    grid-template-columns: 1fr;
    max-width: 480px;
  }
  
  .figma-login-left {
    display: none;
  }
  
  .figma-login-right {
    padding: 48px 32px;
  }
  
  .figma-back-button {
    margin-bottom: 28px;
  }
  
  .figma-login-header h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 640px) {
  .figma-login-content {
    margin: 20px;
    border-radius: 24px;
  }
  
  .figma-login-right {
    padding: 36px 24px;
  }
  
  .figma-login-header h2 {
    font-size: 1.375rem;
  }
  
  .figma-input :deep(.el-input__wrapper) {
    padding: 13px 16px 13px 50px;
  }
  
  .figma-login-button {
    height: 52px;
    padding: 13px 24px;
  }
  
  .figma-quick-actions {
    flex-direction: column;
  }
}
</style>
