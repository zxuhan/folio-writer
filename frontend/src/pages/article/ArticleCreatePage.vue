<template>
  <div class="article-create-page">
    <div class="create-layout">
      <aside class="sidebar-left">
        <div class="sidebar-header">
          <h3 class="sidebar-title">Creation Pipeline</h3>
          <p class="sidebar-subtitle">Agent collaboration visualization</p>
        </div>

        <div class="flow-timeline">
          <div
            v-for="(step, index) in agentSteps"
            :key="index"
            :class="['flow-item', {
              'active': currentStep === index,
              'completed': currentStep > index,
              'pending': currentStep < index
            }]"
          >
            <div class="flow-indicator">
              <LoadingOutlined v-if="currentStep === index && isCreating" class="spin-icon" />
              <CheckCircleOutlined v-else-if="currentStep > index" />
              <span v-else class="step-number">{{ index + 1 }}</span>
            </div>
            <div class="flow-content">
              <div class="flow-title">{{ step.title }}</div>
              <div class="flow-desc">{{ step.description }}</div>
              <div v-if="currentStep === index && isCreating" class="flow-status">
                <span class="status-dot"></span>
                Running...
              </div>
            </div>
          </div>
        </div>

      </aside>

      <main ref="mainContentRef" class="main-content">
        <Transition name="fade-slide" mode="out-in">
          <div v-if="currentPhase === 'INPUT'" key="input" class="input-state">
          <div class="input-card">
            <div class="input-header">
              <h1 class="input-title">Create a New Article</h1>
              <p class="input-subtitle">Enter your topic and let AI generate a viral article for you</p>
            </div>

            <div class="input-area">
              <a-textarea
                v-model:value="topic"
                placeholder="Enter the topic you want to write about, e.g.: How AI will reshape the workplace in 2026"
                :rows="6"
                :maxlength="500"
                show-count
                class="topic-textarea"
              />

              <div class="style-section">
                <div class="section-header">
                  <span class="section-title">Article Style</span>
                  <span class="section-tip">(leave blank to use the default style)</span>
                </div>
                <a-radio-group v-model:value="selectedStyle" class="style-group">
                  <a-radio value="">Default</a-radio>
                  <a-radio value="tech">Tech</a-radio>
                  <a-radio value="emotional">Emotional</a-radio>
                  <a-radio value="educational">Educational</a-radio>
                  <a-radio value="humorous">Light & Humorous</a-radio>
                </a-radio-group>
              </div>

              <div class="image-methods-section">
                <div class="section-header">
                  <span class="section-title">Image Sources</span>
                  <span class="section-tip">(leave blank to enable all sources)</span>
                </div>
                <a-checkbox-group v-model:value="selectedImageMethods" class="methods-group">
                  <a-checkbox value="PEXELS">Pexels</a-checkbox>
                  <a-tooltip :title="isVip ? '' : 'VIP members only'">
                    <a-checkbox value="NANO_BANANA" :disabled="!isVip">
                      Nano Banana
                      <CrownOutlined v-if="!isVip" class="vip-icon" />
                    </a-checkbox>
                  </a-tooltip>
                  <a-checkbox value="MERMAID">Mermaid</a-checkbox>
                  <a-checkbox value="ICONIFY">Iconify</a-checkbox>
                  <a-checkbox value="EMOJI_PACK">Emoji Pack</a-checkbox>
                  <a-tooltip :title="isVip ? '' : 'VIP members only'">
                    <a-checkbox value="SVG_DIAGRAM" :disabled="!isVip">
                      SVG
                      <CrownOutlined v-if="!isVip" class="vip-icon" />
                    </a-checkbox>
                  </a-tooltip>
                </a-checkbox-group>
                <div v-if="!isVip" class="vip-notice">
                  <CrownOutlined />
                  <span>AI image generation and SVG diagrams are VIP-only features.</span>
                  <RouterLink to="/vip" class="upgrade-link">Upgrade now</RouterLink>
                </div>
              </div>

              <a-button
                type="primary"
                size="large"
                :loading="isCreating"
                :disabled="!topic.trim() || !hasQuota"
                @click="startCreate"
                class="create-btn"
              >
                <template #icon>
                  <RocketOutlined />
                </template>
                Start Writing
              </a-button>
              <div v-if="!hasQuota" class="quota-warning">
                <WarningOutlined />
                <span>Quota exhausted, you cannot create new articles</span>
              </div>
            </div>
          </div>
          </div>

          <div v-else-if="currentPhase === 'TITLE_GENERATING'" key="title-generating" class="loading-stage">
            <a-spin size="large" />
            <h3>AI is generating title options...</h3>
            <p>Please wait a moment while we craft a few great headlines</p>
          </div>

          <TitleSelectingStage
            v-else-if="currentPhase === 'TITLE_SELECTING'"
            key="title-selecting"
            :title-options="titleOptions"
            :loading="confirmLoading"
            @confirm="handleConfirmTitle"
          />

          <div v-else-if="currentPhase === 'OUTLINE_GENERATING'" key="outline-generating" class="outline-generating-state">
            <div v-if="article.mainTitle" class="preview-header">
              <h1 class="article-title">{{ article.mainTitle }}</h1>
              <p class="article-subtitle">{{ article.subTitle }}</p>
            </div>

            <div class="outline-preview">
              <div class="section-label">
                <BulbOutlined />
                <span>AI is planning the article outline</span>
                <span class="typing-cursor">|</span>
              </div>
              <div v-if="parsedOutline.length > 0" class="outline-list">
                <div
                  v-for="item in parsedOutline"
                  :key="item.section"
                  class="outline-item fade-in"
                >
                  <div class="outline-title">{{ item.section }}. {{ item.title }}</div>
                  <ul class="outline-points">
                    <li v-for="(point, idx) in item.points" :key="idx">{{ point }}</li>
                  </ul>
                </div>
              </div>
              <div v-else class="outline-loading">
                <a-spin />
                <span>Building article structure...</span>
              </div>
            </div>
          </div>

          <OutlineEditingStage
            v-else-if="currentPhase === 'OUTLINE_EDITING'"
            key="outline-editing"
            :outline="outline"
            :loading="confirmLoading"
            :task-id="taskId"
            @confirm="handleConfirmOutline"
          />

          <div v-else-if="currentPhase === 'CONTENT_GENERATING'" key="content-generating" class="creating-state">
          <div v-if="article.mainTitle" class="preview-header">
            <h1 class="article-title">{{ article.mainTitle }}</h1>
            <p class="article-subtitle">{{ article.subTitle }}</p>
          </div>

          <div v-if="outlineRaw" class="outline-preview">
            <div class="section-label">
              <BulbOutlined />
              <span>Article Outline</span>
              <span v-if="isOutlineStreaming" class="typing-cursor">|</span>
            </div>
            <div class="outline-list">
              <div
                v-for="item in parsedOutline"
                :key="item.section"
                class="outline-item"
              >
                <div class="outline-title">{{ item.section }}. {{ item.title }}</div>
                <ul class="outline-points">
                  <li v-for="(point, idx) in item.points" :key="idx">{{ point }}</li>
                </ul>
              </div>
            </div>
          </div>

          <div v-if="article.content" class="content-preview">
            <div v-html="markdownToHtml(article.content)" class="markdown-body"></div>
            <span v-if="isStreaming" class="typing-cursor">|</span>
          </div>

          <div v-if="currentStep === 3" class="image-progress-box">
            <div class="progress-header">
              <LoadingOutlined class="spin-icon" />
              <span>Analyzing image requirements</span>
            </div>
            <p class="progress-hint">Working out which images this article needs…</p>
          </div>

          <div v-if="currentStep === 4" class="image-progress-box">
            <div class="progress-header">
              <PictureOutlined />
              <span>Generating images</span>
            </div>
            <a-progress
              :percent="imageProgress"
              :status="imageProgress >= 100 ? 'success' : 'active'"
              :stroke-color="{ from: '#F97316', to: '#EA580C' }"
            />
            <p class="progress-hint">
              {{ imageCount }}/{{ totalImages || '?' }} images ready · runs in the background, feel free to wait
            </p>
          </div>

          <div v-if="currentStep === 5" class="image-progress-box">
            <div class="progress-header">
              <LoadingOutlined class="spin-icon" />
              <span>Composing final article</span>
            </div>
            <p class="progress-hint">Stitching the content and images together…</p>
          </div>

          <div v-if="currentStep === 0 && !article.mainTitle" class="loading-placeholder">
            <a-spin size="large" />
            <p>AI is brainstorming titles...</p>
          </div>
          </div>

          <div v-else-if="currentPhase === 'COMPLETED'" key="completed" class="completed-state">
          <div class="success-header">
            <CheckCircleFilled class="success-icon" />
            <span>Article creation complete!</span>
          </div>

          <div class="preview-header">
            <h1 class="article-title">{{ article.mainTitle }}</h1>
            <p class="article-subtitle">{{ article.subTitle }}</p>
          </div>
          <div class="content-preview">
            <div v-html="markdownToHtml(article.fullContent || article.content || '')" class="markdown-body"></div>
          </div>
          </div>
        </Transition>
      </main>

      <aside class="sidebar-right">
        <div v-if="currentPhase === 'INPUT'" class="panel-section quota-section">
          <h4 class="panel-title">
            <CrownOutlined />
            Creation Quota
          </h4>
          <div v-if="isAdmin" class="quota-admin">
            <span class="quota-badge admin">Admin</span>
            <span class="quota-text">Unlimited</span>
          </div>
          <div v-else-if="isVip" class="quota-admin">
            <span class="quota-badge vip">VIP Member</span>
            <span class="quota-text">Unlimited</span>
          </div>
          <div v-else class="quota-info">
            <div class="quota-display">
              <span class="quota-number" :class="{ 'low': quota <= 1, 'empty': quota === 0 }">{{ quota }}</span>
              <span class="quota-unit">left</span>
            </div>
            <div class="quota-label">Remaining quota</div>
            <a-progress
              :percent="(quota / 5) * 100"
              :show-info="false"
              :stroke-color="quota <= 1 ? '#ff4d4f' : '#F97316'"
              size="small"
              class="quota-progress"
            />
          </div>
        </div>

        <div v-if="currentPhase === 'INPUT'" class="panel-section">
          <h4 class="panel-title">
            <BulbOutlined />
            Trending Topics
          </h4>
          <div class="hot-tags">
            <span
              v-for="example in exampleTopics"
              :key="example"
              class="hot-tag"
              @click="topic = example"
            >
              {{ example }}
            </span>
          </div>
        </div>

        <div v-if="currentPhase === 'INPUT'" class="panel-section">
          <h4 class="panel-title">
            <StarOutlined />
            Viral Tips
          </h4>
          <div class="tips-list">
            <div class="tip-item">
              <div class="tip-icon">1</div>
              <div class="tip-content">
                <div class="tip-title">Hit a pain point</div>
                <div class="tip-desc">Address what your readers care about most</div>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">2</div>
              <div class="tip-content">
                <div class="tip-title">Build suspense</div>
                <div class="tip-desc">Spark the reader's curiosity</div>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">3</div>
              <div class="tip-content">
                <div class="tip-title">Use numbers</div>
                <div class="tip-desc">Concrete data adds credibility</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="isCreating || currentPhase === 'TITLE_SELECTING' || currentPhase === 'OUTLINE_EDITING'" class="panel-section">
          <h4 class="panel-title">
            <ClockCircleOutlined />
            Progress
          </h4>
          <div class="progress-info">
            <div class="progress-step">
              <span class="step-label">Current step</span>
              <span class="step-value">{{ agentSteps[currentStep]?.title }}</span>
            </div>
            <div class="progress-step">
              <span class="step-label">Completed</span>
              <span class="step-value">{{ currentStep }}/{{ agentSteps.length }}</span>
            </div>
          </div>
          <div v-if="isCreating" class="progress-tip">
            <InfoCircleOutlined />
            <span>AI is hard at work, please be patient...</span>
          </div>
          <div v-else class="progress-tip waiting">
            <InfoCircleOutlined />
            <span>Waiting for your confirmation...</span>
          </div>
        </div>

        <div v-if="realtimeLogs.length > 0" class="panel-section realtime-logs-section">
          <h4 class="panel-title">
            <FileTextOutlined />
            Execution Log
          </h4>
          <div class="logs-container">
            <div
              v-for="(log, index) in realtimeLogs"
              :key="index"
              :class="['log-entry', log.level]"
            >
              <span class="log-time">{{ formatLogTime(log.timestamp) }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </div>

        <div v-if="currentPhase !== 'INPUT' && currentPhase !== 'COMPLETED' && topic" class="panel-section">
          <h4 class="panel-title">
            <BulbOutlined />
            Topic
          </h4>
          <div class="topic-display">
            <p>{{ topic }}</p>
          </div>
        </div>

        <div v-if="currentPhase === 'TITLE_GENERATING'" class="panel-section tips-section">
          <h4 class="panel-title">
            <StarOutlined />
            Tip
          </h4>
          <div class="tips-list">
            <div class="tip-item">
              <div class="tip-icon">💡</div>
              <div class="tip-content">
                <div class="tip-desc">AI is analyzing your topic and generating multiple eye-catching title options</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentPhase === 'TITLE_SELECTING'" class="panel-section tips-section">
          <h4 class="panel-title">
            <StarOutlined />
            Tip
          </h4>
          <div class="tips-list">
            <div class="tip-item">
              <div class="tip-icon">✅</div>
              <div class="tip-content">
                <div class="tip-desc">Pick the title that fits your vision best, or add notes so the AI can match your needs more closely</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentPhase === 'OUTLINE_GENERATING'" class="panel-section tips-section">
          <h4 class="panel-title">
            <StarOutlined />
            Tip
          </h4>
          <div class="tips-list">
            <div class="tip-item">
              <div class="tip-icon">📝</div>
              <div class="tip-content">
                <div class="tip-desc">AI is planning the article structure and laying out clear sections</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentPhase === 'OUTLINE_EDITING'" class="panel-section tips-section">
          <h4 class="panel-title">
            <StarOutlined />
            Editing Tips
          </h4>
          <div class="tips-list">
            <div class="tip-item">
              <div class="tip-icon">1</div>
              <div class="tip-content">
                <div class="tip-title">Drag to reorder</div>
                <div class="tip-desc">Click and drag the handle on the left of a section to reorder</div>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">2</div>
              <div class="tip-content">
                <div class="tip-title">AI Assistant</div>
                <div class="tip-desc">Use the AI assistant to quickly tweak the outline structure</div>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">3</div>
              <div class="tip-content">
                <div class="tip-title">Add sections</div>
                <div class="tip-desc">Add or remove sections and bullet points as needed</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentPhase === 'COMPLETED'" class="panel-section">
          <h4 class="panel-title">
            <ThunderboltOutlined />
            Quick Actions
          </h4>
          <div class="action-list">
            <a-button block @click="copyContent" class="action-btn">
              <CopyOutlined />
              Copy All
            </a-button>
            <a-button block @click="viewArticle" class="action-btn">
              <EyeOutlined />
              View Details
            </a-button>
            <a-button block type="primary" @click="resetCreate" class="action-btn primary">
              <RedoOutlined />
              Write Another
            </a-button>
          </div>
        </div>

        <div v-if="currentPhase === 'COMPLETED'" class="panel-section stats-section">
          <h4 class="panel-title">
            <BarChartOutlined />
            Article Stats
          </h4>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ (article.fullContent || article.content || '').length }}</div>
              <div class="stat-label">Characters</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ article.images?.length || 0 }}</div>
              <div class="stat-label">Images</div>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <a class="help-link">
            <QuestionCircleOutlined />
            Help
          </a>
          <a class="help-link">
            <MessageOutlined />
            Feedback
          </a>
        </div>
      </aside>
    </div>

    <a-modal
      v-model:open="errorVisible"
      title="Creation Failed"
      @ok="errorVisible = false"
    >
      <p>{{ errorMessage }}</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { useBackgroundTaskStore } from '@/stores/backgroundTask'
import {
  RocketOutlined,
  LoadingOutlined,
  CheckCircleOutlined,
  CheckCircleFilled,
  CopyOutlined,
  EyeOutlined,
  RedoOutlined,
  ThunderboltOutlined,
  BulbOutlined,
  StarOutlined,
  ClockCircleOutlined,
  InfoCircleOutlined,
  BarChartOutlined,
  QuestionCircleOutlined,
  MessageOutlined,
  PictureOutlined,
  WarningOutlined,
  CrownOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue'
import { createArticle, confirmTitle, confirmOutline } from '@/api/articleController'
import { connectSSE, closeSSE, type SSEMessage } from '@/utils/sse'
import { isAdmin as checkIsAdmin, isVip as checkIsVip, hasQuota as checkHasQuota } from '@/utils/permission'
import { marked } from 'marked'
import TitleSelectingStage from './components/TitleSelectingStage.vue'
import OutlineEditingStage from './components/OutlineEditingStage.vue'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const backgroundTaskStore = useBackgroundTaskStore()

const isAdmin = computed(() => checkIsAdmin(loginUserStore.loginUser))
const isVip = computed(() => checkIsVip(loginUserStore.loginUser))
const quota = computed(() => loginUserStore.loginUser.quota ?? 0)
const hasQuota = computed(() => checkHasQuota(loginUserStore.loginUser))

// Agent steps (corresponding to the backend's 6 steps)
const agentSteps = [
  { title: 'Generate Titles', description: 'AI analyzes the topic and crafts catchy titles' },
  { title: 'Plan Outline', description: 'Build the article structure and clarify the flow' },
  { title: 'Write Content', description: 'Stream high-quality article content' },
  { title: 'Analyze Imagery', description: 'Smartly analyze image needs and placement' },
  { title: 'Generate Images', description: 'Auto-match high-resolution royalty-free images' },
  { title: 'Compose Article', description: 'Embed images into the article for the final result' },
]

const exampleTopics = [
  'How AI will reshape the workplace in 2026',
  'How developers can stay competitive',
  'The pros and cons of remote work',
  'How to cultivate deep thinking',
  'Trends in electric vehicles',
  'A guide to healthy eating',
]

// Phase state
const currentPhase = ref<string>('INPUT')  // INPUT, TITLE_SELECTING, OUTLINE_EDITING, CONTENT_GENERATING, COMPLETED

const topic = ref('')
const selectedStyle = ref('')  // Selected article style (empty string means default)
const selectedImageMethods = ref<string[]>([])  // Selected image methods (empty array means all)
const isCreating = ref(false)
const isCompleted = ref(false)
const isStreaming = ref(false)
const isOutlineStreaming = ref(false)
const currentStep = ref(0)
const taskId = ref('')
const errorVisible = ref(false)
const errorMessage = ref('')
const confirmLoading = ref(false)

interface RealtimeLog {
  timestamp: number
  level: string
  message: string
}
const realtimeLogs = ref<RealtimeLog[]>([])

const titleOptions = ref<Array<{mainTitle: string, subTitle: string}>>([])

const outline = ref<Array<{section: number, title: string, points: string[]}>>([])

// Outline data (streaming)
const outlineRaw = ref('')

interface OutlineItem {
  title: string
  points: string[]
  section: number
}

// Parse outline JSON (formatted as { "sections": [...] })
const parsedOutline = computed<OutlineItem[]>(() => {
  if (!outlineRaw.value) return []

  const str = outlineRaw.value.trim()

  // Try to parse the full JSON
  try {
    const parsed = JSON.parse(str)
    if (parsed && Array.isArray(parsed.sections)) {
      return parsed.sections
    }
    return []
  } catch {
    // When the JSON is incomplete, try to parse the completed portion
    try {
      // Find the last complete section object }
      // Format: { "sections": [ {...}, {...} ] }
      const sectionsMatch = str.match(/"sections"\s*:\s*\[/)
      if (!sectionsMatch) return []

      const sectionsStart = str.indexOf('[', sectionsMatch.index)
      if (sectionsStart === -1) return []

      // Starting from the sections array, find the last complete }
      const afterStart = str.substring(sectionsStart)
      const lastBrace = afterStart.lastIndexOf('}')

      if (lastBrace > 0) {
        const partialArray = afterStart.substring(0, lastBrace + 1) + ']'
        const parsed = JSON.parse(partialArray)
        if (Array.isArray(parsed)) {
          return parsed
        }
      }
      return []
    } catch {
      return []
    }
  }
})

// Reference to content area (used for auto-scrolling)
const mainContentRef = ref<HTMLElement | null>(null)

const imageCount = ref(0)
const totalImages = ref(5)
const imageProgress = ref(0)

const article = ref<Partial<API.ArticleVO>>({
  mainTitle: '',
  subTitle: '',
  content: '',
  fullContent: '',
  images: [],
})

let eventSource: EventSource | null = null

const markdownToHtml = (markdown: string | undefined) => {
  return marked(markdown || '')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (mainContentRef.value) {
      mainContentRef.value.scrollTop = mainContentRef.value.scrollHeight
    }
  })
}

const startCreate = async () => {
  if (!topic.value.trim()) {
    message.warning('Please enter a topic')
    return
  }

  if (!hasQuota.value) {
    message.error('Insufficient quota, cannot create article')
    return
  }

  isCreating.value = true
  currentStep.value = 0
  realtimeLogs.value = []
  addLog('Creating article task...', 'info')

  try {
    const res = await createArticle({
      topic: topic.value,
      style: selectedStyle.value || undefined,
      enabledImageMethods: selectedImageMethods.value.length > 0 ? selectedImageMethods.value : undefined
    })
    const newTaskId = res.data.data
    if (!newTaskId) {
      throw new Error('Failed to create task: no task ID returned')
    }
    taskId.value = newTaskId
    addLog(`Task created successfully, ID: ${newTaskId}`, 'success')

    // Refresh user info (update quota)
    await loginUserStore.fetchLoginUser()

    addLog('Real-time connection established, starting generation...', 'info')
    eventSource = connectSSE(taskId.value, {
      onMessage: handleSSEMessage,
      onError: handleSSEError,
      onComplete: handleSSEComplete,
    })
    backgroundTaskStore.startTask({
      id: taskId.value,
      label: 'Generating article',
      detail: topic.value,
      route: '/article/create',
    })
  } catch (error) {
    const err = error as Error
    message.error(err.message || 'Failed to create task')
    isCreating.value = false
  }
}

const addLog = (message: string, level: string = 'info') => {
  realtimeLogs.value.push({
    timestamp: Date.now(),
    level,
    message
  })
  // Cap logs at 50 entries
  if (realtimeLogs.value.length > 50) {
    realtimeLogs.value.shift()
  }
}

const formatLogTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('en-US', { hour12: false })
}

const handleSSEMessage = (msg: SSEMessage) => {
  console.log('SSE message:', msg)

  switch (msg.type) {
    case 'AGENT1_COMPLETE':
      // Agent 1 finished, enter title generating phase (show loading)
      currentPhase.value = 'TITLE_GENERATING'
      currentStep.value = 1
      addLog('Agent 1: title options generated', 'success')
      break

    case 'TITLES_GENERATED':
      // Title options ready, switch to title selecting phase
      currentPhase.value = 'TITLE_SELECTING'
      titleOptions.value = msg.titleOptions || []
      isCreating.value = false
      addLog(`Generated ${msg.titleOptions?.length || 0} title options`, 'success')
      break

    case 'AGENT2_STREAMING':
      // Outline streaming output (show generating state)
      currentPhase.value = 'OUTLINE_GENERATING'
      isOutlineStreaming.value = true
      outlineRaw.value += msg.content || ''
      scrollToBottom()
      break

    case 'OUTLINE_GENERATED':
      // Outline generated, switch to outline editing phase
      currentPhase.value = 'OUTLINE_EDITING'
      outline.value = msg.outline || []
      isCreating.value = false
      isOutlineStreaming.value = false
      addLog('Outline generated, awaiting confirmation', 'success')
      // Stay on step 1 (Plan Outline); user remains in this phase while editing
      break

    case 'AGENT2_COMPLETE':
      // Outline finished (handled internally; phase already switched in OUTLINE_GENERATED)
      // Don't change currentStep; stay on step 1 until the user confirms the outline before moving to step 2
      break

    case 'AGENT3_STREAMING':
      // Content streaming output, advance to step 2 (Write Content)
      if (!isStreaming.value) {
        backgroundTaskStore.updateTask(taskId.value, { detail: 'Writing content…' })
      }
      currentPhase.value = 'CONTENT_GENERATING'
      currentStep.value = 2
      isStreaming.value = true
      article.value.content += msg.content || ''
      scrollToBottom()
      break

    case 'AGENT3_COMPLETE':
      // Content finished, advance to image analysis step
      isStreaming.value = false
      currentStep.value = 3
      addLog('Content generation complete', 'success')
      message.info('Content is done — generating images in the background')
      backgroundTaskStore.updateTask(taskId.value, {
        detail: 'Generating images…',
        progress: 0,
      })
      break

    case 'AGENT4_COMPLETE':
      // Image analysis finished, advance to image generation step
      currentStep.value = 4
      totalImages.value = msg.imageRequirements?.length || 5
      addLog(`Image requirements analyzed, ${totalImages.value} images total`, 'success')
      backgroundTaskStore.updateTask(taskId.value, {
        detail: `0 / ${totalImages.value} images`,
        progress: 0,
      })
      break

    case 'IMAGE_COMPLETE':
      // Single image complete
      imageCount.value++
      imageProgress.value = Math.round((imageCount.value / totalImages.value) * 100)
      addLog(`Generating images ${imageCount.value}/${totalImages.value}`, 'info')
      backgroundTaskStore.updateTask(taskId.value, {
        detail: `${imageCount.value} / ${totalImages.value} images`,
        progress: imageProgress.value,
      })
      break

    case 'AGENT5_COMPLETE':
      // All images complete, advance to article composition step
      currentStep.value = 5
      article.value.images = msg.images
      addLog('All images generated', 'success')
      backgroundTaskStore.updateTask(taskId.value, {
        detail: 'Composing article…',
        progress: 100,
      })
      break

    case 'MERGE_COMPLETE':
      // Article composition finished
      article.value.fullContent = msg.fullContent
      scrollToBottom()
      addLog('Article composition complete', 'success')
      break

    case 'ALL_COMPLETE':
      // Fully complete
      currentPhase.value = 'COMPLETED'
      currentStep.value = 6
      isCompleted.value = true
      message.success('Article creation complete!')
      addLog('Article creation complete!', 'success')
      backgroundTaskStore.finishTask(taskId.value)
      break

    case 'ERROR':
      errorMessage.value = msg.message || 'Creation failed'
      errorVisible.value = true
      isCreating.value = false
      currentPhase.value = 'INPUT'
      addLog(`Creation failed: ${msg.message || 'unknown error'}`, 'error')
      backgroundTaskStore.finishTask(taskId.value)
      break
  }
}

const handleConfirmTitle = async (data: {mainTitle: string, subTitle: string, userDescription: string}) => {
  confirmLoading.value = true
  try {
    await confirmTitle({
      taskId: taskId.value,
      selectedMainTitle: data.mainTitle,
      selectedSubTitle: data.subTitle,
      userDescription: data.userDescription
    })
    // Save title information for use during the outline-generating phase
    article.value.mainTitle = data.mainTitle
    article.value.subTitle = data.subTitle
    // Don't switch phase directly; wait for SSE message OUTLINE_GENERATED
    message.success('Title confirmed, generating outline...')
  } catch (error) {
    const err = error as Error
    message.error(err.message || 'Failed to confirm title')
  } finally {
    confirmLoading.value = false
  }
}

const handleConfirmOutline = async (outlineData: Array<{section: number, title: string, points: string[]}>) => {
  confirmLoading.value = true
  try {
    await confirmOutline({
      taskId: taskId.value,
      outline: outlineData
    })
    // Update outlineRaw to the user-modified outline so CONTENT_GENERATING shows the right outline
    outlineRaw.value = JSON.stringify({ sections: outlineData })
    // Don't switch phase directly; wait for the backend to start content generation and push AGENT3_STREAMING
    message.success('Outline confirmed, generating content...')
  } catch (error) {
    const err = error as Error
    message.error(err.message || 'Failed to confirm outline')
  } finally {
    confirmLoading.value = false
  }
}

const handleSSEError = (error: Event) => {
  console.error('SSE error:', error)
  message.error('Connection failed, please try again')
  isCreating.value = false
  if (taskId.value) {
    backgroundTaskStore.finishTask(taskId.value)
  }
}

const handleSSEComplete = () => {
  console.log('SSE connection closed')
}

const copyContent = async () => {
  const content = article.value.fullContent || article.value.content || ''
  try {
    await navigator.clipboard.writeText(content)
    message.success('Copied to clipboard')
  } catch {
    message.error('Copy failed')
  }
}

const viewArticle = () => {
  router.push(`/article/${taskId.value}`)
}

const resetCreate = () => {
  currentPhase.value = 'INPUT'
  topic.value = ''
  selectedStyle.value = ''
  titleOptions.value = []
  outline.value = []
  isCreating.value = false
  isCompleted.value = false
  isStreaming.value = false
  isOutlineStreaming.value = false
  currentStep.value = 0
  imageCount.value = 0
  imageProgress.value = 0
  outlineRaw.value = ''
  confirmLoading.value = false
  realtimeLogs.value = []
  article.value = {
    mainTitle: '',
    subTitle: '',
    content: '',
    fullContent: '',
    images: [],
  }
}

onMounted(() => {
  if (route.query.topic) {
    topic.value = route.query.topic as string
  }
})

onBeforeUnmount(() => {
  // Keep the SSE alive while a background task is still in flight so the global
  // notice can keep updating across page navigation. The connection closes itself
  // on ALL_COMPLETE / ERROR (see utils/sse.ts).
  if (!taskId.value || !backgroundTaskStore.tasks.has(taskId.value)) {
    closeSSE(eventSource)
  }
})
</script>

<style scoped lang="scss">
.article-create-page {
  height: calc(100vh - 64px);
  background: var(--color-background-secondary);
  overflow: hidden;
}

.create-layout {
  display: grid;
  grid-template-columns: 320px 1fr 300px;
  height: 100%;
}

/* Left sidebar */
.sidebar-left {
  background: white;
  border-right: 1px solid var(--color-border);
  padding: 24px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--color-text);
}

.sidebar-subtitle {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0;
}

.flow-timeline {
  flex: 1;
}

.flow-item {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  position: relative;

  &:not(:last-child)::before {
    content: '';
    position: absolute;
    left: 15px;
    top: 46px;
    bottom: -14px;
    width: 2px;
    background: var(--color-border);
  }

  &.completed::before {
    background: var(--color-primary);
  }

  &.active::before {
    background: linear-gradient(180deg, var(--color-primary) 50%, var(--color-border) 50%);
  }
}

.flow-indicator {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  transition: all var(--transition-normal);

  .pending & {
    background: var(--color-background-tertiary);
    color: var(--color-text-muted);
    border: 2px solid var(--color-border);
  }

  .active & {
    background: rgba(249, 115, 22, 0.1);
    color: var(--color-primary);
    border: 2px solid var(--color-primary);
  }

  .completed & {
    background: var(--color-primary);
    color: white;
  }

  .step-number {
    font-weight: 600;
  }

  .spin-icon {
    animation: spin 1s linear infinite;
  }
}

.flow-content {
  flex: 1;
  min-width: 0;
}

.flow-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 2px;

  .pending & {
    color: var(--color-text-muted);
  }

  .active & {
    color: var(--color-primary-dark);
  }
}

.flow-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.flow-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 500;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: pulse 1.5s infinite;
}


/* Main content */
.main-content {
  padding: 32px 40px;
  overflow-y: auto;
  background: white;
}

/* Input state */
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

.quota-warning {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 16px;
  background: rgba(255, 77, 79, 0.08);
  border: 1px solid rgba(255, 77, 79, 0.2);
  border-radius: var(--radius-md);
  color: #ff4d4f;
  font-size: 13px;
}

/* Article style selection */
.style-section {
  padding: 16px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}

.style-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.style-group :deep(.ant-radio-wrapper) {
  margin: 0;
  padding: 6px 12px;
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.style-group :deep(.ant-radio-wrapper:hover) {
  border-color: var(--color-primary);
  background: rgba(249, 115, 22, 0.04);
}

.style-group :deep(.ant-radio-wrapper-checked) {
  border-color: var(--color-primary);
  background: rgba(249, 115, 22, 0.08);
}

/* Image source selection */
.image-methods-section {
  padding: 16px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.section-tip {
  font-size: 12px;
  color: var(--color-text-muted);
}

.methods-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.methods-group :deep(.ant-checkbox-wrapper) {
  margin: 0;
  padding: 6px 12px;
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.methods-group :deep(.ant-checkbox-wrapper:hover) {
  border-color: var(--color-primary);
  background: rgba(249, 115, 22, 0.04);
}

.methods-group :deep(.ant-checkbox-wrapper-checked) {
  border-color: var(--color-primary);
  background: rgba(249, 115, 22, 0.08);
}

.methods-group :deep(.ant-checkbox-wrapper-disabled) {
  opacity: 0.6;
  cursor: not-allowed;
}

.vip-icon {
  color: var(--color-primary);
  font-size: 12px;
  margin-left: 4px;
}

.vip-notice {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 10px 14px;
  background: rgba(249, 115, 22, 0.08);
  border-radius: var(--radius-md);
  font-size: 12px;
  color: var(--color-primary-dark);
  border: 1px solid rgba(249, 115, 22, 0.2);

  .anticon {
    color: var(--color-primary);
  }

  .upgrade-link {
    color: var(--color-primary);
    font-weight: 600;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

/* Creation in progress */
.creating-state,
.completed-state {
  max-width: 100%;
}

/* Title area */
.preview-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--color-text);
  line-height: 1.4;
}

.article-subtitle {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* Outline preview */
.outline-preview {
  margin-bottom: 24px;
  padding: 20px 24px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-lg);
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 16px;
}

.outline-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.outline-item {
  padding: 12px 16px;
  background: white;
  border-radius: var(--radius-md);
  border-left: 3px solid var(--color-primary);
}

.outline-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.outline-points {
  margin: 0;
  padding-left: 18px;

  li {
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin-bottom: 4px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

/* Content preview */
.content-preview {
  line-height: 1.8;
}

.markdown-body {
  line-height: 1.8;
  font-size: 15px;
  color: var(--color-text);

  :deep(h2) {
    font-size: 20px;
    font-weight: 600;
    margin: 24px 0 14px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--color-border);
    color: var(--color-text);
  }

  :deep(p) {
    margin-bottom: 14px;
    text-indent: 2em;
  }

  :deep(img) {
    display: block;
    max-width: 100%;
    max-height: 600px;
    width: auto;
    height: auto;
    margin: 20px auto;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    object-fit: contain;
  }

  // Mermaid diagram special handling (SVG format)
  :deep(img[src$=".svg"]) {
    max-width: 800px;
    max-height: 500px;
  }
}

.typing-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: var(--color-primary);
  font-weight: bold;
  font-size: 18px;
}

.image-progress-box {
  background: var(--color-background-secondary);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-top: 24px;
  text-align: center;

  .progress-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 16px;
    font-size: 15px;
    font-weight: 600;
    color: var(--color-text);
  }

  .progress-hint {
    margin: 12px 0 0;
    font-size: 13px;
    color: var(--color-text-muted);
  }
}

.loading-placeholder {
  text-align: center;
  padding: 100px 0;

  p {
    margin: 16px 0 0;
    color: var(--color-text-secondary);
    font-size: 15px;
  }
}

/* Completed state */
.success-header {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  margin-bottom: 24px;
  color: white;
  font-size: 14px;
  font-weight: 600;

  .success-icon {
    font-size: 16px;
  }
}

/* Right sidebar */
.sidebar-right {
  background: white;
  border-left: 1px solid var(--color-border);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.panel-section {
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-light);

  &:last-of-type {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 16px;
}

/* Quota info */
.quota-section {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.05) 0%, rgba(249, 115, 22, 0.02) 100%);
  border-radius: var(--radius-lg);
  padding: 16px !important;
  margin: -8px -8px 12px -8px;
}

.quota-admin {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quota-badge {
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;

  &.admin {
    background: linear-gradient(135deg, #0F172A 0%, #1E293B 100%);
    color: white;
  }

  &.vip {
    background: var(--gradient-primary);
    color: white;
  }
}

.quota-text {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.quota-info {
  text-align: center;
}

.quota-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.quota-number {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;

  &.low {
    color: #faad14;
  }

  &.empty {
    color: #ff4d4f;
  }
}

.quota-unit {
  font-size: 14px;
  color: var(--color-text-muted);
}

.quota-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 4px 0 12px;
}

.quota-progress {
  max-width: 120px;
  margin: 0 auto;
}

/* Trending topics */
.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  display: inline-block;
  padding: 8px 12px;
  background: var(--color-background-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
    background: rgba(249, 115, 22, 0.05);
    transform: translateY(-1px);
  }
}

/* Tips */
.tips-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);

  &:hover {
    background: rgba(249, 115, 22, 0.05);
  }
}

.tip-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--gradient-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.tip-content {
  flex: 1;
  min-width: 0;
}

.tip-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 2px;
}

.tip-desc {
  font-size: 11px;
  color: var(--color-text-muted);
  line-height: 1.4;
}

/* Progress info */
.progress-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.progress-step {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-md);
}

.step-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

.step-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
}

.progress-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: rgba(249, 115, 22, 0.08);
  border-radius: var(--radius-md);
  font-size: 12px;
  color: var(--color-primary-dark);
  line-height: 1.5;

  .anticon {
    flex-shrink: 0;
    margin-top: 2px;
  }

  &.waiting {
    background: rgba(250, 173, 20, 0.08);
    color: #d48806;
  }
}

/* Realtime logs */
.realtime-logs-section {
  .logs-container {
    max-height: 300px;
    overflow-y: auto;
    background: var(--color-background);
    border-radius: var(--radius-md);
    border: 1px solid var(--color-border-light);
    padding: 8px;

    .log-entry {
      display: flex;
      gap: 8px;
      padding: 6px 8px;
      font-size: 11px;
      line-height: 1.4;
      border-radius: var(--radius-sm);
      margin-bottom: 4px;
      transition: background var(--transition-fast);

      &:hover {
        background: var(--color-background-secondary);
      }

      &.success {
        .log-time {
          color: var(--color-success);
        }
      }

      &.error {
        background: rgba(239, 68, 68, 0.05);
        .log-time {
          color: var(--color-error);
        }
        .log-message {
          color: var(--color-error);
        }
      }

      .log-time {
        flex-shrink: 0;
        color: var(--color-text-muted);
        font-weight: 500;
      }

      .log-message {
        flex: 1;
        color: var(--color-text-secondary);
      }
    }

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--color-border);
      border-radius: var(--radius-full);
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }
}

/* Topic display */
.topic-display {
  padding: 12px 16px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--color-primary);

  p {
    margin: 0;
    font-size: 13px;
    color: var(--color-text);
    line-height: 1.6;
  }
}

/* Tip panel styling */
.tips-section {
  .tip-icon {
    background: transparent;
    font-size: 16px;
  }

  .tip-desc {
    font-size: 12px;
  }
}

/* Article stats */
.stats-section {
  margin-top: auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 16px 12px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-md);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Footer help links */
.panel-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
  display: flex;
  justify-content: center;
  gap: 20px;
}

.help-link {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--color-primary);
  }
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  height: 40px;
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &.primary {
    background: var(--gradient-primary);
    border: none;
    color: white;

    &:hover {
      opacity: 0.9;
    }
  }
}

/* Phase transition animation */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Loading stage style */
.loading-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 40px;
  text-align: center;

  h3 {
    font-size: 20px;
    font-weight: 600;
    color: var(--color-text);
    margin: 24px 0 8px;
  }

  p {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
  }
}

/* Outline generating state */
.outline-generating-state {
  max-width: 100%;
}

.outline-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in {
  animation: fade-in 0.4s ease-out;
}

/* Responsive */
@media (max-width: 1400px) {
  .create-layout {
    grid-template-columns: 280px 1fr 260px;
  }
}

@media (max-width: 1200px) {
  .create-layout {
    grid-template-columns: 240px 1fr 220px;
  }
}

@media (max-width: 992px) {
  .article-create-page {
    height: auto;
    min-height: calc(100vh - 64px);
    overflow: visible;
  }

  .create-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .sidebar-left,
  .sidebar-right {
    display: none;
  }

  .main-content {
    padding: 20px;
  }
}
</style>
