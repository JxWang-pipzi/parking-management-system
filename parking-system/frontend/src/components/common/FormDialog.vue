<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    :width="width"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :before-close="handleClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleDialogClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-width="labelWidth"
      :label-position="labelPosition"
      :disabled="loading"
    >
      <slot name="form" :form-data="formData" />
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" :disabled="loading">
          {{ cancelText }}
        </el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :loading="loading"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  // 是否显示对话框
  modelValue: {
    type: Boolean,
    default: false
  },
  // 对话框标题
  title: {
    type: String,
    default: '表单'
  },
  // 对话框宽度
  width: {
    type: String,
    default: '600px'
  },
  // 表单数据
  formData: {
    type: Object,
    required: true
  },
  // 表单验证规则
  rules: {
    type: Object,
    default: () => ({})
  },
  // 标签宽度
  labelWidth: {
    type: String,
    default: '100px'
  },
  // 标签位置
  labelPosition: {
    type: String,
    default: 'right'
  },
  // 确认按钮文字
  confirmText: {
    type: String,
    default: '确定'
  },
  // 取消按钮文字
  cancelText: {
    type: String,
    default: '取消'
  },
  // 点击遮罩层是否关闭
  closeOnClickModal: {
    type: Boolean,
    default: false
  },
  // 按下ESC是否关闭
  closeOnPressEscape: {
    type: Boolean,
    default: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'confirm',
  'cancel',
  'open',
  'opened',
  'close'
])

// 表单引用
const formRef = ref(null)

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 打开对话框
const handleOpen = () => {
  emit('open')
}

// 对话框打开后
const handleOpened = () => {
  emit('opened')
}

// 关闭对话框
const handleClose = (done) => {
  if (props.loading) {
    ElMessage.warning('操作进行中，请稍候...')
    return
  }
  done()
}

// 对话框关闭后
const handleDialogClose = () => {
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('close')
}

// 取消
const handleCancel = () => {
  if (props.loading) {
    ElMessage.warning('操作进行中，请稍候...')
    return
  }
  dialogVisible.value = false
  emit('cancel')
}

// 确认
const handleConfirm = async () => {
  if (!formRef.value) return
  
  try {
    // 验证表单
    await formRef.value.validate()
    // 触发确认事件
    emit('confirm', props.formData)
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('请检查表单填写是否正确')
  }
}

// 暴露方法
defineExpose({
  // 验证表单
  validate: () => formRef.value?.validate(),
  // 重置表单
  resetFields: () => formRef.value?.resetFields(),
  // 清空验证
  clearValidate: () => formRef.value?.clearValidate()
})
</script>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
