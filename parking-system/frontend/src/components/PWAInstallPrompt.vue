<template>
  <div v-if="showPrompt" class="pwa-install-prompt show">
    <div class="pwa-install-prompt-content">
      <div class="pwa-install-prompt-icon">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
          <circle cx="12" cy="10" r="3"/>
        </svg>
      </div>
      <div class="pwa-install-prompt-text">
        <h3>安装智慧停车应用</h3>
        <p>添加到主屏幕，获得更好的使用体验</p>
      </div>
      <div class="pwa-install-prompt-actions">
        <button class="btn btn-secondary" @click="dismissPrompt">稍后</button>
        <button class="btn btn-primary" @click="installPWA">安装</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const showPrompt = ref(false)
let deferredPrompt = null

onMounted(() => {
  // 监听beforeinstallprompt事件
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault()
    deferredPrompt = e
    
    // 检查是否已经拒绝过安装
    const dismissed = localStorage.getItem('pwa-install-dismissed')
    if (!dismissed) {
      showPrompt.value = true
    }
  })
  
  // 监听appinstalled事件
  window.addEventListener('appinstalled', () => {
    showPrompt.value = false
    deferredPrompt = null
    console.log('PWA应用已安装')
  })
})

const installPWA = async () => {
  if (deferredPrompt) {
    deferredPrompt.prompt()
    const { outcome } = await deferredPrompt.userChoice
    
    if (outcome === 'accepted') {
      console.log('用户接受安装PWA')
    } else {
      console.log('用户拒绝安装PWA')
    }
    
    deferredPrompt = null
    showPrompt.value = false
  }
}

const dismissPrompt = () => {
  showPrompt.value = false
  // 记录用户拒绝安装
  localStorage.setItem('pwa-install-dismissed', 'true')
  
  // 7天后再次提示
  setTimeout(() => {
    localStorage.removeItem('pwa-install-dismissed')
  }, 7 * 24 * 60 * 60 * 1000)
}
</script>

<style scoped>
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
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

.btn-secondary {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: var(--border-color);
}
</style>
