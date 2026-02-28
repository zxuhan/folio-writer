import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export interface BackgroundTask {
  id: string
  label: string
  detail?: string
  progress?: number
  route?: string
}

/**
 * Tracks long-running backend tasks (e.g. SSE-driven article generation) so the UI
 * can show a persistent notice across page navigation and prompt before tab close.
 */
export const useBackgroundTaskStore = defineStore('backgroundTask', () => {
  const tasks = ref(new Map<string, BackgroundTask>())

  const hasActive = computed(() => tasks.value.size > 0)
  const activeList = computed(() => Array.from(tasks.value.values()))

  function startTask(task: BackgroundTask) {
    tasks.value.set(task.id, { ...task })
  }

  function updateTask(id: string, patch: Partial<BackgroundTask>) {
    const existing = tasks.value.get(id)
    if (!existing) return
    tasks.value.set(id, { ...existing, ...patch })
  }

  function finishTask(id: string) {
    tasks.value.delete(id)
  }

  return { tasks, hasActive, activeList, startTask, updateTask, finishTask }
})
