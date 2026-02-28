<template>
  <a-layout-header class="header">
    <div class="header-container">
      <div class="header-left">
        <RouterLink to="/" class="logo-link">
          <div class="logo-wrapper">
            <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
            <h1 class="site-title">Folio</h1>
          </div>
        </RouterLink>
      </div>

      <nav class="nav-center">
        <RouterLink
          v-for="item in menuItems"
          :key="item.key"
          :to="item.key"
          :class="['nav-item', { active: selectedKeys.includes(item.key) }]"
        >
          <component :is="item.icon" class="nav-icon" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="header-right">
        <div v-if="loginUserStore.loginUser.id" class="user-dropdown">
          <RouterLink v-if="!isVip" to="/vip" class="upgrade-vip-btn">
            <CrownOutlined />
            <span>Upgrade to VIP</span>
          </RouterLink>
          <RouterLink v-else to="/vip" class="vip-badge">
            <CrownOutlined />
            <span>VIP</span>
          </RouterLink>

          <a-dropdown>
            <a-space class="user-info">
              <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="36" class="user-avatar" />
              <span class="user-name">
                {{ loginUserStore.loginUser.userName ?? 'Anonymous' }}
              </span>
            </a-space>
            <template #overlay>
              <a-menu class="dropdown-menu">
                <a-menu-item v-if="isVip" key="vip-info" class="vip-info-item" @click="router.push('/vip')">
                  <CrownOutlined />
                  <span>Lifetime Member Benefits</span>
                </a-menu-item>
                <a-menu-divider v-if="isVip" />
                <a-menu-item @click="doLogout" class="dropdown-item">
                  <LogoutOutlined />
                  <span>Sign Out</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <RouterLink to="/user/login" class="login-btn">Sign In</RouterLink>
        </div>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import {
  LogoutOutlined,
  HomeOutlined,
  EditOutlined,
  UnorderedListOutlined,
  SettingOutlined,
  CrownOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import { isVip as checkIsVip } from '@/utils/permission'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const selectedKeys = ref<string[]>(['/'])
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// Admin users are also treated as VIP
const isVip = computed(() => checkIsVip(loginUserStore.loginUser))

const originItems = [
  {
    key: '/',
    icon: HomeOutlined,
    label: 'Home',
  },
  {
    key: '/create',
    icon: EditOutlined,
    label: 'Create',
  },
  {
    key: '/article/list',
    icon: UnorderedListOutlined,
    label: 'History',
  },
  {
    key: '/admin/userManage',
    icon: SettingOutlined,
    label: 'Admin',
    admin: true,
  },
  {
    key: '/admin/statistics',
    icon: BarChartOutlined,
    label: 'Stats',
    admin: true,
  },
]

const menuItems = computed(() => {
  return originItems.filter((item) => {
    if (item.admin) {
      const loginUser = loginUserStore.loginUser
      return loginUser && loginUser.userRole === 'admin'
    }
    return true
  })
})

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: 'Not signed in',
    })
    message.success('Signed out successfully')
    await router.push('/user/login')
  } else {
    message.error('Sign out failed: ' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  padding: 0;
  height: 64px;
  line-height: 64px;
  border-bottom: 1px solid var(--color-border);
  transition: all var(--transition-normal);
  overflow: hidden;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-link {
  display: block;
  transition: opacity var(--transition-fast);
}

.logo-link:hover {
  opacity: 0.8;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  border-radius: var(--radius-md);
}

.site-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
  white-space: nowrap;
  letter-spacing: -0.3px;
}

.nav-center {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.nav-item:hover {
  color: var(--color-text);
  background: var(--color-background-secondary);
}

.nav-item.active {
  color: var(--color-primary-dark);
  background: rgba(249, 115, 22, 0.1);
}

.nav-icon {
  font-size: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  cursor: pointer;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.upgrade-vip-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  background: transparent;
  color: var(--color-primary);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: rgba(249, 115, 22, 0.08);
    color: var(--color-primary-dark);
  }

  .anticon {
    font-size: 13px;
  }
}

.vip-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    color: var(--color-primary-dark);
  }

  .anticon {
    font-size: 13px;
  }
}

.user-info {
  padding: 6px 12px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
}

.user-info:hover {
  background: var(--color-background-secondary);
}

.user-avatar {
  border: 2px solid var(--color-border);
}

.user-name {
  font-weight: 500;
  color: var(--color-text);
  font-size: 14px;
}

.login-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  padding: 0 24px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-brand);
  transition: all var(--transition-normal);
  text-decoration: none;
}

.login-btn:hover {
  color: white;
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.35);
}

.dropdown-menu {
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--color-background-secondary);
}

.vip-info-item {
  color: var(--color-primary-dark);
  background: rgba(249, 115, 22, 0.1);
  font-weight: 600;
  cursor: default;

  &:hover {
    background: rgba(249, 115, 22, 0.15);
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .site-title {
    display: none;
  }

  .nav-item span {
    display: none;
  }

  .nav-item {
    padding: 8px 12px;
  }

  .user-name {
    display: none;
  }
}
</style>
