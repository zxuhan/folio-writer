<template>
  <div id="userManagePage">
    <div class="page-header">
      <div class="header-container">
        <div class="header-content">
          <h1 class="page-title">User Management</h1>
          <p class="page-subtitle">Manage all users in the system</p>
        </div>
      </div>
    </div>

    <div class="container">
      <a-card :bordered="false" class="content-card">
        <div class="search-section">
          <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
            <a-form-item label="Account">
              <a-input v-model:value="searchParams.userAccount" placeholder="Enter account" class="search-input" />
            </a-form-item>
            <a-form-item label="Username">
              <a-input v-model:value="searchParams.userName" placeholder="Enter username" class="search-input" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" class="search-btn">
                <template #icon>
                  <SearchOutlined />
                </template>
                Search
              </a-button>
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <a-table
          :columns="columns"
          :data-source="data"
          :pagination="pagination"
          @change="doTableChange"
          class="user-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'userAvatar'">
              <a-avatar :src="record.userAvatar" :size="48" class="user-avatar" />
            </template>
            <template v-else-if="column.dataIndex === 'userRole'">
              <a-tag v-if="record.userRole === 'admin'" color="purple" class="role-tag">
                Admin
              </a-tag>
              <a-tag v-else color="blue" class="role-tag">
                Member
              </a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'createTime'">
              <span class="time-text">{{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-popconfirm
                title="Are you sure you want to delete this user?"
                ok-text="Confirm"
                cancel-text="Cancel"
                @confirm="doDelete(record.id)"
              >
                <a-button type="link" danger class="delete-btn">Delete</a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: 'Account',
    dataIndex: 'userAccount',
  },
  {
    title: 'Username',
    dataIndex: 'userName',
  },
  {
    title: 'Avatar',
    dataIndex: 'userAvatar',
  },
  {
    title: 'Profile',
    dataIndex: 'userProfile',
  },
  {
    title: 'Role',
    dataIndex: 'userRole',
  },
  {
    title: 'Created At',
    dataIndex: 'createTime',
  },
  {
    title: 'Action',
    key: 'action',
  },
]

const data = ref<API.UserVO[]>([])
const total = ref(0)

const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('Failed to load data: ' + res.data.message)
  }
}

const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `${total} total`,
  }
})

const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id: Number(id) })
  if (res.data.code === 0) {
    message.success('Deleted successfully')
    fetchData()
  } else {
    message.error('Delete failed')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
#userManagePage {
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

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .content-card {
    border-radius: var(--radius-lg);
    border: 1px solid var(--color-border);
    box-shadow: none;
    background: white;

    :deep(.ant-card-body) {
      padding: 24px;
    }
  }

  .search-section {
    margin-bottom: 8px;
  }

  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: flex-end;

    :deep(.ant-form-item) {
      margin-bottom: 0;
    }

    :deep(.ant-form-item-label > label) {
      font-weight: 500;
      font-size: 13px;
      color: var(--color-text-secondary);
    }
  }

  .search-input {
    width: 180px;
    border-radius: var(--radius-md);

    &:hover {
      border-color: var(--color-primary-light);
    }

    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.1);
    }
  }

  .search-btn {
    border-radius: var(--radius-md);
    font-weight: 500;
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

  .user-table {
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
      margin: 16px 0 0;
    }
  }

  .user-avatar {
    border: 2px solid var(--color-border);
  }

  .role-tag {
    border-radius: var(--radius-full);
    font-weight: 500;
    font-size: 12px;
    padding: 2px 10px;
  }

  .time-text {
    color: var(--color-text-secondary);
    font-size: 13px;
  }

  .delete-btn {
    font-weight: 500;
    font-size: 13px;
    color: var(--color-error);
    padding: 4px 8px;

    &:hover {
      color: #DC2626;
    }
  }
}

@media (max-width: 768px) {
  #userManagePage {
    .page-header {
      padding: 24px 20px;
    }

    .page-title {
      font-size: 22px;
    }

    .search-form {
      flex-direction: column;
      align-items: stretch;

      :deep(.ant-form-item) {
        width: 100%;
      }
    }

    .search-input {
      width: 100%;
    }
  }
}
</style>
