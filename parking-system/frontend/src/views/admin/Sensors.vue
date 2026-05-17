<template>
  <div class="sensor-management">
    <div class="page-header">
      <div class="header-title-section">
        <h1 class="page-title">传感器管理</h1>
        <p class="page-subtitle">管理停车场传感器设备，监控数据质量</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog" class="btn-primary">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          添加传感器
        </el-button>
        <el-button @click="loadSensors" class="btn-default">刷新</el-button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalSensors || 0 }}</span>
          <span class="stat-label">传感器总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.activeSensors || 0 }}</span>
          <span class="stat-label">在线设备</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon quality">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ (stats.avgQuality || 0).toFixed(1) }}%</span>
          <span class="stat-label">平均质量</span>
        </div>
      </div>
    </div>

    <div class="sensor-table-card">
      <el-table :data="sensors" style="width: 100%" v-loading="loading" class="sensor-table">
        <el-table-column prop="sensorCode" label="传感器编码" width="150" />
        <el-table-column prop="sensorName" label="名称" width="150" />
        <el-table-column prop="sensorType" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getSensorTypeTag(scope.row.sensorType)" class="type-tag">
              {{ getSensorTypeName(scope.row.sensorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parkingLotId" label="停车场" width="150">
          <template #default="scope">
            {{ getParkingLotName(scope.row.parkingLotId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" class="status-tag">
              {{ scope.row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataQuality" label="数据质量" width="150">
          <template #default="scope">
            <div class="quality-bar">
              <div class="quality-fill" :style="{ width: (scope.row.dataQuality || 0) + '%' }"></div>
              <span class="quality-text">{{ scope.row.dataQuality || 0 }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="最后更新" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.lastUpdateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewSensorData(scope.row)" class="table-action-btn">数据</el-button>
            <el-button size="small" type="primary" @click="editSensor(scope.row)" class="table-action-btn">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteSensor(scope.row)" class="table-action-btn">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑传感器' : '添加传感器'" width="560px" class="form-dialog">
      <el-form :model="formData" label-width="100px" class="sensor-form">
        <el-form-item label="传感器编码">
          <el-input v-model="formData.sensorCode" placeholder="请输入传感器编码" class="form-input" />
        </el-form-item>
        <el-form-item label="传感器名称">
          <el-input v-model="formData.sensorName" placeholder="请输入传感器名称" class="form-input" />
        </el-form-item>
        <el-form-item label="传感器类型">
          <el-select v-model="formData.sensorType" placeholder="请选择类型" class="form-select">
            <el-option label="地磁传感器" :value="1" />
            <el-option label="摄像头" :value="2" />
            <el-option label="超声波检测器" :value="3" />
            <el-option label="红外传感器" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属停车场">
          <el-select v-model="formData.parkingLotId" placeholder="请选择停车场" class="form-select">
            <el-option v-for="lot in parkingLots" :key="lot.id" :label="lot.name" :value="lot.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车位ID">
          <el-input v-model="formData.spaceId" placeholder="请输入车位ID" class="form-input" />
        </el-form-item>
        <el-form-item label="厂商">
          <el-input v-model="formData.manufacturer" placeholder="请输入厂商名称" class="form-input" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="formData.model" placeholder="请输入设备型号" class="form-input" />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="formData.ipAddress" placeholder="请输入IP地址" class="form-input" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="formData.port" placeholder="请输入端口号" class="form-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-default">取消</el-button>
          <el-button type="primary" @click="saveSensor" class="btn-primary">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialogVisible" title="传感器数据" width="800px" class="data-dialog">
      <div class="sensor-data-chart">
        <div class="chart-header">
          <span class="chart-title">数据趋势（最近1小时）</span>
        </div>
        <div class="data-list">
          <el-table :data="sensorDataList" style="width: 100%" max-height="400" class="data-table">
            <el-table-column prop="collectTime" label="采集时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.collectTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="rawValue" label="原始值" width="100" />
            <el-table-column prop="processedValue" label="处理值" width="100" />
            <el-table-column prop="dataQuality" label="质量" width="100">
              <template #default="scope">
                {{ scope.row.dataQuality }}%
              </template>
            </el-table-column>
            <el-table-column prop="isAnomaly" label="异常" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.isAnomaly === 1 ? 'danger' : 'success'" size="small" class="anomaly-tag">
                  {{ scope.row.isAnomaly === 1 ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="anomalyType" label="异常类型" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import apiClient from '../../utils/api'

const loading = ref(false)
const sensors = ref([])
const parkingLots = ref([])
const stats = ref({})
const dialogVisible = ref(false)
const dataDialogVisible = ref(false)
const isEdit = ref(false)
const formData = ref({})
const sensorDataList = ref([])

const sensorTypes = {
  1: '地磁传感器',
  2: '摄像头',
  3: '超声波检测器',
  4: '红外传感器'
}

const getSensorTypeName = (type) => sensorTypes[type] || '未知'
const getSensorTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return tags[type] || 'info'
}

const getParkingLotName = (id) => {
  const lot = parkingLots.value.find(l => l.id === id)
  return lot ? lot.name : '-'
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const loadSensors = async () => {
  loading.value = true
  try {
    const response = await apiClient.get('/sensors')
    if (response.data.code === 200) {
      sensors.value = Array.isArray(response.data.data) ? response.data.data : []
    }
  } catch (error) {
    console.error('加载传感器失败:', error)
    ElMessage.error('加载传感器失败')
  } finally {
    loading.value = false
  }
}

const loadParkingLots = async () => {
  try {
    const response = await apiClient.get('/parking-lots')
    if (response.data.code === 200) {
      parkingLots.value = Array.isArray(response.data.data) ? response.data.data : []
    }
  } catch (error) {
    console.error('加载停车场失败:', error)
  }
}

const loadStats = async () => {
  try {
    const response = await apiClient.get('/sensors/statistics')
    if (response.data.code === 200) {
      stats.value = response.data.data
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const showAddDialog = () => {
  isEdit.value = false
  formData.value = {
    sensorCode: '',
    sensorName: '',
    sensorType: 1,
    parkingLotId: null,
    status: 1
  }
  dialogVisible.value = true
}

const editSensor = (sensor) => {
  isEdit.value = true
  formData.value = { ...sensor }
  dialogVisible.value = true
}

const saveSensor = async () => {
  try {
    let response
    if (isEdit.value) {
      response = await apiClient.put(`/sensors/${formData.value.id}`, formData.value)
    } else {
      response = await apiClient.post('/sensors', formData.value)
    }
    
    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadSensors()
      loadStats()
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

const deleteSensor = async (sensor) => {
  try {
    await ElMessageBox.confirm('确定要删除这个传感器吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await apiClient.delete(`/sensors/${sensor.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadSensors()
      loadStats()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const viewSensorData = async (sensor) => {
  try {
    const endTime = Date.now()
    const startTime = endTime - 3600000
    const response = await apiClient.get(`/sensors/${sensor.id}/data`, {
      params: { startTime, endTime }
    })
    
    if (response.data.code === 200) {
      sensorDataList.value = Array.isArray(response.data.data) ? response.data.data : []
      dataDialogVisible.value = true
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadSensors()
  loadParkingLots()
  loadStats()
})
</script>

<style scoped>
.sensor-management {
  padding: 24px;
  background: #f8fafc;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-title {
  font-size: 1.625rem;
  font-weight: 700;
  margin: 0;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 0.9375rem;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-primary {
  border-radius: 12px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  padding: 0 20px;
  height: 40px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px -6px rgba(16, 185, 129, 0.4);
}

.btn-default {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 0 20px;
  height: 40px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-default:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px -8px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.total {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #10b981;
}

.stat-icon.active {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #10b981;
}

.stat-icon.quality {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #f59e0b;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

.sensor-table-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 12px -6px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
}

.sensor-table {
  border-radius: 12px;
}

.type-tag {
  font-weight: 500;
  border-radius: 8px;
  padding: 4px 10px;
}

.status-tag {
  font-weight: 500;
  border-radius: 8px;
  padding: 4px 10px;
}

.table-action-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.table-action-btn:hover {
  transform: translateY(-1px);
}

.quality-bar {
  position: relative;
  width: 100%;
  height: 20px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.quality-fill {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #059669);
  border-radius: 10px;
  transition: width 0.6s ease;
}

.quality-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.75rem;
  font-weight: 600;
  color: #374151;
}

.form-dialog :deep(.el-dialog),
.data-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

.form-dialog :deep(.el-dialog__header),
.data-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f0fdf4 0%, #d1fae5 100%);
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid #dcfce7;
}

.form-dialog :deep(.el-dialog__title),
.data-dialog :deep(.el-dialog__title) {
  font-size: 1.125rem;
  font-weight: 600;
  color: #065f46;
}

.sensor-form {
  padding: 8px 0;
}

.form-input :deep(.el-input__wrapper),
.form-select :deep(.el-select__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s ease;
}

.form-input :deep(.el-input__wrapper:hover),
.form-select :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

.form-input :deep(.el-input__wrapper.is-focus),
.form-select :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px #10b981 inset;
}

.sensor-data-chart {
  padding: 8px 0;
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 1rem;
  font-weight: 600;
  color: #0f172a;
}

.data-table {
  border-radius: 12px;
}

.anomaly-tag {
  font-weight: 500;
  border-radius: 6px;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
