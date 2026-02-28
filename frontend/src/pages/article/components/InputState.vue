<template>
  <div class="input-state">
    <div class="input-card">
      <div class="input-header">
        <h1 class="input-title">Create a New Article</h1>
        <p class="input-subtitle">Enter your topic and let AI generate a viral article for you</p>
      </div>

      <div class="input-area">
        <a-textarea
          :value="topic"
          @update:value="$emit('update:topic', $event)"
          placeholder="Enter the topic you want to write about, e.g.: How AI will reshape the workplace in 2026"
          :rows="6"
          :maxlength="MAX_TOPIC_LENGTH"
          show-count
          class="topic-textarea"
        />
        <a-button
          type="primary"
          size="large"
          :loading="loading"
          :disabled="!topic.trim()"
          @click="$emit('start')"
          class="create-btn"
        >
          <template #icon>
            <RocketOutlined />
          </template>
          Start Writing
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RocketOutlined } from '@ant-design/icons-vue'
import { MAX_TOPIC_LENGTH } from '@/constants/article'

defineProps<{
  topic: string
  loading?: boolean
}>()

defineEmits<{
  (e: 'update:topic', value: string): void
  (e: 'start'): void
}>()
</script>

<style scoped lang="scss">
.input-state {
  max-width: 700px;
  margin: 0 auto;
  padding-top: 60px;
}

.input-card {
  background: var(--color-background-secondary);
  border-radius: var(--radius-xl);
  padding: 40px;
}

.input-header {
  text-align: center;
  margin-bottom: 32px;
}

.input-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--color-text);
}

.input-subtitle {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 0;
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.topic-textarea {
  font-size: 15px;
  border-radius: var(--radius-lg);
  padding: 16px;
  background: white;

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
  }
}

.create-btn.ant-btn {
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary) !important;
  border: none !important;
  color: white !important;
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.3) !important;

  &:hover,
  &:focus,
  &:active {
    background: var(--gradient-primary) !important;
    color: white !important;
    border: none !important;
    box-shadow: 0 4px 14px rgba(249, 115, 22, 0.3) !important;
    opacity: 0.92;
  }

  &:disabled,
  &.ant-btn-disabled {
    background: var(--color-border) !important;
    box-shadow: none !important;
    opacity: 0.6;
    color: var(--color-text-muted) !important;
  }
}
</style>
