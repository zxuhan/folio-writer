/**
 * Article-related utility functions
 */
import { STATUS_TEXT_MAP, STATUS_TAG_COLOR_MAP, STATUS_COLOR_MAP } from '@/constants/article'

/**
 * Get status text
 * @param status Status value
 */
export const getStatusText = (status: string): string => {
  return STATUS_TEXT_MAP[status] || status
}

/**
 * Get status tag color (for Ant Design Tag)
 * @param status Status value
 */
export const getStatusTagColor = (status: string): string => {
  return STATUS_TAG_COLOR_MAP[status] || 'default'
}

/**
 * Get status color (for custom styling)
 * @param status Status value
 */
export const getStatusColor = (status: string): string => {
  return STATUS_COLOR_MAP[status] || '#999'
}

/**
 * Export article as a Markdown file
 * @param title Article title
 * @param subTitle Subtitle
 * @param content Body content
 * @param fullContent Full article with images (optional)
 * @param outline Outline (optional)
 * @param images List of images (optional)
 */
export interface ExportArticleOptions {
  title: string
  subTitle?: string
  content?: string
  fullContent?: string
  outline?: Array<{ section: number; title: string }>
  images?: Array<{ description: string; url: string }>
}

export const exportAsMarkdown = (options: ExportArticleOptions): void => {
  const { title, subTitle, content, fullContent, outline, images } = options

  let markdown = `# ${title}\n\n`
  if (subTitle) {
    markdown += `> ${subTitle}\n\n`
  }

  // Prefer full content with images
  if (fullContent) {
    markdown += fullContent
  } else {
    if (outline && outline.length > 0) {
      markdown += `## Table of Contents\n\n`
      outline.forEach((item) => {
        markdown += `${item.section}. ${item.title}\n`
      })
      markdown += `\n---\n\n`
    }

    markdown += content || ''

    if (images && images.length > 0) {
      markdown += `\n\n## Images\n\n`
      images.forEach((image) => {
        markdown += `![${image.description}](${image.url})\n\n`
      })
    }
  }

  const blob = new Blob([markdown], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${title || 'article'}.md`
  a.click()
  URL.revokeObjectURL(url)
}
