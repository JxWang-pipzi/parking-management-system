<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PWAInstallPrompt from './components/PWAInstallPrompt.vue'
import OfflineIndicator from './components/OfflineIndicator.vue'
import TabBar from './components/TabBar.vue'

const route = useRoute()

const showTabBar = computed(() => {
  return route.matched.some(record => record.meta.showTabBar)
})
</script>

<template>
  <div class="app-container" :class="{ 'has-tabbar': showTabBar }">
    <OfflineIndicator />
    
    <router-view />
    
    <TabBar v-if="showTabBar" />
    
    <PWAInstallPrompt />
  </div>
</template>

<style>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-container.has-tabbar {
  padding-bottom: 64px;
}
</style>
