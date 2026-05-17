<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索订单" 
          class="search-input" 
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Plus" @click="openAddOrderDialog">
          新增订单
        </el-button>
      </div>
    </div>
    
    <el-table :data="filteredOrders" style="width: 100%" class="orders-table" stripe>
      <el-table-column prop="id" label="订单ID" width="80"></el-table-column>
      <el-table-column prop="plateNumber" label="车牌号" width="120"></el-table-column>
      <el-table-column prop="parkingLotId" label="停车场ID" width="100"></el-table-column>
      <el-table-column prop="parkingSpaceId" label="车位ID" width="80"></el-table-column>
      <el-table-column prop="amount" label="金额" width="100">
        <template #default="scope">
          <span v-if="scope.row.amount != null && Number(scope.row.amount) === 0" class="text-free">免费</span>
          <span v-else class="text-primary">¥{{ scope.row.amount != null ? Number(scope.row.amount).toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间"></el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button 
            type="primary" 
            circle 
            :icon="Edit" 
            size="small"
            @click="openEditOrderDialog(scope.row)"
          />
          <el-button 
            type="danger" 
            circle 
            :icon="Delete" 
            size="small"
            @click="deleteOrder(scope.row.id)"
          />
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog title="新增订单" v-model="addDialogVisible" width="500px" destroy-on-close>
      <el-form :model="addOrderForm" :rules="addOrderRules" ref="addOrderFormRef" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="addOrderForm.userId" placeholder="请输入用户ID"></el-input>
        </el-form-item>
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="addOrderForm.plateNumber" placeholder="请输入车牌号"></el-input>
        </el-form-item>
        <el-form-item label="停车场ID" prop="parkingLotId">
          <el-input-number v-model="addOrderForm.parkingLotId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="车位ID" prop="parkingSpaceId">
          <el-input-number v-model="addOrderForm.parkingSpaceId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="addOrderForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="addOrderForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待支付" :value="0"></el-option>
            <el-option label="已完成" :value="1"></el-option>
            <el-option label="已取消" :value="2"></el-option>
            <el-option label="停车中" :value="3"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addOrder">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog title="编辑订单" v-model="editDialogVisible" width="500px" destroy-on-close>
      <el-form :model="editOrderForm" :rules="editOrderRules" ref="editOrderFormRef" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="editOrderForm.userId" placeholder="请输入用户ID"></el-input>
        </el-form-item>
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="editOrderForm.plateNumber" placeholder="请输入车牌号"></el-input>
        </el-form-item>
        <el-form-item label="停车场ID" prop="parkingLotId">
          <el-input-number v-model="editOrderForm.parkingLotId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="车位ID" prop="parkingSpaceId">
          <el-input-number v-model="editOrderForm.parkingSpaceId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="editOrderForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editOrderForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待支付" :value="0"></el-option>
            <el-option label="已完成" :value="1"></el-option>
            <el-option label="已取消" :value="2"></el-option>
            <el-option label="停车中" :value="3"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateOrder">确定</el-button>
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

const orders = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const searchQuery = ref('')
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

const addOrderFormRef = ref(null)
const editOrderFormRef = ref(null)

const addOrderForm = ref({
  userId: '',
  plateNumber: '',
  parkingLotId: null,
  parkingSpaceId: null,
  amount: 0,
  status: 0
})

const editOrderForm = ref({
  id: null,
  userId: '',
  plateNumber: '',
  parkingLotId: null,
  parkingSpaceId: null,
  amount: 0,
  status: 0
})

const addOrderRules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' }
  ]
}

const editOrderRules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' }
  ]
}

const filteredOrders = computed(() => {
  if (!searchQuery.value) {
    return orders.value
  }
  const query = searchQuery.value.toLowerCase()
  return orders.value.filter(order => {
    return order.plateNumber?.toLowerCase().includes(query) ||
           order.id?.toString().includes(query)
  })
})

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: '' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '已完成', 2: '已取消', 3: '停车中' }
  return map[status] || '未知'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await apiClient.get('/order-management/list', { params: { page: 1, pageSize: 100 } })
    const rawData = res.data.data
    let list = []
    if (Array.isArray(rawData)) {
      list = rawData
    } else if (rawData && Array.isArray(rawData.records)) {
      list = rawData.records
    } else if (rawData && Array.isArray(rawData.list)) {
      list = rawData.list
    }
    orders.value = list
    console.log(`[成功][阶段1][加载订单列表] 时间：${Date.now()} | 参数：page=1,pageSize=100 | 结果：${orders.value.length}条记录`)
  } catch (e) {
    console.log(`[失败][阶段1][加载订单列表] 时间：${Date.now()} | 原因：${e.message} | 参数：page=1,pageSize=100`)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const openAddOrderDialog = () => {
  addOrderForm.value = {
    userId: '',
    plateNumber: '',
    parkingLotId: null,
    parkingSpaceId: null,
    amount: 0,
    status: 0
  }
  addDialogVisible.value = true
}

const openEditOrderDialog = (order) => {
  editOrderForm.value = { ...order }
  editDialogVisible.value = true
}

const addOrder = async () => {
  if (addOrderFormRef.value) {
    await addOrderFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const res = await apiClient.post('/order-management/create', addOrderForm.value)
          console.log(`[成功][阶段2][新增订单] 时间：${Date.now()} | 参数：${JSON.stringify(addOrderForm.value)} | 结果：${JSON.stringify(res.data)}`)
          ElMessage.success('添加成功')
          addDialogVisible.value = false
          loadOrders()
        } catch (e) {
          console.log(`[失败][阶段2][新增订单] 时间：${Date.now()} | 原因：${e.message} | 参数：${JSON.stringify(addOrderForm.value)}`)
          ElMessage.error('添加订单失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

const updateOrder = async () => {
  if (editOrderFormRef.value) {
    await editOrderFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const res = await apiClient.put(`/order-management/update/${editOrderForm.value.id}`, editOrderForm.value)
          console.log(`[成功][阶段2][更新订单] 时间：${Date.now()} | 参数：id=${editOrderForm.value.id} | 结果：${JSON.stringify(res.data)}`)
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          loadOrders()
        } catch (e) {
          console.log(`[失败][阶段2][更新订单状态] 时间：${Date.now()} | 原因：${e.message} | 参数：${JSON.stringify(editOrderForm.value)}`)
          ElMessage.error('更新订单失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

const deleteOrder = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个订单吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    try {
      const res = await apiClient.delete(`/order-management/delete/${id}`)
      console.log(`[成功][阶段2][删除订单] 时间：${Date.now()} | 参数：id=${id} | 结果：${JSON.stringify(res.data)}`)
      ElMessage.success('删除成功')
      loadOrders()
    } catch (e) {
      console.log(`[失败][阶段2][删除订单] 时间：${Date.now()} | 原因：${e.message} | 参数：id=${id}`)
      ElMessage.error('删除订单失败')
    }
  } catch {
  }
}

const onOrderMessage = (data) => {
  if (['ORDER_CREATED', 'ORDER_PAID', 'ORDER_CANCELLED', 'ORDER_COMPLETED'].includes(data.type)) {
    ElNotification({
      title: data.type === 'ORDER_CREATED' ? '新订单' : data.type === 'ORDER_PAID' ? '订单支付' : data.type === 'ORDER_COMPLETED' ? '订单完成' : '订单取消',
      message: `订单 #${data.orderId || ''} 已更新`,
      type: data.type === 'ORDER_CANCELLED' ? 'warning' : 'success',
      duration: 3000,
      position: 'top-right'
    })
    loadOrders()
  }
}

onMounted(() => {
  loadOrders()
  const token = localStorage.getItem('token')
  if (token) {
    wsManager.connect(token)
  }
  wsManager.on('system', onOrderMessage)
})

onUnmounted(() => {
  wsManager.off('system', onOrderMessage)
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

.orders-table {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(241, 245, 249, 0.9);
  backdrop-filter: blur(10px);
}

.orders-table :deep(.el-table__body-wrapper table),
.orders-table :deep(.el-table__header-wrapper table) {
  table-layout: fixed;
}

.orders-table :deep(.el-table__header-wrapper th) {
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.95) 100%);
  font-weight: 600;
  color: #0f172a;
  border-bottom: 2px solid rgba(226, 232, 240, 0.9);
}

.orders-table :deep(.el-table__body tr) {
  transition: all 0.2s ease;
}

.orders-table :deep(.el-table__body tr:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.orders-table :deep(.el-table__row--striped) {
  background: rgba(248, 250, 252, 0.6);
}

.orders-table :deep(.el-table__row--striped:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.orders-table :deep(.el-button--primary) {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

.orders-table :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -4px rgba(59, 130, 246, 0.5);
}

.orders-table :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(239, 68, 68, 0.4);
  transition: all 0.3s ease;
}

.orders-table :deep(.el-button--danger:hover) {
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

.text-primary {
  color: #10b981;
  font-weight: 600;
}

.text-free {
  color: #10b981;
  font-weight: 600;
}
</style>
