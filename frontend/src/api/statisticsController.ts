// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** Get system statistics overview GET /statistics/overview */
export async function getStatistics(options?: { [key: string]: any }) {
  return request<API.BaseResponseStatisticsVO>('/statistics/overview', {
    method: 'GET',
    ...(options || {}),
  })
}
