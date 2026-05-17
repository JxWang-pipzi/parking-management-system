<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">车位管理</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索车位" 
          class="search-input" 
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Plus" @click="openAddParkingSpaceDialog">
          添加车位
        </el-button>
      </div>
    </div>
    
    <el-table :data="filteredParkingSpaces" style="width: 100%" class="parking-spaces-table" stripe>
      <el-table-column prop="id" label="车位ID" width="80"></el-table-column>
      <el-table-column prop="parkingLotId" label="停车场ID" width="100"></el-table-column>
      <el-table-column prop="parkingLotName" label="停车场名称"></el-table-column>
      <el-table-column prop="spaceNumber" label="车位编号"></el-table-column>
      <el-table-column prop="type" label="车位类型" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.type === 1 ? 'primary' : 'warning'">
            {{ scope.row.type === 1 ? '普通车位' : 'VIP车位' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
            {{ scope.row.status === 0 ? '空闲' : '占用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            circle 
            :icon="Edit" 
            size="small"
            @click="openEditParkingSpaceDialog(scope.row)"
          />
          <el-button 
            type="danger" 
            circle 
            :icon="Delete" 
            size="small"
            @click="deleteParkingSpace(scope.row.id)"
          />
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog title="添加车位" v-model="addDialogVisible" width="500px" destroy-on-close>
      <el-form :model="addParkingSpaceForm" :rules="addParkingSpaceRules" ref="addParkingSpaceFormRef" label-width="80px">
        <el-form-item label="停车场" prop="parkingLotId">
          <el-select v-model="addParkingSpaceForm.parkingLotId" placeholder="请选择停车场" style="width: 100%">
            <el-option v-for="lot in parkingLots" :key="lot.id" :label="lot.name" :value="lot.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="车位编号" prop="spaceNumber">
          <el-input v-model="addParkingSpaceForm.spaceNumber" placeholder="请输入车位编号"></el-input>
        </el-form-item>
        <el-form-item label="车位类型" prop="type">
          <el-select v-model="addParkingSpaceForm.type" placeholder="请选择车位类型" style="width: 100%">
            <el-option label="普通车位" :value="1"></el-option>
            <el-option label="VIP车位" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="addParkingSpaceForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="空闲" :value="0"></el-option>
            <el-option label="占用" :value="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addParkingSpace">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog title="编辑车位" v-model="editDialogVisible" width="500px" destroy-on-close>
      <el-form :model="editParkingSpaceForm" :rules="editParkingSpaceRules" ref="editParkingSpaceFormRef" label-width="80px">
        <el-form-item label="停车场" prop="parkingLotId">
          <el-select v-model="editParkingSpaceForm.parkingLotId" placeholder="请选择停车场" style="width: 100%">
            <el-option v-for="lot in parkingLots" :key="lot.id" :label="lot.name" :value="lot.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="车位编号" prop="spaceNumber">
          <el-input v-model="editParkingSpaceForm.spaceNumber" placeholder="请输入车位编号"></el-input>
        </el-form-item>
        <el-form-item label="车位类型" prop="type">
          <el-select v-model="editParkingSpaceForm.type" placeholder="请选择车位类型" style="width: 100%">
            <el-option label="普通车位" :value="1"></el-option>
            <el-option label="VIP车位" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editParkingSpaceForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="空闲" :value="0"></el-option>
            <el-option label="占用" :value="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateParkingSpace">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useParkingStore } from '../../store/parking'
import apiClient from '../../utils/api'
import wsManager from '../../utils/websocket'

const parkingStore = useParkingStore()

const parkingLots = computed(() => parkingStore.parkingLots)

const parkingSpaces = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const searchQuery = ref('')
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

const addParkingSpaceFormRef = ref(null)
const editParkingSpaceFormRef = ref(null)

const addParkingSpaceForm = ref({
  parkingLotId: null,
  spaceNumber: '',
  type: 1,
  status: 0
})

const editParkingSpaceForm = ref({
  id: null,
  parkingLotId: null,
  spaceNumber: '',
  type: 1,
  status: 0
})

const addParkingSpaceRules = {
  parkingLotId: [
    { required: true, message: '请选择停车场', trigger: 'change' }
  ],
  spaceNumber: [
    { required: true, message: '请输入车位编号', trigger: 'blur' }
  ]
}

const editParkingSpaceRules = {
  parkingLotId: [
    { required: true, message: '请选择停车场', trigger: 'change' }
  ],
  spaceNumber: [
    { required: true, message: '请输入车位编号', trigger: 'blur' }
  ]
}

const filteredParkingSpaces = computed(() => {
  if (!searchQuery.value) {
    return parkingSpaces.value
  }
  const query = searchQuery.value.toLowerCase()
  return parkingSpaces.value.filter(space => {
    return space.spaceNumber?.toLowerCase().includes(query) ||
           space.parkingLotName?.toLowerCase().includes(query)
  })
})

const loadParkingSpaces = async () => {
  loading.value = true
  try {
    const res = await apiClient.get('/parking-spaces')
    const data = res.data.data || res.data.records || []
    parkingSpaces.value = Array.isArray(data) ? data : (Array.isArray(res.data) ? res.data : [])
    console.log(`[成功][阶段1][加载车位列表] 时间：${Date.now()} | 参数：无 | 结果：${parkingSpaces.value.length}条记录`)
  } catch (e) {
    console.log(`[失败][阶段1][加载车位列表] 时间：${Date.now()} | 原因：${e.message} | 参数：无`)
    ElMessage.error('加载车位列表失败')
  } finally {
    loading.value = false
  }
}

const openAddParkingSpaceDialog = () => {
  addParkingSpaceForm.value = {
    parkingLotId: null,
    spaceNumber: '',
    type: 1,
    status: 0
  }
  addDialogVisible.value = true
}

const openEditParkingSpaceDialog = (parkingSpace) => {
  editParkingSpaceForm.value = { ...parkingSpace }
  editDialogVisible.value = true
}

const addParkingSpace = async () => {
  if (addParkingSpaceFormRef.value) {
    await addParkingSpaceFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const res = await apiClient.post('/parking-spaces', addParkingSpaceForm.value)
          console.log(`[成功][阶段2][添加车位] 时间：${Date.now()} | 参数：${JSON.stringify(addParkingSpaceForm.value)} | 结果：${JSON.stringify(res.data)}`)
          ElMessage.success('添加成功')
          addDialogVisible.value = false
          loadParkingSpaces()
        } catch (e) {
          console.log(`[失败][阶段2][添加车位] 时间：${Date.now()} | 原因：${e.message} | 参数：${JSON.stringify(addParkingSpaceForm.value)}`)
          ElMessage.error('添加车位失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

const updateParkingSpace = async () => {
  if (editParkingSpaceFormRef.value) {
    await editParkingSpaceFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const { id, ...updateData } = editParkingSpaceForm.value
          const res = await apiClient.put(`/parking-spaces/${id}`, updateData)
          console.log(`[成功][阶段2][更新车位] 时间：${Date.now()} | 参数：id=${id} | 结果：${JSON.stringify(res.data)}`)
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          loadParkingSpaces()
        } catch (e) {
          console.log(`[失败][阶段2][更新车位] 时间：${Date.now()} | 原因：${e.message} | 参数：${JSON.stringify(editParkingSpaceForm.value)}`)
          ElMessage.error('更新车位失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

const deleteParkingSpace = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个车位吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    try {
      const res = await apiClient.delete(`/parking-spaces/${id}`)
      console.log(`[成功][阶段2][删除车位] 时间：${Date.now()} | 参数：id=${id} | 结果：${JSON.stringify(res.data)}`)
      ElMessage.success('删除成功')
      loadParkingSpaces()
    } catch (e) {
      console.log(`[失败][阶段2][删除车位] 时间：${Date.now()} | 原因：${e.message} | 参数：id=${id}`)
      ElMessage.error('删除车位失败')
    }
  } catch {
  }
}

const onSpaceMessage = (data) => {
  if (['ORDER_CREATED', 'ORDER_PAID', 'ORDER_CANCELLED'].includes(data.type)) {
    ElNotification({
      title: '车位状态变更',
      message: `停车场 #${data.parkingLotId || ''} 车位状态已更新`,
      type: 'info',
      duration: 3000,
      position: 'top-right'
    })
    loadParkingSpaces()
  }
}

onMounted(async () => {
  await parkingStore.getParkingLots()
  loadParkingSpaces()
  const token = localStorage.getItem('token')
  if (token) {
    wsManager.connect(token)
  }
  wsManager.on('system', onSpaceMessage)
})

onUnmounted(() => {
  wsManager.off('system', onSpaceMessage)
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

.parking-spaces-table {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(241, 245, 249, 0.9);
  backdrop-filter: blur(10px);
}

.parking-spaces-table :deep(.el-table__header-wrapper th) {
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.95) 100%);
  font-weight: 600;
  color: #0f172a;
  border-bottom: 2px solid rgba(226, 232, 240, 0.9);
}

.parking-spaces-table :deep(.el-table__body tr) {
  transition: all 0.2s ease;
}

.parking-spaces-table :deep(.el-table__body tr:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.parking-spaces-table :deep(.el-table__row--striped) {
  background: rgba(248, 250, 252, 0.6);
}

.parking-spaces-table :deep(.el-table__row--striped:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.parking-spaces-table :deep(.el-button--primary) {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

.parking-spaces-table :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -4px rgba(59, 130, 246, 0.5);
}

.parking-spaces-table :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(239, 68, 68, 0.4);
  transition: all 0.3s ease;
}

.parking-spaces-table :deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -4px rgba(239, 68, 68, 0.5);
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

.page-container :deep(.el-tag) {
  border-radius: 8px;
  font-weight: 500;
  padding: 4px 12px;
  border: none;
}

.page-container :deep(.el-tag--success) {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #065f46;
}

.page-container :deep(.el-tag--danger) {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #991b1b;
}

.page-container :deep(.el-tag--warning) {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
}

.page-container :deep(.el-tag--info) {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
}

.page-container :deep(.el-tag--primary) {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
}
</style>
