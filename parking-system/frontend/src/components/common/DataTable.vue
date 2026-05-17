<template>
  <div class="data-table">
    <el-table
      :data="tableData"
      :border="border"
      :stripe="stripe"
      :loading="loading"
      :height="height"
      :max-height="maxHeight"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      v-bind="$attrs"
    >
      <!-- 多选列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        align="center"
      />
      
      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="60"
        align="center"
        :index="indexMethod"
      />
      
      <!-- 数据列 -->
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :align="column.align || 'left'"
        :sortable="column.sortable"
        :fixed="column.fixed"
        :show-overflow-tooltip="column.showOverflowTooltip !== false"
      >
        <template #default="scope">
          <slot
            v-if="column.slot"
            :name="column.slot"
            :row="scope.row"
            :column="column"
            :$index="scope.$index"
          />
          <span v-else>{{ scope.row[column.prop] }}</span>
        </template>
      </el-table-column>
      
      <!-- 操作列 -->
      <el-table-column
        v-if="showActions"
        label="操作"
        :width="actionsWidth"
        :fixed="actionsFixed"
        align="center"
      >
        <template #default="scope">
          <slot name="actions" :row="scope.row" :$index="scope.$index" />
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div v-if="showPagination" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        :layout="paginationLayout"
        :background="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 列配置
  columns: {
    type: Array,
    required: true
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: true
  },
  // 是否显示斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 表格高度
  height: {
    type: [String, Number],
    default: undefined
  },
  // 表格最大高度
  maxHeight: {
    type: [String, Number],
    default: undefined
  },
  // 是否显示多选
  showSelection: {
    type: Boolean,
    default: false
  },
  // 是否显示序号
  showIndex: {
    type: Boolean,
    default: false
  },
  // 是否显示操作列
  showActions: {
    type: Boolean,
    default: false
  },
  // 操作列宽度
  actionsWidth: {
    type: [String, Number],
    default: 180
  },
  // 操作列固定
  actionsFixed: {
    type: String,
    default: 'right'
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 当前页码
  page: {
    type: Number,
    default: 1
  },
  // 每页条数
  size: {
    type: Number,
    default: 10
  },
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  // 每页条数选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 分页布局
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  }
})

const emit = defineEmits([
  'selection-change',
  'sort-change',
  'page-change',
  'size-change'
])

// 表格数据
const tableData = computed(() => props.data)

// 当前页码
const currentPage = ref(props.page)
// 每页条数
const pageSize = ref(props.size)

// 监听外部页码变化
watch(() => props.page, (val) => {
  currentPage.value = val
})

watch(() => props.size, (val) => {
  pageSize.value = val
})

// 序号计算
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 多选变化
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

// 排序变化
const handleSortChange = ({ column, prop, order }) => {
  emit('sort-change', { column, prop, order })
}

// 每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  emit('size-change', val)
  emit('page-change', { page: currentPage.value, size: val })
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  emit('page-change', { page: val, size: pageSize.value })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.data-table {
  width: 100%;
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-md;
  }
}
</style>
