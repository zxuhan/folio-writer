/**
 * Article-related constants.
 */

export enum ArticleStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export const STATUS_TEXT_MAP: Record<string, string> = {
  [ArticleStatus.PENDING]: 'Pending',
  [ArticleStatus.PROCESSING]: 'Generating',
  [ArticleStatus.COMPLETED]: 'Completed',
  [ArticleStatus.FAILED]: 'Failed',
}

// Status colors for Ant Design Tag
export const STATUS_TAG_COLOR_MAP: Record<string, string> = {
  [ArticleStatus.PENDING]: 'default',
  [ArticleStatus.PROCESSING]: 'processing',
  [ArticleStatus.COMPLETED]: 'success',
  [ArticleStatus.FAILED]: 'error',
}

// Status colors for custom styling
export const STATUS_COLOR_MAP: Record<string, string> = {
  [ArticleStatus.PENDING]: '#6B7280',
  [ArticleStatus.PROCESSING]: '#3B82F6',
  [ArticleStatus.COMPLETED]: '#16A34A',
  [ArticleStatus.FAILED]: '#EF4444',
}

export const MAX_TOPIC_LENGTH = 500
export const DEFAULT_TOTAL_IMAGES = 5

export const STATUS_OPTIONS = [
  { value: '', label: 'All statuses' },
  { value: ArticleStatus.COMPLETED, label: 'Completed' },
  { value: ArticleStatus.PROCESSING, label: 'Generating' },
  { value: ArticleStatus.PENDING, label: 'Pending' },
  { value: ArticleStatus.FAILED, label: 'Failed' },
]
