<template>
  <el-tag
    :type="tagType"
    :effect="effect"
    :size="size"
    :round="round"
    :closable="closable"
    @close="handleClose"
  >
    <el-icon v-if="showIcon" class="status-icon">
      <component :is="iconComponent" />
    </el-icon>
    {{ displayText }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import {
  CircleCheck,
  CircleClose,
  Warning,
  Clock,
  InfoFilled,
  QuestionFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  // 状态值
  status: {
    type: [String, Number],
    required: true
  },
  // 状态映射配置
  statusMap: {
    type: Object,
    default: () => ({})
  },
  // 标签尺寸
  size: {
    type: String,
    default: 'default'
  },
  // 标签效果
  effect: {
    type: String,
    default: 'light'
  },
  // 是否圆角
  round: {
    type: Boolean,
    default: false
  },
  // 是否可关闭
  closable: {
    type: Boolean,
    default: false
  },
  // 是否显示图标
  showIcon: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

// 默认状态映射
const defaultStatusMap = {
  // 通用状态
  0: { text: '禁用', type: 'info', icon: 'CircleClose' },
  1: { text: '启用', type: 'success', icon: 'CircleCheck' },
  
  // 订单状态
  pending: { text: '待支付', type: 'warning', icon: 'Clock' },
  paid: { text: '已支付', type: 'success', icon: 'CircleCheck' },
  completed: { text: '已完成', type: 'success', icon: 'CircleCheck' },
  cancelled: { text: '已取消', type: 'info', icon: 'CircleClose' },
  
  // 预约状态
  reserved: { text: '已预约', type: 'warning', icon: 'Clock' },
  confirmed: { text: '已确认', type: 'success', icon: 'CircleCheck' },
  arrived: { text: '已到达', type: 'primary', icon: 'InfoFilled' },
  expired: { text: '已过期', type: 'info', icon: 'CircleClose' },
  
  // 支付状态
  unpaid: { text: '未支付', type: 'warning', icon: 'Clock' },
  paying: { text: '支付中', type: 'primary', icon: 'Clock' },
  success: { text: '支付成功', type: 'success', icon: 'CircleCheck' },
  failed: { text: '支付失败', type: 'danger', icon: 'CircleClose' },
  refunded: { text: '已退款', type: 'info', icon: 'Warning' },
  
  // 车位状态
  available: { text: '空闲', type: 'success', icon: 'CircleCheck' },
  occupied: { text: '占用', type: 'danger', icon: 'CircleClose' },
  
  // 传感器状态
  online: { text: '在线', type: 'success', icon: 'CircleCheck' },
  offline: { text: '离线', type: 'info', icon: 'CircleClose' },
  error: { text: '故障', type: 'danger', icon: 'Warning' }
}

// 合并状态映射
const mergedStatusMap = computed(() => ({
  ...defaultStatusMap,
  ...props.statusMap
}))

// 当前状态配置
const currentStatus = computed(() => {
  return mergedStatusMap.value[props.status] || {
    text: String(props.status),
    type: 'info',
    icon: 'QuestionFilled'
  }
})

// 标签类型
const tagType = computed(() => currentStatus.value.type)

// 显示文本
const displayText = computed(() => currentStatus.value.text)

// 图标组件
const iconComponent = computed(() => {
  const iconName = currentStatus.value.icon
  const iconMap = {
    CircleCheck,
    CircleClose,
    Warning,
    Clock,
    InfoFilled,
    QuestionFilled
  }
  return iconMap[iconName] || QuestionFilled
})

// 关闭事件
const handleClose = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
.status-icon {
  margin-right: 4px;
  vertical-align: middle;
}
</style>
