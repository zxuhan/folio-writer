<template>
  <div class="article-list-page">
    <div class="page-header">
      <div class="header-container">
        <div class="header-content">
          <h1 class="page-title">History</h1>
          <p class="page-subtitle">Manage all the articles you have created</p>
        </div>
        <a-button type="primary" size="large" @click="goToCreate" class="create-btn">
          <template #icon>
            <PlusOutlined />
          </template>
          New Article
        </a-button>
      </div>
    </div>

    <div class="container">
      <div class="filter-bar">
        <div class="filter-left">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="Search article titles..."
            style="width: 280px"
            @search="handleSearch"
            @change="handleSearchChange"
            allow-clear
            class="search-input"
          >
            <template #prefix>
              <SearchOutlined class="search-icon" />
            </template>
          </a-input-search>

          <a-range-picker
            v-model:value="dateRange"
            :placeholder="['Start date', 'End date']"
            @change="handleDateChange"
            class="date-picker"
          />

          <a-select
            v-model:value="statusFilter"
            placeholder="All statuses"
            style="width: 140px"
            allow-clear
            @change="handleStatusChange"
            class="status-select"
          >
            <a-select-option value="">All statuses</a-select-option>
            <a-select-option value="COMPLETED">Completed</a-select-option>
            <a-select-option value="PROCESSING">Generating</a-select-option>
            <a-select-option value="PENDING">Pending</a-select-option>
            <a-select-option value="FAILED">Failed</a-select-option>
          </a-select>
        </div>

        <div class="filter-right">
          <span class="total-count">{{ pagination.total }} articles total</span>
        </div>
      </div>

      <a-card :bordered="false" class="table-card">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          row-key="id"
          class="article-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <div class="title-cell" @click="viewArticle(record)">
                <div class="main-title">{{ record.mainTitle || record.topic || '-' }}</div>
                <div class="sub-title">{{ record.subTitle || '-' }}</div>
              </div>
            </template>

            <template v-else-if="column.key === 'status'">
              <span :class="['status-badge', `status-${record.status?.toLowerCase()}`]">
                <span class="status-dot"></span>
                {{ getStatusText(record.status) }}
              </span>
            </template>

            <template v-else-if="column.key === 'createTime'">
              <span class="time-text">{{ formatDate(record.createTime) }}</span>
            </template>

            <template v-else-if="column.key === 'action'">
              <div class="action-group">
                <a-button type="link" size="small" @click="viewArticle(record)" class="action-btn view-btn">
                  <EyeOutlined />
                  View
                </a-button>
                <a-button
                  v-if="record.status === 'FAILED'"
                  type="link"
                  size="small"
                  @click="retryArticle(record)"
                  class="action-btn retry-btn"
                >
                  <RedoOutlined />
                  Retry
                </a-button>
                <a-button
                  v-else
                  type="link"
                  size="small"
                  @click="exportArticle(record)"
                  class="action-btn export-btn"
                >
                  <DownloadOutlined />
                  Export
                </a-button>
                <a-popconfirm
                  title="Are you sure you want to delete this article?"
                  ok-text="Confirm"
                  cancel-text="Cancel"
                  @confirm="deleteArticle(record)"
                >
                  <a-button type="link" size="small" danger class="action-btn delete-btn">
                    <DeleteOutlined />
                    Delete
                  </a-button>
                </a-popconfirm>
              </div>
            </template>
          </template>

          <template #emptyText>
            <div class="empty-state">
              <FileTextOutlined class="empty-icon" />
              <p class="empty-title">No articles yet</p>
              <p class="empty-desc">Start writing your first article</p>
              <a-button type="primary" @click="goToCreate">
                <PlusOutlined />
                New Article
              </a-button>
            </div>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  EyeOutlined,
  DownloadOutlined,
  DeleteOutlined,
  FileTextOutlined,
  RedoOutlined
} from '@ant-design/icons-vue'
import { listArticle, deleteArticle as deleteArticleApi, getArticle } from '@/api/articleController'
import dayjs, { type Dayjs } from 'dayjs'

const router = useRouter()

const searchKeyword = ref('')
const dateRange = ref<[Dayjs, Dayjs] | null>(null)
const statusFilter = ref<string>('')

const columns = [
  {
    title: 'Topic',
    dataIndex: 'topic',
    key: 'topic',
    width: 180,
    ellipsis: true,
  },
  {
    title: 'Title',
    key: 'title',
    width: 280,
  },
  {
    title: 'Status',
    key: 'status',
    width: 110,
  },
  {
    title: 'Created At',
    key: 'createTime',
    width: 160,
  },
  {
    title: 'Actions',
    key: 'action',
    width: 200,
  },
]

const loading = ref(false)
const dataSource = ref<API.ArticleVO[]>([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `${total} total`,
  pageSizeOptions: ['10', '20', '50', '100']
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await listArticle({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      // If backend supports it, search params can be passed
      // keyword: searchKeyword.value,
      // status: statusFilter.value,
    })
    const pageData = res.data.data
    let records = pageData?.records || []

    // Frontend filter (when backend doesn't support it)
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      records = records.filter((item: API.ArticleVO) =>
        item.mainTitle?.toLowerCase().includes(keyword) ||
        item.topic?.toLowerCase().includes(keyword)
      )
    }

    if (statusFilter.value) {
      records = records.filter((item: API.ArticleVO) => item.status === statusFilter.value)
    }

    if (dateRange.value) {
      const [start, end] = dateRange.value
      records = records.filter((item: API.ArticleVO) => {
        const createTime = dayjs(item.createTime)
        return createTime.isAfter(start.startOf('day')) && createTime.isBefore(end.endOf('day'))
      })
    }

    dataSource.value = records
    pagination.value.total = pageData?.totalRow || 0
  } catch (error: any) {
    message.error(error.message || 'Failed to load')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.current = 1
  loadData()
}

const handleSearchChange = () => {
  // Trigger search when the search box is cleared
  if (!searchKeyword.value) {
    handleSearch()
  }
}

const handleDateChange = () => {
  pagination.value.current = 1
  loadData()
}

const handleStatusChange = () => {
  pagination.value.current = 1
  loadData()
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const viewArticle = (record: API.ArticleVO) => {
  router.push(`/article/${record.taskId}`)
}

const exportArticle = async (record: API.ArticleVO) => {
  try {
    const res = await getArticle({ taskId: record.taskId || '' })
    const article = res.data.data
    if (!article) {
      message.error('Article data not found')
      return
    }

    let markdown = `# ${article.mainTitle}\n\n`
    markdown += `> ${article.subTitle}\n\n`

    if (article.fullContent) {
      markdown += article.fullContent
    } else {
      markdown += article.content || ''
    }

    const blob = new Blob([markdown], { type: 'text/markdown' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${article.mainTitle || 'article'}.md`
    a.click()
    URL.revokeObjectURL(url)

    message.success('Exported successfully')
  } catch (error) {
    message.error((error as Error).message || 'Export failed')
  }
}

const deleteArticle = async (record: API.ArticleVO) => {
  try {
    await deleteArticleApi({ id: record.id })
    message.success('Deleted successfully')
    loadData()
  } catch (error) {
    message.error((error as Error).message || 'Delete failed')
  }
}

const retryArticle = (record: API.ArticleVO) => {
  Modal.confirm({
    title: 'Confirm Retry',
    content: `Recreate the article using the same topic "${record.topic}". Continue?`,
    okText: 'Confirm',
    cancelText: 'Cancel',
    onOk: () => {
      router.push({
        path: '/create',
        query: {
          topic: record.topic || '',
          style: record.userDescription || ''
        }
      })
    }
  })
}

const goToCreate = () => {
  router.push('/create')
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    PENDING: 'Pending',
    PROCESSING: 'Generating',
    COMPLETED: 'Completed',
    FAILED: 'Failed',
  }
  return textMap[status] || status
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.article-list-page {
  background: var(--color-background-secondary);
  min-height: 100vh;
  padding-bottom: 60px;

  .page-header {
    background: var(--gradient-hero);
    padding: 32px 20px;
    margin-bottom: 24px;
  }

  .header-container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-content {
    color: var(--color-text);
  }

  .page-title {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 6px;
    letter-spacing: -0.5px;
    color: var(--color-text);
  }

  .page-subtitle {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
  }

  .create-btn {
    height: 44px;
    padding: 0 24px;
    font-size: 15px;
    font-weight: 600;
    border-radius: var(--radius-lg);
    background: var(--gradient-primary) !important;
    border: none !important;
    color: white !important;
    box-shadow: var(--shadow-brand) !important;
    transition: opacity var(--transition-normal) !important;

    &:hover,
    &:focus,
    &:active {
      background: var(--gradient-primary) !important;
      border: none !important;
      color: white !important;
      box-shadow: var(--shadow-brand) !important;
      opacity: 0.92;
    }

    :deep(.ant-wave) {
      display: none;
    }
  }

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  // Filter bar
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px 20px;
    background: white;
    border-radius: var(--radius-lg);
    border: 1px solid var(--color-border);
  }

  .filter-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .search-input {
    :deep(.ant-input-affix-wrapper) {
      border-top-left-radius: var(--radius-md);
      border-bottom-left-radius: var(--radius-md);
      border-top-right-radius: 0;
      border-bottom-right-radius: 0;
      border-color: var(--color-border);

      &:hover, &:focus {
        border-color: var(--color-primary);
      }
    }

    .search-icon {
      color: var(--color-text-muted);
    }
  }

  .date-picker {
    :deep(.ant-picker) {
      border-radius: var(--radius-md);
    }
  }

  .status-select {
    :deep(.ant-select-selector) {
      border-radius: var(--radius-md) !important;
    }
  }

  .filter-right {
    .total-count {
      font-size: 14px;
      color: var(--color-text-secondary);
    }
  }

  .table-card {
    border-radius: var(--radius-lg);
    border: 1px solid var(--color-border);
    box-shadow: none;
    overflow: hidden;

    :deep(.ant-card-body) {
      padding: 0;
    }
  }

  .article-table {
    :deep(.ant-table-thead > tr > th) {
      background: var(--color-background-secondary);
      font-weight: 600;
      font-size: 13px;
      color: var(--color-text-secondary);
      border-bottom: 1px solid var(--color-border);
      padding: 14px 16px;
    }

    :deep(.ant-table-tbody > tr > td) {
      padding: 16px;
      border-bottom: 1px solid var(--color-border-light);
    }

    :deep(.ant-table-tbody > tr:hover > td) {
      background: rgba(249, 115, 22, 0.02);
    }

    :deep(.ant-table-pagination) {
      margin: 16px;
    }
  }

  .title-cell {
    cursor: pointer;
    transition: all var(--transition-fast);

    &:hover {
      .main-title {
        color: var(--color-primary);
      }
    }

    .main-title {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 4px;
      color: var(--color-text);
      transition: color var(--transition-fast);
      line-height: 1.4;
    }

    .sub-title {
      font-size: 13px;
      color: var(--color-text-muted);
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    border-radius: var(--radius-full);
    font-size: 12px;
    font-weight: 500;

    .status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
    }

    &.status-completed {
      background: rgba(249, 115, 22, 0.1);
      color: var(--color-primary-dark);

      .status-dot {
        background: var(--color-primary);
      }
    }

    &.status-processing {
      background: rgba(59, 130, 246, 0.1);
      color: #2563EB;

      .status-dot {
        background: #3B82F6;
        animation: pulse 1.5s infinite;
      }
    }

    &.status-pending {
      background: var(--color-background-tertiary);
      color: var(--color-text-secondary);

      .status-dot {
        background: var(--color-text-muted);
      }
    }

    &.status-failed {
      background: rgba(239, 68, 68, 0.1);
      color: #DC2626;

      .status-dot {
        background: #EF4444;
      }
    }
  }

  .time-text {
    color: var(--color-text-secondary);
    font-size: 13px;
  }

  .action-group {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .action-btn {
    font-size: 13px;
    padding: 4px 8px;
    height: auto;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    transition: all var(--transition-fast);

    &.view-btn {
      color: var(--color-primary);

      &:hover {
        color: var(--color-primary-dark);
      }
    }

    &.retry-btn {
      color: #ff4d4f;

      &:hover {
        color: #DC2626;
      }
    }

    &.export-btn {
      color: var(--color-text-secondary);

      &:hover {
        color: var(--color-text);
      }
    }

    &.delete-btn {
      &:hover {
        color: #DC2626;
      }
    }
  }

  // Empty state
  .empty-state {
    padding: 60px 20px;
    text-align: center;

    .empty-icon {
      font-size: 48px;
      color: var(--color-text-muted);
      margin-bottom: 16px;
    }

    .empty-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text);
      margin: 0 0 8px;
    }

    .empty-desc {
      font-size: 14px;
      color: var(--color-text-muted);
      margin: 0 0 20px;
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@media (max-width: 992px) {
  .article-list-page {
    .filter-bar {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;
    }

    .filter-left {
      flex-wrap: wrap;
    }

    .filter-right {
      text-align: right;
    }
  }
}

@media (max-width: 768px) {
  .article-list-page {
    .page-header {
      padding: 24px 20px;
    }

    .header-container {
      flex-direction: column;
      gap: 16px;
      text-align: center;
    }

    .page-title {
      font-size: 22px;
    }

    .create-btn {
      width: 100%;
    }

    .filter-left {
      flex-direction: column;
      width: 100%;

      .search-input,
      .date-picker,
      .status-select {
        width: 100% !important;
      }
    }
  }
}
</style>
