import { useLoginUserStore } from '@/stores/loginUser'
import { message } from 'ant-design-vue'
import router from '@/router'
import { USER_ROLE_ADMIN } from '@/constants/user'

let firstFetchLoginUser = true

/**
 * Global route guard for permission checks.
 * On first navigation we wait for the backend to return the current user
 * before evaluating admin-only routes.
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  const toUrl = to.fullPath
  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== USER_ROLE_ADMIN) {
      message.error('You do not have permission')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  next()
})
