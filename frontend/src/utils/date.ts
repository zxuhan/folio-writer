/**
 * Date utility functions
 */
import dayjs from 'dayjs'

/**
 * Format a date
 * @param date Date string or timestamp
 * @param format Format template, defaults to 'YYYY-MM-DD HH:mm'
 */
export const formatDate = (date: string | number, format = 'YYYY-MM-DD HH:mm'): string => {
  return dayjs(date).format(format)
}

/**
 * Format a date (short format)
 * @param date Date string or timestamp
 */
export const formatDateShort = (date: string | number): string => {
  return formatDate(date, 'MM-DD HH:mm')
}

/**
 * Format a date (full format, including seconds)
 * @param date Date string or timestamp
 */
export const formatDateFull = (date: string | number): string => {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}
