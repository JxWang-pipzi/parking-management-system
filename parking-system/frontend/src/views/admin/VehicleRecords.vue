<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">车辆进出管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchQuery"
          placeholder="搜索车牌号"
          class="search-input"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Plus" @click="openEntryDialog">
          车辆入场
        </el-button>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card stat-card-blue">
        <div class="stat-card-header">
          <div class="stat-icon">
            <el-icon :size="24"><Van /></el-icon>
          </div>
        </div>
        <h3 class="stat-value">{{ stats.currentIn }}</h3>
        <p class="stat-label">当前在场车辆</p>
      </div>
      <div class="stat-card stat-card-green">
        <div class="stat-card-header">
          <div class="stat-icon">
            <el-icon :size="24"><Right /></el-icon>
          </div>
        </div>
        <h3 class="stat-value">{{ stats.todayEntry }}</h3>
        <p class="stat-label">今日入场</p>
      </div>
      <div class="stat-card stat-card-orange">
        <div class="stat-card-header">
          <div class="stat-icon">
            <el-icon :size="24"><Back /></el-icon>
          </div>
        </div>
        <h3 class="stat-value">{{ stats.todayExit }}</h3>
        <p class="stat-label">今日出场</p>
      </div>
    </div>

    <el-table :data="filteredRecords" style="width: 100%" class="records-table" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70"></el-table-column>
      <el-table-column prop="plateNumber" label="车牌号" width="130"></el-table-column>
      <el-table-column prop="parkingLotName" label="停车场名称" width="160"></el-table-column>
      <el-table-column prop="spaceNumber" label="车位编号" width="100"></el-table-column>
      <el-table-column prop="entryTime" label="入场时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.entryTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="exitTime" label="出场时间" width="180">
        <template #default="scope">
          {{ scope.row.exitTime ? formatDateTime(scope.row.exitTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
            {{ scope.row.status === 0 ? '在场' : '已离场' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="recognitionConfidence" label="识别置信度" width="120">
        <template #default="scope">
          <span v-if="scope.row.recognitionConfidence != null">
            {{ scope.row.recognitionConfidence }}%
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            type="danger"
            circle
            :icon="SwitchButton"
            size="small"
            @click="handleExit(scope.row)"
          />
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="车辆入场" v-model="entryDialogVisible" width="560px" destroy-on-close>
      <el-tabs v-model="entryTab">
        <el-tab-pane label="拍照识别入场" name="photo">
          <el-form :model="photoEntryForm" :rules="photoEntryRules" ref="photoEntryFormRef" label-width="100px">
            <el-form-item label="车牌照片" prop="file">
              <el-upload
                class="plate-uploader"
                :http-request="handlePlateUpload"
                :show-file-list="false"
                accept="image/*"
                :before-upload="beforeUpload"
              >
                <img v-if="photoEntryForm.imageUrl" :src="photoEntryForm.imageUrl" class="plate-preview" />
                <div v-else class="upload-placeholder">
                  <el-icon :size="32"><UploadFilled /></el-icon>
                  <span>点击上传车牌照片</span>
                </div>
              </el-upload>
            </el-form-item>
            <el-form-item label="识别车牌号" prop="plateNumber">
              <el-input v-model="photoEntryForm.plateNumber" placeholder="识别后可编辑">
                <template #suffix>
                  <el-tag v-if="photoEntryForm.recognized" type="success" size="small">已识别</el-tag>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="停车场" prop="parkingLotId">
              <el-select v-model="photoEntryForm.parkingLotId" placeholder="请选择停车场" style="width: 100%">
                <el-option
                  v-for="lot in parkingLots"
                  :key="lot.id"
                  :label="lot.name"
                  :value="lot.id"
                />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="手动输入入场" name="manual">
          <el-form :model="manualEntryForm" :rules="manualEntryRules" ref="manualEntryFormRef" label-width="100px">
            <el-form-item label="车牌号" prop="plateNumber">
              <el-input v-model="manualEntryForm.plateNumber" placeholder="请输入车牌号"></el-input>
            </el-form-item>
            <el-form-item label="停车场" prop="parkingLotId">
              <el-select v-model="manualEntryForm.parkingLotId" placeholder="请选择停车场" style="width: 100%">
                <el-option
                  v-for="lot in parkingLots"
                  :key="lot.id"
                  :label="lot.name"
                  :value="lot.id"
                />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="entryDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitEntry">确认入场</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Search, Plus, Van, Right, Back, SwitchButton, UploadFilled } from '@element-plus/icons-vue'
import apiClient from '../../utils/api'
import wsManager from '../../utils/websocket'

const records = ref([])
const parkingLots = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const searchQuery = ref('')
const entryDialogVisible = ref(false)
const entryTab = ref('photo')

const photoEntryFormRef = ref(null)
const manualEntryFormRef = ref(null)

const stats = ref({
  currentIn: 0,
  todayEntry: 0,
  todayExit: 0
})

const photoEntryForm = ref({
  imageUrl: '',
  plateNumber: '',
  parkingLotId: null,
  recognized: false
})

const manualEntryForm = ref({
  plateNumber: '',
  parkingLotId: null
})

const photoEntryRules = {
  plateNumber: [
    { required: true, message: '请输入或识别车牌号', trigger: 'blur' }
  ],
  parkingLotId: [
    { required: true, message: '请选择停车场', trigger: 'change' }
  ]
}

const manualEntryRules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' }
  ],
  parkingLotId: [
    { required: true, message: '请选择停车场', trigger: 'change' }
  ]
}

const filteredRecords = computed(() => {
  if (!searchQuery.value) {
    return records.value
  }
  const query = searchQuery.value.toLowerCase()
  return records.value.filter(record => {
    return record.plateNumber?.toLowerCase().includes(query) ||
           record.id?.toString().includes(query)
  })
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const d = new Date(dateTime)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const computeStats = () => {
  const today = new Date()
  const todayStr = today.toDateString()
  let currentIn = 0
  let todayEntry = 0
  let todayExit = 0
  records.value.forEach(record => {
    if (record.status === 0) currentIn++
    if (record.entryTime && new Date(record.entryTime).toDateString() === todayStr) todayEntry++
    if (record.status === 1 && record.exitTime && new Date(record.exitTime).toDateString() === todayStr) todayExit++
  })
  stats.value = { currentIn, todayEntry, todayExit }
  console.log(`[成功][阶段4][计算统计] 时间：${Date.now()} | 参数：${records.value.length}条记录 | 结果：在场${currentIn},入场${todayEntry},出场${todayExit}`)
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await apiClient.get('/vehicle-records')
    const rawData = res.data.data
    let list = []
    if (Array.isArray(rawData)) {
      list = rawData
    } else if (rawData && Array.isArray(rawData.records)) {
      list = rawData.records
    }
    records.value = list
    computeStats()
    console.log(`[成功][阶段1][加载车辆记录] 时间：${Date.now()} | 参数：无 | 结果：${list.length}条记录`)
  } catch (e) {
    console.log(`[失败][阶段1][加载车辆记录] 时间：${Date.now()} | 原因：${e.message} | 参数：无`)
    ElMessage.error('加载车辆记录失败')
  } finally {
    loading.value = false
  }
}

const loadParkingLots = async () => {
  try {
    const res = await apiClient.get('/parking-lots')
    const rawData = res.data.data
    if (Array.isArray(rawData)) {
      parkingLots.value = rawData
    } else if (rawData && Array.isArray(rawData.records)) {
      parkingLots.value = rawData.records
    } else {
      parkingLots.value = []
    }
    console.log(`[成功][阶段1][加载停车场] 时间：${Date.now()} | 参数：无 | 结果：${parkingLots.value.length}个停车场`)
  } catch (e) {
    console.log(`[失败][阶段1][加载停车场] 时间：${Date.now()} | 原因：${e.message} | 参数：无`)
    ElMessage.error('加载停车场列表失败')
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    console.log(`[失败][阶段2][上传校验] 时间：${Date.now()} | 原因：文件类型非图片 | 参数：${file.type}`)
  }
  return isImage
}

const handlePlateUpload = async (options) => {
  const file = options.file
  photoEntryForm.value.imageUrl = URL.createObjectURL(file)
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await apiClient.post('/vehicle-records/recognize', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200 && res.data.data && res.data.data.plateNumber) {
      photoEntryForm.value.plateNumber = res.data.data.plateNumber
      photoEntryForm.value.recognized = true
      console.log(`[成功][阶段2][车牌识别] 时间：${Date.now()} | 参数：${file.name} | 结果：${photoEntryForm.value.plateNumber}`)
      ElMessage.success(`识别成功：${photoEntryForm.value.plateNumber}`)
    } else {
      photoEntryForm.value.plateNumber = ''
      photoEntryForm.value.recognized = false
      console.log(`[失败][阶段2][车牌识别] 时间：${Date.now()} | 原因：${res.data.message || '识别无结果'} | 参数：${file.name}`)
      ElMessage.warning(res.data.message || '未能识别车牌号，请手动输入')
    }
  } catch (e) {
    photoEntryForm.value.recognized = false
    console.log(`[失败][阶段2][车牌识别] 时间：${Date.now()} | 原因：${e.message} | 参数：${file.name}`)
    ElMessage.error('车牌识别请求失败，请手动输入')
  }
}

const openEntryDialog = () => {
  photoEntryForm.value = {
    imageUrl: '',
    plateNumber: '',
    parkingLotId: null,
    recognized: false
  }
  manualEntryForm.value = {
    plateNumber: '',
    parkingLotId: null
  }
  entryTab.value = 'photo'
  entryDialogVisible.value = true
}

const submitEntry = async () => {
  if (entryTab.value === 'photo') {
    if (photoEntryFormRef.value) {
      await photoEntryFormRef.value.validate(async (valid) => {
        if (valid) {
          submitLoading.value = true
          try {
            const params = new URLSearchParams()
            params.append('parkingLotId', photoEntryForm.value.parkingLotId)
            params.append('plateNumber', photoEntryForm.value.plateNumber)
            if (photoEntryForm.value.imageUrl) {
              params.append('plateImageUrl', photoEntryForm.value.imageUrl)
            }
            const res = await apiClient.post('/vehicle-records/entry', params, {
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            })
            if (res.data.code === 200) {
              console.log(`[成功][阶段2][拍照入场] 时间：${Date.now()} | 参数：plateNumber=${photoEntryForm.value.plateNumber}, parkingLotId=${photoEntryForm.value.parkingLotId} | 结果：code=${res.data.code}`)
              ElMessage.success('车辆入场成功')
              entryDialogVisible.value = false
              loadRecords()
            } else {
              console.log(`[失败][阶段2][拍照入场] 时间：${Date.now()} | 原因：${res.data.message} | 参数：plateNumber=${photoEntryForm.value.plateNumber}`)
              ElMessage.error(res.data.message || '车辆入场失败')
            }
          } catch (e) {
            console.log(`[失败][阶段2][拍照入场] 时间：${Date.now()} | 原因：${e.message} | 参数：plateNumber=${photoEntryForm.value.plateNumber}`)
            ElMessage.error('车辆入场失败')
          } finally {
            submitLoading.value = false
          }
        }
      })
    }
  } else {
    if (manualEntryFormRef.value) {
      await manualEntryFormRef.value.validate(async (valid) => {
        if (valid) {
          submitLoading.value = true
          try {
            const params = new URLSearchParams()
            params.append('parkingLotId', manualEntryForm.value.parkingLotId)
            params.append('plateNumber', manualEntryForm.value.plateNumber)
            const res = await apiClient.post('/vehicle-records/entry', params, {
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            })
            if (res.data.code === 200) {
              console.log(`[成功][阶段2][手动入场] 时间：${Date.now()} | 参数：plateNumber=${manualEntryForm.value.plateNumber}, parkingLotId=${manualEntryForm.value.parkingLotId} | 结果：code=${res.data.code}`)
              ElMessage.success('车辆入场成功')
              entryDialogVisible.value = false
              loadRecords()
            } else {
              console.log(`[失败][阶段2][手动入场] 时间：${Date.now()} | 原因：${res.data.message} | 参数：plateNumber=${manualEntryForm.value.plateNumber}`)
              ElMessage.error(res.data.message || '车辆入场失败')
            }
          } catch (e) {
            console.log(`[失败][阶段2][手动入场] 时间：${Date.now()} | 原因：${e.message} | 参数：plateNumber=${manualEntryForm.value.plateNumber}`)
            ElMessage.error('车辆入场失败')
          } finally {
            submitLoading.value = false
          }
        }
      })
    }
  }
}

const handleExit = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认车辆 ${row.plateNumber} 出场？`,
      '确认出场',
      {
        confirmButtonText: '确认出场',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    try {
      const res = await apiClient.post(`/vehicle-records/${row.id}/exit`)
      console.log(`[成功][阶段2][车辆出场] 时间：${Date.now()} | 参数：id=${row.id} | 结果：${JSON.stringify(res.data)}`)
      ElMessage.success('车辆出场成功')
      loadRecords()
    } catch (e) {
      console.log(`[失败][阶段2][车辆出场] 时间：${Date.now()} | 原因：${e.message} | 参数：id=${row.id}`)
      ElMessage.error('车辆出场失败')
    }
  } catch {
  }
}

const onSystemMessage = (data) => {
  if (!data || !data.type) return
  const vehicleTypes = ['ORDER_CREATED', 'ORDER_COMPLETED', 'ORDER_CANCELLED', 'SPACE_UPDATE']
  if (vehicleTypes.includes(data.type)) {
    console.log(`[成功][阶段3][车辆记录WS推送] 时间：${Date.now()} | 类型：${data.type} | 结果：刷新车辆记录`)
    loadRecords()
  }
  if (data.type === 'ORDER_CREATED') {
    ElNotification({
      title: '车辆入场',
      message: `新车辆入场，订单 #${data.orderId || ''}`,
      type: 'info',
      duration: 3000,
      position: 'top-right'
    })
  } else if (data.type === 'ORDER_COMPLETED') {
    ElNotification({
      title: '车辆出场',
      message: `订单 #${data.orderId || ''} 已完成出场`,
      type: 'success',
      duration: 3000,
      position: 'top-right'
    })
  }
}

onMounted(() => {
  loadRecords()
  loadParkingLots()
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #f1f5f9;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.stat-card-blue::before {
  background: linear-gradient(90deg, #38bdf8 0%, #60a5fa 100%);
}

.stat-card-green::before {
  background: linear-gradient(90deg, #34d399 0%, #10b981 100%);
}

.stat-card-orange::before {
  background: linear-gradient(90deg, #fb923c 0%, #f59e0b 100%);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.08);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card-blue .stat-icon {
  background: #e0f2fe;
  color: #2563eb;
}

.stat-card-green .stat-icon {
  background: #d1fae5;
  color: #059669;
}

.stat-card-orange .stat-icon {
  background: #ffedd5;
  color: #ea580c;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px 0;
  line-height: 1.1;
}

.stat-label {
  color: #475569;
  font-size: 0.875rem;
  margin: 0;
  font-weight: 500;
}

.records-table {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(241, 245, 249, 0.9);
  backdrop-filter: blur(10px);
}

.records-table :deep(.el-table__body-wrapper table),
.records-table :deep(.el-table__header-wrapper table) {
  table-layout: fixed;
}

.records-table :deep(.el-table__header-wrapper th) {
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.95) 100%);
  font-weight: 600;
  color: #0f172a;
  border-bottom: 2px solid rgba(226, 232, 240, 0.9);
}

.records-table :deep(.el-table__body tr) {
  transition: all 0.2s ease;
}

.records-table :deep(.el-table__body tr:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.records-table :deep(.el-table__row--striped) {
  background: rgba(248, 250, 252, 0.6);
}

.records-table :deep(.el-table__row--striped:hover > td) {
  background: rgba(220, 252, 231, 0.4);
}

.records-table :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  box-shadow: 0 2px 8px -4px rgba(239, 68, 68, 0.4);
  transition: all 0.3s ease;
}

.records-table :deep(.el-button--danger:hover) {
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

.plate-uploader :deep(.el-upload) {
  border: 2px dashed rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(248, 250, 252, 0.8);
}

.plate-uploader :deep(.el-upload:hover) {
  border-color: #10b981;
  background: rgba(220, 252, 231, 0.3);
}

.plate-preview {
  width: 100%;
  height: 200px;
  object-fit: contain;
  display: block;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #94a3b8;
}

.upload-placeholder span {
  font-size: 0.875rem;
  font-weight: 500;
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

.page-container :deep(.el-tag--warning) {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
}

.page-container :deep(.el-tabs__item) {
  font-weight: 600;
  color: #64748b;
  transition: color 0.3s ease;
}

.page-container :deep(.el-tabs__item.is-active) {
  color: #10b981;
}

.page-container :deep(.el-tabs__active-bar) {
  background-color: #10b981;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }
}
</style>
