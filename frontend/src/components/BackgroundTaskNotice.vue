<template>
  <Transition name="task-notice">
    <div v-if="store.hasActive" class="task-notice">
      <div
        v-for="task in store.activeList"
        :key="task.id"
        class="task-row"
        :class="{ clickable: !!task.route }"
        @click="handleClick(task)"
      >
        <a-spin size="small" />
        <div class="task-info">
          <div class="task-label">{{ task.label }}</div>
          <div v-if="task.detail" class="task-detail">{{ task.detail }}</div>
          <a-progress
            v-if="typeof task.progress === 'number'"
            :percent="task.progress"
            size="small"
            :show-info="false"
            class="task-progress"
          />
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBackgroundTaskStore, type BackgroundTask } from '@/stores/backgroundTask'

const store = useBackgroundTaskStore()
const router = useRouter()

const handleClick = (task: BackgroundTask) => {
  if (task.route) {
    router.push(task.route)
  }
}

const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (store.hasActive) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onMounted(() => window.addEventListener('beforeunload', handleBeforeUnload))
onBeforeUnmount(() => window.removeEventListener('beforeunload', handleBeforeUnload))
</script>

<style scoped>
.task-notice {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
  width: 300px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 4px 0;
}

.task-row.clickable {
  cursor: pointer;
}

.task-row.clickable:hover .task-label {
  color: var(--color-primary);
}

.task-info {
  flex: 1;
  min-width: 0;
}

.task-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  transition: color var(--transition-fast);
}

.task-detail {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-progress {
  margin-top: 6px;
}

.task-notice-enter-active,
.task-notice-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.task-notice-enter-from,
.task-notice-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
