import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'
import { DEFAULT_USERNAME } from '@/constants/user'

/**
 * Logged-in user information
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  // Default value
  const loginUser = ref<API.LoginUserVO>({
    userName: DEFAULT_USERNAME,
  })

  // Fetch logged-in user information
  async function fetchLoginUser() {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  // Update logged-in user information
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
