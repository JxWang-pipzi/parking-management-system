<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">支付管理</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索支付记录" 
          class="search-input" 
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Download" @click="handleExport">
          导出
        </el-button>
      </div>
    </div>
    
    <el-table :data="filteredPayments" style="width: 100%" class="payments-table" stripe>
      <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
      <el-table-column prop="transactionId" label="交易号" width="180"></el-table-column>
      <el-table-column prop="orderId" label="订单ID" width="80"></el-table-column>
      <el-table-column prop="amount" label="支付金额" width="100">
        <template #default="scope">
          <span class="text-primary">¥{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="100">
        <template #default="scope">
          <el-tag :type="getPaymentMethodType(scope.row.paymentMethod)">
            {{ getPaymentMethodText(scope.row.paymentMethod) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="支付状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button 
            type="warning" 
            circle 
            :icon="RefreshLeft" 
            size="small"
            @click="handleRefund(scope.row)"
            v-if="scope.row.status === 1"
          />
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog title="申请退款" v-model="refundDialogVisible" width="500px" destroy-on-close>
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="80px">
        <el-form-item label="交易号">
          <el-input v-model="refundForm.transactionId" disabled></el-input>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input v-model="refundForm.amount" disabled>
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0"
            :max="refundForm.amount"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入退款原因"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRefund">确认退款</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Search, Download, RefreshLeft } from '@element-plus/icons-vue'
import apiClient from '../../utils/api'
import wsManager from '../../utils/websocket'

const payments = ref([])
const loading = ref(false)
const searchQuery = ref('')
const refundDialogVisible = ref(false)
const refundFormRef = ref(null)

const refundForm = ref({
  transactionId: '',
  amount: 0,
  refundAmount: 0,
  reason: ''
})

const refundRules = {
  refundAmount: [
    { required: true, message: '请输入退款金额', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入退款原因', trigger: 'blur' }
  ]
}

const filteredPayments = computed(() => {
  if (!searchQuery.value) {
    return payments.value
  }
  const query = searchQuery.value.toLowerCase()
  return payments.value.filter(payment => {
    return payment.transactionId?.toLowerCase().includes(query) ||
           payment.orderId?.toString().includes(query)
  })
})

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '支付成功', 2: '支付失败', 3: '已退款' }
  return map[status] || '未知'
}

const getPaymentMethodType = (method) => {
  const map = { 0: 'success', 1: 'primary', 2: 'warning' }
  return map[method] || 'info'
}

const getPaymentMethodText = (method) => {
  const map = { 0: '微信支付', 1: '支付宝', 2: '现金' }
  return map[method] || '未知'
}

const loadPayments = async () => {
  loading.value = true
  try {
    const res = await apiClient.get('/payment/list', { params: { page: 1, pageSize: 100 } })
    const rawData = res.data.data
    let list = []
    if (Array.isArray(rawData)) {
      list = rawData
    } else if (rawData && Array.isArray(rawData.records)) {
      list = rawData.records
    }
    payments.value = list
    console.log(`[成功][阶段1][加载支付列表] 时间：${Date.now()} | 参数：page=1,pageSize=100 | 结果：${payments.value.length}条记录`)
  } catch (e) {
    console.log(`[失败][阶段1][加载支付列表] 时间：${Date.now()} | 原因：${e.message} | 参数：page=1,pageSize=100`)
    ElMessage.error('加载支付列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefund = (payment) => {
  refundForm.value = {
    transactionId: payment.transactionId,
    amount: payment.amount,
    refundAmount: payment.amount,
    reason: ''
  }
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (refundFormRef.value) {
    await refundFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          await ElMessageBox.confirm('确定要申请退款吗？', '提示', {
            type: 'warning',
            confirmButtonText: '确定',
            cancelButtonText: '取消'
          })
          try {
            const res = await apiClient.post('/payment/refund', {
              transactionId: refundForm.value.transactionId,
              refundAmount: refundForm.value.refundAmount,
              reason: refundForm.value.reason
            })
            console.log(`[成功][阶段2][申请退款] 时间：${Date.now()} | 参数：transactionId=${refundForm.value.transactionId},refundAmount=${refundForm.value.refundAmount} | 结果：${JSON.stringify(res.data)}`)
            ElMessage.success('退款申请成功')
            refundDialogVisible.value = false
            loadPayments()
          } catch (e) {
            console.log(`[失败][阶段2][申请退款] 时间：${Date.now()} | 原因：${e.message} | 参数：transactionId=${refundForm.value.transactionId}`)
            ElMessage.error('退款申请失败')
          }
        } catch {
        }
      }
    })
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const onSystemMessage = (data) => {
  if (!data || !data.type) return
  const paymentTypes = ['ORDER_PAID', 'ORDER_CANCELLED', 'ORDER_COMPLETED']
  if (paymentTypes.includes(data.type)) {
    console.log(`[成功][阶段3][支付WS推送] 时间：${Date.now()} | 类型：${data.type} | 结果：刷新支付列表`)
    loadPayments()
  }
  if (data.type === 'ORDER_PAID') {
    ElNotification({
      title: '支付通知',
      message: `订单 #${data.orderId || ''} 已支付`,
      type: 'success',
      duration: 3000,
      position: 'top-right'
    })
  }
}

onMounted(() => {
  loadPayments()
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

.payments-table {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(241, 245, 249, 0.9);
  backdrop-filter: blur(10px);
}

.payments-table :deep(.el-table__body-wrapper table),
.payments-table :deep(.el-table__header-wrapper table) {
  table-layout: fixed;
}

.payments-table :deep(.el-table__header-wrapper th) {
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.95) 100%);
  font-weight: 600;
  color: #0f172a;
  border-bottom: 2px solid rgba(226, 232, 240, 0.9);
}

.payments-table :deep(.el-table__body tr) {
  transition: all 0.2s ease;
}

.payments-table :deep(.el-table__body tr:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.payments-table :deep(.el-table__row--striped) {
  background: rgba(248, 250, 252, 0.6);
}

.payments-table :deep(.el-table__row--striped:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.payments-table :deep(.el-button--warning) {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(245, 158, 11, 0.4);
  transition: all 0.3s ease;
}

.payments-table :deep(.el-button--warning:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -4px rgba(245, 158, 11, 0.5);
}

.payments-table :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.payments-table :deep(.el-dialog__header) {
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.95) 100%);
  padding: 24px 24px 20px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.payments-table :deep(.el-dialog__title) {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.payments-table :deep(.el-dialog__body) {
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
}

.payments-table :deep(.el-dialog__footer) {
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
</style>
