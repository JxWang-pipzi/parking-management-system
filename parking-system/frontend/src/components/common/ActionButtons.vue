<template>
  <div class="action-buttons">
    <el-button
      v-for="button in visibleButtons"
      :key="button.key"
      :type="button.type || 'default'"
      :size="button.size || size"
      :icon="button.icon"
      :disabled="button.disabled"
      :loading="button.loading"
      :plain="button.plain"
      :round="button.round"
      :circle="button.circle"
      :link="button.link"
      @click="handleClick(button)"
    >
      {{ button.label }}
    </el-button>
    
    <!-- 更多操作下拉菜单 -->
    <el-dropdown
      v-if="moreButtons.length > 0"
      :size="size"
      @command="handleCommand"
    >
      <el-button :size="size">
        更多操作
        <el-icon class="el-icon--right">
          <arrow-down />
        </el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="button in moreButtons"
            :key="button.key"
            :command="button.key"
            :disabled="button.disabled"
            :divided="button.divided"
          >
            <el-icon v-if="button.icon">
              <component :is="button.icon" />
            </el-icon>
            {{ button.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  // 按钮配置列表
  buttons: {
    type: Array,
    default: () => []
  },
  // 按钮尺寸
  size: {
    type: String,
    default: 'default'
  },
  // 最多显示按钮数量，超出的放入更多操作
  maxVisible: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['click'])

// 可见按钮
const visibleButtons = computed(() => {
  return props.buttons
    .filter(btn => !btn.hidden)
    .slice(0, props.maxVisible)
})

// 更多按钮
const moreButtons = computed(() => {
  return props.buttons
    .filter(btn => !btn.hidden)
    .slice(props.maxVisible)
})

// 按钮点击
const handleClick = (button) => {
  if (button.disabled || button.loading) return
  emit('click', button.key, button)
}

// 下拉菜单选择
const handleCommand = (command) => {
  const button = props.buttons.find(btn => btn.key === command)
  if (button && !button.disabled) {
    emit('click', command, button)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.action-buttons {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}
</style>
