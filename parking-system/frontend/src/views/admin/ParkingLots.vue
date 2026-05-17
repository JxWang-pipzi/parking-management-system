<template>
  <div class="page-container">
    <!-- Page Header -->
    <div class="page-header">
      <h2 class="page-title">停车场管理</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索停车场..." 
          class="search-input"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Plus" @click="openAddParkingLotDialog">
          添加停车场
        </el-button>
      </div>
    </div>
    
    <!-- Parking Lots List -->
    <div class="parking-lots-grid fade-in">
      <div v-for="lot in filteredParkingLots" :key="lot.id" class="parking-card">
        <div class="parking-card-header">
          <div class="parking-status" :class="lot.status === 1 ? 'active' : 'inactive'">
            {{ lot.status === 1 ? '开放' : '关闭' }}
          </div>
          <div class="parking-actions">
            <el-button 
              type="primary" 
              circle 
              :icon="Edit" 
              size="small"
              @click="openEditParkingLotDialog(lot)"
            />
            <el-button 
              type="danger" 
              circle 
              :icon="Delete" 
              size="small"
              @click="deleteParkingLot(lot.id)"
            />
          </div>
        </div>
        <div class="parking-card-body">
          <h3 class="parking-name">{{ lot.name }}</h3>
          <p class="parking-address">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            {{ lot.address }}
          </p>
          <div class="parking-stats">
            <div class="stat-item">
              <span class="stat-value">{{ lot.totalSpaces }}</span>
              <span class="stat-unit">总车位</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value available">{{ lot.availableSpaces }}</span>
              <span class="stat-unit">可用</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value price">¥{{ lot.hourlyRate }}</span>
              <span class="stat-unit">/小时</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Empty State -->
    <div v-if="filteredParkingLots.length === 0" class="empty-state">
      <el-empty description="暂无停车场">
        <el-button type="primary" :icon="Plus" @click="openAddParkingLotDialog">
          创建第一个停车场
        </el-button>
      </el-empty>
    </div>
    
    <el-dialog 
      :title="addDialogVisible ? '添加停车场' : '编辑停车场'" 
      v-model="dialogVisible" 
      width="600px" 
      destroy-on-close
      @close="closeDialog"
    >
      <el-form :model="currentForm" label-width="100px">
        <el-form-item label="停车场名称" required>
          <el-input v-model="currentForm.name" placeholder="请输入停车场名称" />
        </el-form-item>
        <el-form-item label="地址" required>
          <el-input v-model="currentForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总车位数">
              <el-input-number v-model="currentForm.totalSpaces" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可用车位数">
              <el-input-number v-model="currentForm.availableSpaces" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="小时收费">
          <el-input-number v-model="currentForm.hourlyRate" :min="0" :precision="2" :step="0.5" style="width: 100%">
            <template #prefix>¥</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentForm.status" style="width: 100%">
            <el-option label="开放" :value="1" />
            <el-option label="关闭" :value="0" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input-number v-model="currentForm.latitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input-number v-model="currentForm.longitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            {{ addDialogVisible ? '添加' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { useParkingStore } from '../../store/parking'
import { Search, Plus, Edit, Delete, Location, Van, Clock } from '@element-plus/icons-vue'
import wsManager from '../../utils/websocket'

const parkingStore = useParkingStore()

const parkingLots = computed(() => parkingStore.parkingLots)

const searchQuery = ref('')
const addDialogVisible = ref(false)
    const editDialogVisible = ref(false)
    const dialogVisible = computed({
      get: () => addDialogVisible.value || editDialogVisible.value,
      set: (val) => {
        if (!val) closeDialog()
      }
    })
const currentForm = ref({
  id: null,
  name: '',
  address: '',
  totalSpaces: 0,
  availableSpaces: 0,
  hourlyRate: 0,
  latitude: 0,
  longitude: 0,
  status: 1
})

const totalSpaces = computed(() => {
  return parkingLots.value.reduce((sum, lot) => sum + lot.totalSpaces, 0)
})

const availableSpaces = computed(() => {
  return parkingLots.value.reduce((sum, lot) => sum + lot.availableSpaces, 0)
})

const filteredParkingLots = computed(() => {
  if (!searchQuery.value) {
    return parkingLots.value
  }
  const query = searchQuery.value.toLowerCase()
  return parkingLots.value.filter(lot => {
    return lot.name.toLowerCase().includes(query) || lot.address.toLowerCase().includes(query)
  })
})

const openAddParkingLotDialog = () => {
  currentForm.value = {
    id: null,
    name: '',
    address: '',
    totalSpaces: 0,
    availableSpaces: 0,
    hourlyRate: 0,
    latitude: 0,
    longitude: 0,
    status: 1
  }
  addDialogVisible.value = true
}

const openEditParkingLotDialog = (lot) => {
  currentForm.value = { ...lot }
  editDialogVisible.value = true
}

const closeDialog = () => {
  addDialogVisible.value = false
  editDialogVisible.value = false
}

const handleSubmit = async () => {
  if (!currentForm.value.name || !currentForm.value.address) {
    ElMessage.warning('请填写必填信息')
    return
  }
  
  if (addDialogVisible.value) {
    const success = await parkingStore.addParkingLot(currentForm.value)
    if (success) {
      ElMessage.success('停车场添加成功')
    } else {
      ElMessage.error('添加失败')
    }
  } else {
    const success = await parkingStore.updateParkingLot(currentForm.value.id, currentForm.value)
    if (success) {
      ElMessage.success('停车场更新成功')
    } else {
      ElMessage.error('更新失败')
    }
  }
  closeDialog()
}

const deleteParkingLot = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个停车场吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    const success = await parkingStore.deleteParkingLot(id)
    if (success) {
      ElMessage.success('停车场删除成功')
    } else {
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 组件挂载时获取停车场数据
const onSystemMessage = (data) => {
  if (!data || !data.type) return
  const parkingLotTypes = ['ORDER_CREATED', 'ORDER_PAID', 'ORDER_COMPLETED', 'ORDER_CANCELLED', 'SPACE_UPDATE', 'PARKING_LOT_UPDATE']
  if (parkingLotTypes.includes(data.type)) {
    console.log(`[成功][阶段3][停车场WS推送] 时间：${Date.now()} | 类型：${data.type} | 结果：刷新停车场数据`)
    parkingStore.getParkingLots()
  }
  if (data.type === 'SPACE_UPDATE') {
    ElNotification({
      title: '车位变更',
      message: `停车场车位信息已更新`,
      type: 'info',
      duration: 3000,
      position: 'top-right'
    })
  }
}

onMounted(async () => {
  await parkingStore.getParkingLots()
  const token = localStorage.getItem('token')
  if (token) {
    wsManager.connect(token)
  }
  wsManager.on('system', onSystemMessage)
})

onUnmounted(() => {
  wsManager.off('system', onSystemMessage)
})
</script>

<style scoped>
.page-container {
  width: 100%;
  max-width: 100%;
  min-height: 100%;
  overflow-x: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.02em;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-input {
  width: 250px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 14px;
  border: 1.5px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.8);
  box-shadow: none;
  transition: all 0.3s ease;
  padding: 10px 16px;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(203, 213, 225, 1);
  background: rgba(255, 255, 255, 0.9);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #10b981;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.12);
}

.header-actions .el-button--primary {
  border-radius: 14px;
  padding: 10px 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 4px 14px -4px rgba(16, 185, 129, 0.35);
  transition: all 0.3s ease;
}

.header-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px -6px rgba(16, 185, 129, 0.45);
}

.parking-lots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-top: 24px;
}

.parking-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(241, 245, 249, 0.9);
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.parking-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px -8px rgba(0, 0, 0, 0.12);
  border-color: rgba(226, 232, 240, 1);
}

.parking-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.parking-status {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  border: none;
}

.parking-status.active {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #065f46;
}

.parking-status.inactive {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #991b1b;
}

.parking-actions {
  display: flex;
  gap: 8px;
}

.parking-actions .el-button--primary {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

.parking-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -4px rgba(59, 130, 246, 0.5);
}

.parking-actions .el-button--danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(239, 68, 68, 0.4);
  transition: all 0.3s ease;
}

.parking-actions .el-button--danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -4px rgba(239, 68, 68, 0.5);
}

.parking-name {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.01em;
}

.parking-address {
  margin: 0;
  color: #64748b;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 6px;
}

.parking-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
  margin-top: auto;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 1.125rem;
  font-weight: 700;
  color: #0f172a;
}

.stat-value.available {
  color: #10b981;
}

.stat-value.price {
  color: #10b981;
}

.stat-unit {
  font-size: 0.75rem;
  color: #64748b;
  margin-top: 4px;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: linear-gradient(180deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.empty-state {
  margin-top: 100px;
}

.page-container :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.page-container :deep(.el-dialog__header) {
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.95) 100%);
  padding: 24px 24px 20px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.page-container :deep(.el-dialog__title) {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.page-container :deep(.el-dialog__body) {
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
}

.page-container :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.95) 100%);
  border-top: 1px solid rgba(226, 232, 240, 0.8);
}

.page-container :deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

.page-container :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 1.5px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.8);
  box-shadow: none;
  transition: all 0.3s ease;
}

.page-container :deep(.el-input__wrapper:hover) {
  border-color: rgba(203, 213, 225, 1);
  background: rgba(255, 255, 255, 0.9);
}

.page-container :deep(.el-input__wrapper.is-focus) {
  border-color: #10b981;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.12);
}

.page-container :deep(.el-input-number .el-input__wrapper) {
  padding-right: 8px;
}

.page-container :deep(.el-select .el-input__wrapper) {
  padding-right: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button:not(.el-button--primary) {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 500;
  border: 1.5px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.8);
  color: #64748b;
  transition: all 0.3s ease;
}

.dialog-footer .el-button:not(.el-button--primary):hover {
  border-color: rgba(203, 213, 225, 1);
  background: rgba(255, 255, 255, 0.9);
  color: #475569;
}

.dialog-footer .el-button--primary {
  border-radius: 12px;
  padding: 10px 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 4px 14px -4px rgba(16, 185, 129, 0.35);
  transition: all 0.3s ease;
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px -6px rgba(16, 185, 129, 0.45);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
  
  .header-actions .el-button {
    width: 100%;
  }
}
</style>