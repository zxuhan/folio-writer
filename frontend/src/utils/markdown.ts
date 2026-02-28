/**
 * Markdown utility functions
 */
import { marked } from 'marked'

/**
 * Convert Markdown to HTML
 * @param markdown Markdown content
 */
export const markdownToHtml = (markdown: string): string => {
  return marked(markdown) as string
}
