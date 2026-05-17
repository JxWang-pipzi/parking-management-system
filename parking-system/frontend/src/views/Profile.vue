<template>
  <div class="profile-page">
    <div v-if="!isLoggedIn" class="login-prompt">
      <svg width="160" height="160" viewBox="0 0 160 160" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="80" cy="80" r="76" fill="#f0fdf4" stroke="#d1fae5" stroke-width="2"/>
        <circle cx="80" cy="60" r="24" fill="#dcfce7" stroke="#10b981" stroke-width="2"/>
        <path d="M40 130c0-22 18-40 40-40s40 18 40 40" fill="#dcfce7" stroke="#10b981" stroke-width="2"/>
        <rect x="60" y="100" width="40" height="30" rx="6" fill="#d1fae5" stroke="#10b981" stroke-width="1.5"/>
        <line x1="70" y1="110" x2="90" y2="110" stroke="#10b981" stroke-width="2" stroke-linecap="round"/>
        <line x1="70" y1="118" x2="85" y2="118" stroke="#86efac" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <h2>请先登录</h2>
      <p>登录后即可查看个人信息和订单</p>
      <button class="btn btn-primary" @click="router.push('/login')">去登录</button>
    </div>

    <template v-else>
      <div class="profile-header">
        <div class="profile-header-bg"></div>
        <img class="profile-header-decor" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=city%20skyline%20silhouette%20minimal%20green%20illustration%20clean&image_size=landscape_16_9" alt="" />
        <div class="profile-avatar-section">
          <div v-if="isWechatUser && userStore.user.wechatAvatar" class="profile-avatar wechat-avatar">
            <img :src="userStore.user.wechatAvatar" alt="微信头像" @error="onAvatarError" />
          </div>
          <div v-else class="profile-avatar default-avatar">
            {{ userForm.name ? userForm.name.charAt(0).toUpperCase() : 'U' }}
          </div>
          <div class="profile-user-info">
            <h2 class="profile-name">{{ isWechatUser ? (userStore.user.wechatNickname || userForm.name) : (userForm.name || '用户') }}</h2>
            <p class="profile-username">@{{ userForm.username }}</p>
            <span v-if="isWechatUser" class="wechat-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
              微信用户
            </span>
          </div>
        </div>
      </div>

      <div class="profile-content">
        <div class="profile-card">
          <div class="card-header">
            <div class="card-header-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <h3>个人信息</h3>
          </div>
          <div class="card-body">
            <form class="profile-form">
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">用户名</label>
                  <input v-model="userForm.username" type="text" class="form-input" disabled />
                </div>
                <div class="form-group">
                  <label class="form-label">真实姓名</label>
                  <input v-model="userForm.name" type="text" class="form-input" placeholder="请输入真实姓名" />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">手机号码</label>
                  <input v-model="userForm.phone" type="tel" class="form-input" placeholder="请输入手机号码" />
                </div>
                <div class="form-group">
                  <label class="form-label">邮箱</label>
                  <input v-model="userForm.email" type="email" class="form-input" placeholder="请输入邮箱" />
                </div>
              </div>
              <div class="form-actions">
                <button type="button" class="btn btn-primary" @click="updateProfile">保存修改</button>
              </div>
            </form>
          </div>
        </div>

        <div class="profile-card">
          <div class="card-header">
            <div class="card-header-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2">
                <rect x="3" y="3" width="7" height="7"/>
                <rect x="14" y="3" width="7" height="7"/>
                <rect x="14" y="14" width="7" height="7"/>
                <rect x="3" y="14" width="7" height="7"/>
              </svg>
            </div>
            <h3>常用功能</h3>
          </div>
          <div class="card-body">
            <div class="menu-list">
              <div class="menu-item" @click="router.push('/orders')">
                <div class="menu-item-icon" style="background: #fef3c7;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                </div>
                <div class="menu-item-text">
                  <span class="menu-item-title">我的订单</span>
                  <span class="menu-item-desc">查看停车订单记录</span>
                </div>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
              <div class="menu-item">
                <div class="menu-item-icon" style="background: #dbeafe;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2">
                    <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                    <line x1="1" y1="10" x2="23" y2="10"/>
                  </svg>
                </div>
                <div class="menu-item-text">
                  <span class="menu-item-title">我的车辆</span>
                  <span class="menu-item-desc">管理车牌信息</span>
                </div>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
              <div class="menu-item">
                <div class="menu-item-icon" style="background: #fce7f3;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ec4899" stroke-width="2">
                    <path d="M20 12v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-6"/>
                    <rect x="2" y="7" width="20" height="5" rx="1"/>
                    <line x1="12" y1="22" x2="12" y2="7"/>
                    <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/>
                    <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
                  </svg>
                </div>
                <div class="menu-item-text">
                  <span class="menu-item-title">优惠券</span>
                  <span class="menu-item-desc">查看可用优惠券</span>
                </div>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
              <div class="menu-item">
                <div class="menu-item-icon" style="background: #f0fdf4;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2">
                    <circle cx="12" cy="12" r="3"/>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
                  </svg>
                </div>
                <div class="menu-item-text">
                  <span class="menu-item-title">设置</span>
                  <span class="menu-item-desc">应用偏好设置</span>
                </div>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!isWechatUser" class="profile-card">
          <div class="card-header">
            <div class="card-header-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </div>
            <h3>修改密码</h3>
          </div>
          <div class="card-body">
            <form class="profile-form">
              <div class="form-group">
                <label class="form-label">原密码</label>
                <input v-model="passwordForm.oldPassword" type="password" class="form-input" placeholder="请输入原密码" />
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">新密码</label>
                  <input v-model="passwordForm.newPassword" type="password" class="form-input" placeholder="请输入新密码" />
                </div>
                <div class="form-group">
                  <label class="form-label">确认密码</label>
                  <input v-model="passwordForm.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" />
                </div>
              </div>
              <div class="form-actions">
                <button type="button" class="btn btn-secondary" @click="changePassword">修改密码</button>
              </div>
            </form>
          </div>
        </div>

        <div class="profile-card danger-card">
          <div class="card-header">
            <div class="card-header-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
            </div>
            <h3>账户操作</h3>
          </div>
          <div class="card-body">
            <div class="danger-actions">
              <div class="danger-info">
                <h4>退出登录</h4>
                <p>退出当前账户，需要重新登录才能继续使用</p>
              </div>
              <button type="button" class="btn btn-danger" @click="handleLogout">退出登录</button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isWechatUser = computed(() => userStore.user?.username?.startsWith('wx_'))

const userForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const avatarError = ref(false)

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await userStore.getProfile()
    const user = userStore.user
    if (user) {
      userForm.username = user.username || ''
      userForm.name = user.name || ''
      userForm.phone = user.phone || ''
      userForm.email = user.email || ''
    }
  }
})

const onAvatarError = () => {
  avatarError.value = true
}

const updateProfile = async () => {
  if (!userForm.name) {
    ElMessage.warning('请填写姓名')
    return
  }
  const success = await userStore.updateProfile(userForm)
  if (success) {
    ElMessage.success('个人信息更新成功')
  } else {
    ElMessage.error('个人信息更新失败')
  }
}

const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('密码长度至少为6位')
    return
  }
  const success = await userStore.changePassword(passwordForm.oldPassword, passwordForm.newPassword)
  if (success) {
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } else {
    ElMessage.error('密码修改失败，原密码错误')
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', sans-serif;
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  padding: 40px 24px;
  text-align: center;
}

.login-prompt h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  margin: 24px 0 8px;
}

.login-prompt p {
  color: #64748b;
  font-size: 0.9375rem;
  margin-bottom: 32px;
}

.profile-header {
  position: relative;
  padding: 0 24px 32px;
  padding-top: 60px;
}

.profile-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: #10b981;
  border-radius: 0 0 32px 32px;
}

.profile-avatar-section {
  position: relative;
  display: flex;
  align-items: center;
  gap: 20px;
  padding-top: 40px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 4px solid #ffffff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.default-avatar {
  background: #10b981;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
}

.wechat-avatar {
  overflow: hidden;
  padding: 0;
}

.wechat-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-user-info {
  position: relative;
}

.profile-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 4px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.profile-username {
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.875rem;
  margin: 0 0 8px;
}

.wechat-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 100px;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

.profile-content {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card {
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.card-header-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0fdf4;
}

.card-header h3 {
  font-size: 1.0625rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
}

.card-body {
  padding: 24px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 0.8125rem;
  font-weight: 500;
  color: #64748b;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
  font-size: 0.9375rem;
  color: #0f172a;
  transition: all 0.25s ease;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #10b981;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-input:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 28px;
  font-weight: 600;
  font-size: 0.9375rem;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
}

.btn-primary {
  background: #10b981;
  color: white;
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3);
}

.btn-primary:hover {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

.btn-secondary {
  background: #f1f5f9;
  color: #0f172a;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.danger-card .card-header-icon {
  background: #fef2f2;
}

.danger-card .card-header-icon svg {
  stroke: #ef4444;
}

.danger-card .card-header h3 {
  color: #ef4444;
}

.danger-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.danger-info h4 {
  margin: 0 0 4px;
  font-size: 1rem;
  font-weight: 600;
  color: #0f172a;
}

.danger-info p {
  margin: 0;
  font-size: 0.8125rem;
  color: #64748b;
}

.btn-danger {
  background: #ef4444;
  color: white;
  padding: 10px 24px;
  font-weight: 600;
  font-size: 0.875rem;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
}

.btn-danger:hover {
  background: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

@media (max-width: 640px) {
  .profile-header {
    padding: 0 16px 24px;
    padding-top: 48px;
  }

  .profile-header-bg {
    height: 180px;
    border-radius: 0 0 24px 24px;
  }

  .profile-avatar-section {
    gap: 16px;
    padding-top: 32px;
  }

  .profile-avatar {
    width: 64px;
    height: 64px;
  }

  .default-avatar {
    font-size: 1.5rem;
  }

  .profile-name {
    font-size: 1.25rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .card-body {
    padding: 16px;
  }

  .danger-actions {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .profile-header-decor {
    display: none;
  }
}

.profile-header-decor {
  position: absolute;
  bottom: 0;
  right: 24px;
  height: 80px;
  opacity: 0.15;
  object-fit: contain;
  pointer-events: none;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.2s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #f8fafc;
  margin: 0 -24px;
  padding: 16px 24px;
  border-radius: 12px;
}

.menu-item-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.menu-item-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #0f172a;
}

.menu-item-desc {
  font-size: 0.75rem;
  color: #94a3b8;
}
</style>
