import { USER_ROLE_ADMIN, USER_ROLE_VIP } from '@/constants/user'

/**
 * Permission check utilities
 */

/**
 * Check whether the user is an administrator
 */
export const isAdmin = (user?: API.LoginUserVO): boolean => {
  return user?.userRole === USER_ROLE_ADMIN
}

/**
 * Check whether the user is a VIP (administrators included)
 */
export const isVip = (user?: API.LoginUserVO): boolean => {
  return user?.userRole === USER_ROLE_VIP || isAdmin(user)
}

/**
 * Check whether the user has remaining quota
 */
export const hasQuota = (user?: API.LoginUserVO): boolean => {
  if (isAdmin(user) || isVip(user)) {
    return true
  }
  return (user?.quota ?? 0) > 0
}
