<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">预约管理</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索预约" 
          class="search-input" 
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Plus" @click="openAddReservationDialog">
          新增预约
        </el-button>
      </div>
    </div>
    
    <el-table :data="filteredReservations" style="width: 100%" class="reservations-table" stripe>
      <el-table-column prop="id" label="预约ID" width="80"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="80"></el-table-column>
      <el-table-column prop="plateNumber" label="车牌号" width="120"></el-table-column>
      <el-table-column prop="parkingLotId" label="停车场ID" width="100"></el-table-column>
      <el-table-column prop="spaceId" label="车位ID" width="80"></el-table-column>
      <el-table-column prop="reservationTime" label="预约时间"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button 
            type="primary" 
            circle 
            :icon="Edit" 
            size="small"
            @click="openEditReservationDialog(scope.row)"
          />
          <el-button 
            type="danger" 
            circle 
            :icon="Delete" 
            size="small"
            @click="deleteReservation(scope.row.id)"
          />
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog title="新增预约" v-model="addDialogVisible" width="500px" destroy-on-close>
      <el-form :model="addReservationForm" :rules="addReservationRules" ref="addReservationFormRef" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="addReservationForm.userId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="addReservationForm.plateNumber" placeholder="请输入车牌号"></el-input>
        </el-form-item>
        <el-form-item label="停车场ID" prop="parkingLotId">
          <el-input-number v-model="addReservationForm.parkingLotId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="车位ID" prop="spaceId">
          <el-input-number v-model="addReservationForm.spaceId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预约时间" prop="reservationTime">
          <el-date-picker
            v-model="addReservationForm.reservationTime"
            type="datetime"
            placeholder="选择预约时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="addReservationForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待使用" :value="0"></el-option>
            <el-option label="已使用" :value="1"></el-option>
            <el-option label="已取消" :value="2"></el-option>
            <el-option label="已过期" :value="3"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addReservation">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog title="编辑预约" v-model="editDialogVisible" width="500px" destroy-on-close>
      <el-form :model="editReservationForm" :rules="editReservationRules" ref="editReservationFormRef" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="editReservationForm.userId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="editReservationForm.plateNumber" placeholder="请输入车牌号"></el-input>
        </el-form-item>
        <el-form-item label="停车场ID" prop="parkingLotId">
          <el-input-number v-model="editReservationForm.parkingLotId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="车位ID" prop="spaceId">
          <el-input-number v-model="editReservationForm.spaceId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预约时间" prop="reservationTime">
          <el-date-picker
            v-model="editReservationForm.reservationTime"
            type="datetime"
            placeholder="选择预约时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editReservationForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待使用" :value="0"></el-option>
            <el-option label="已使用" :value="1"></el-option>
            <el-option label="已取消" :value="2"></el-option>
            <el-option label="已过期" :value="3"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateReservation">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import apiClient from '../../utils/api'
import wsManager from '../../utils/websocket'

const reservations = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const searchQuery = ref('')
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

const addReservationFormRef = ref(null)
const editReservationFormRef = ref(null)

const addReservationForm = ref({
  userId: null,
  plateNumber: '',
  parkingLotId: null,
  spaceId: null,
  reservationTime: '',
  duration: 0,
  amount: 0,
  status: 0
})

const editReservationForm = ref({
  id: null,
  userId: null,
  plateNumber: '',
  parkingLotId: null,
  spaceId: null,
  reservationTime: '',
  duration: 0,
  amount: 0,
  status: 0
})

const addReservationRules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' }
  ]
}

const editReservationRules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' }
  ]
}

const filteredReservations = computed(() => {
  if (!searchQuery.value) {
    return reservations.value
  }
  const query = searchQuery.value.toLowerCase()
  return reservations.value.filter(reservation => {
    return reservation.plateNumber?.toLowerCase().includes(query) ||
           reservation.id?.toString().includes(query)
  })
})

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待使用', 1: '已使用', 2: '已取消', 3: '已过期' }
  return map[status] || '未知'
}

const loadReservations = async () => {
  loading.value = true
  try {
    const res = await apiClient.get('/reservation/all', { params: { page: 1, size: 100 } })
    const rawData = res.data.data
    let list = []
    if (Array.isArray(rawData)) {
      list = rawData
    } else if (rawData && Array.isArray(rawData.records)) {
      list = rawData.records
    }
    reservations.value = list
    console.log(`[成功][阶段1][加载预约列表] 时间：${Date.now()} | 参数：page=1,size=100 | 结果：${reservations.value.length}条记录`)
  } catch (e) {
    console.log(`[失败][阶段1][加载预约列表] 时间：${Date.now()} | 原因：${e.message} | 参数：page=1,size=100`)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const openAddReservationDialog = () => {
  addReservationForm.value = {
    userId: null,
    plateNumber: '',
    parkingLotId: null,
    spaceId: null,
    reservationTime: '',
    duration: 0,
    amount: 0,
    status: 0
  }
  addDialogVisible.value = true
}

const openEditReservationDialog = (reservation) => {
  editReservationForm.value = { ...reservation }
  editDialogVisible.value = true
}

const addReservation = async () => {
  if (addReservationFormRef.value) {
    await addReservationFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const res = await apiClient.post('/reservation/create', addReservationForm.value)
          console.log(`[成功][阶段2][新增预约] 时间：${Date.now()} | 参数：${JSON.stringify(addReservationForm.value)} | 结果：${JSON.stringify(res.data)}`)
          ElMessage.success('添加成功')
          addDialogVisible.value = false
          loadReservations()
        } catch (e) {
          console.log(`[失败][阶段2][新增预约] 时间：${Date.now()} | 原因：${e.message} | 参数：${JSON.stringify(addReservationForm.value)}`)
          ElMessage.error('添加预约失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

const updateReservation = async () => {
  if (editReservationFormRef.value) {
    await editReservationFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const res = await apiClient.put('/reservation/update', editReservationForm.value)
          console.log(`[成功][阶段2][更新预约] 时间：${Date.now()} | 参数：${JSON.stringify(editReservationForm.value)} | 结果：${JSON.stringify(res.data)}`)
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          loadReservations()
        } catch (e) {
          console.log(`[失败][阶段2][更新预约] 时间：${Date.now()} | 原因：${e.message} | 参数：${JSON.stringify(editReservationForm.value)}`)
          ElMessage.error('更新预约失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

const deleteReservation = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个预约吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    try {
      const res = await apiClient.delete(`/reservation/${id}`)
      console.log(`[成功][阶段2][删除预约] 时间：${Date.now()} | 参数：id=${id} | 结果：${JSON.stringify(res.data)}`)
      ElMessage.success('删除成功')
      loadReservations()
    } catch (e) {
      console.log(`[失败][阶段2][删除预约] 时间：${Date.now()} | 原因：${e.message} | 参数：id=${id}`)
      ElMessage.error('删除预约失败')
    }
  } catch {
  }
}

const onSystemMessage = (data) => {
  if (!data || !data.type) return
  const reservationTypes = ['ORDER_CREATED', 'ORDER_CANCELLED', 'ORDER_COMPLETED']
  if (reservationTypes.includes(data.type)) {
    console.log(`[成功][阶段3][预约WS推送] 时间：${Date.now()} | 类型：${data.type} | 结果：刷新预约列表`)
    loadReservations()
  }
  if (data.type === 'ORDER_CREATED') {
    ElNotification({
      title: '新预约',
      message: `预约 #${data.orderId || ''} 已创建`,
      type: 'info',
      duration: 3000,
      position: 'top-right'
    })
  }
}

onMounted(() => {
  loadReservations()
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

.reservations-table {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(241, 245, 249, 0.9);
  backdrop-filter: blur(10px);
}

.reservations-table :deep(.el-table__body-wrapper table),
.reservations-table :deep(.el-table__header-wrapper table) {
  table-layout: fixed;
}

.reservations-table :deep(.el-table__header-wrapper th) {
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.95) 100%);
  font-weight: 600;
  color: #0f172a;
  border-bottom: 2px solid rgba(226, 232, 240, 0.9);
}

.reservations-table :deep(.el-table__body tr) {
  transition: all 0.2s ease;
}

.reservations-table :deep(.el-table__body tr:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.reservations-table :deep(.el-table__row--striped) {
  background: rgba(248, 250, 252, 0.6);
}

.reservations-table :deep(.el-table__row--striped:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.reservations-table :deep(.el-button--primary) {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

.reservations-table :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -4px rgba(59, 130, 246, 0.5);
}

.reservations-table :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(239, 68, 68, 0.4);
  transition: all 0.3s ease;
}

.reservations-table :deep(.el-button--danger:hover) {
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
