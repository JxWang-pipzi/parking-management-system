<template>
  <div class="search-box">
    <el-form
      :inline="true"
      :model="searchForm"
      @submit.prevent="handleSearch"
    >
      <el-form-item
        v-for="field in fields"
        :key="field.prop"
        :label="field.label"
      >
        <!-- 输入框 -->
        <el-input
          v-if="field.type === 'input' || !field.type"
          v-model="searchForm[field.prop]"
          :placeholder="field.placeholder || `请输入${field.label}`"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '200px' }"
          @clear="handleClear"
        />
        
        <!-- 选择器 -->
        <el-select
          v-else-if="field.type === 'select'"
          v-model="searchForm[field.prop]"
          :placeholder="field.placeholder || `请选择${field.label}`"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '200px' }"
          @clear="handleClear"
        >
          <el-option
            v-for="option in field.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        
        <!-- 日期选择器 -->
        <el-date-picker
          v-else-if="field.type === 'date'"
          v-model="searchForm[field.prop]"
          :type="field.dateType || 'date'"
          :placeholder="field.placeholder || `请选择${field.label}`"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '200px' }"
          :value-format="field.valueFormat || 'YYYY-MM-DD'"
          @clear="handleClear"
        />
        
        <!-- 日期范围选择器 -->
        <el-date-picker
          v-else-if="field.type === 'daterange'"
          v-model="searchForm[field.prop]"
          type="daterange"
          :range-separator="field.rangeSeparator || '至'"
          :start-placeholder="field.startPlaceholder || '开始日期'"
          :end-placeholder="field.endPlaceholder || '结束日期'"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '240px' }"
          :value-format="field.valueFormat || 'YYYY-MM-DD'"
          @clear="handleClear"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button
          type="primary"
          :icon="Search"
          @click="handleSearch"
        >
          搜索
        </el-button>
        <el-button
          :icon="Refresh"
          @click="handleReset"
        >
          重置
        </el-button>
        <el-button
          v-if="showExpand && fields.length > expandThreshold"
          link
          @click="expanded = !expanded"
        >
          {{ expanded ? '收起' : '展开' }}
          <el-icon>
            <arrow-up v-if="expanded" />
            <arrow-down v-else />
          </el-icon>
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { Search, Refresh, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  // 搜索字段配置
  fields: {
    type: Array,
    required: true
  },
  // 初始搜索值
  modelValue: {
    type: Object,
    default: () => ({})
  },
  // 是否显示展开/收起
  showExpand: {
    type: Boolean,
    default: false
  },
  // 展开阈值
  expandThreshold: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

// 搜索表单
const searchForm = reactive({})

// 是否展开
const expanded = ref(false)

// 初始化搜索表单
const initSearchForm = () => {
  props.fields.forEach(field => {
    searchForm[field.prop] = props.modelValue[field.prop] || field.defaultValue || ''
  })
}

// 监听字段变化
watch(() => props.fields, () => {
  initSearchForm()
}, { immediate: true })

// 监听外部值变化
watch(() => props.modelValue, (val) => {
  Object.assign(searchForm, val)
}, { deep: true })

// 搜索
const handleSearch = () => {
  emit('update:modelValue', { ...searchForm })
  emit('search', { ...searchForm })
}

// 重置
const handleReset = () => {
  props.fields.forEach(field => {
    searchForm[field.prop] = field.defaultValue || ''
  })
  emit('update:modelValue', { ...searchForm })
  emit('reset')
}

// 清空
const handleClear = () => {
  // 延迟触发搜索，确保清空操作完成
  setTimeout(() => {
    handleSearch()
  }, 100)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.search-box {
  background: $bg-color;
  padding: $spacing-md;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-md;
  
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}
</style>
