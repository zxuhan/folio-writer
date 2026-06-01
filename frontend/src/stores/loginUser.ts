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
    try {
      const res = await getLoginUser()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      }
    } catch (e) {
      // Probing login state must never throw: it runs inside the router guard,
      // and a transient backend error (e.g. the get/login 5xx) would otherwise
      // reject the guard's await and block all navigation. Treat failure as
      // "not signed in" and let the app render.
      console.error('fetchLoginUser failed', e)
    }
  }

  // Update logged-in user information
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
