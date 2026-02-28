// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** Create VIP payment session POST /payment/create-vip-session */
export async function createVipPaymentSession(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/payment/create-vip-session', {
    method: 'POST',
    ...(options || {}),
  })
}

/** Get current user's payment records GET /payment/records */
export async function getPaymentRecords(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPaymentRecord>('/payment/records', {
    method: 'GET',
    ...(options || {}),
  })
}

/** Request a refund POST /payment/refund */
export async function refund(
  // Param type generated for path/query (swagger does not generate an object for non-body params by default)
  params: API.refundParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/payment/refund', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
