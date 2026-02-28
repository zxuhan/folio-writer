/**
 * Server-Sent Events helper for streaming article generation progress.
 */

export interface SSEMessage {
  type: string
  data?: any
  [key: string]: any
}

export interface SSEOptions {
  onMessage: (message: SSEMessage) => void
  onError?: (error: Event) => void
  onComplete?: () => void
}

export const connectSSE = (taskId: string, options: SSEOptions): EventSource => {
  const { onMessage, onError, onComplete } = options

  // withCredentials forces the browser to attach the session cookie
  // even when the request is proxied through Vite's dev server.
  const eventSource = new EventSource(`/api/article/progress/${taskId}`, {
    withCredentials: true,
  })

  eventSource.onmessage = (event) => {
    try {
      const message: SSEMessage = JSON.parse(event.data)
      onMessage(message)

      if (message.type === 'ALL_COMPLETE' || message.type === 'ERROR') {
        eventSource.close()
        onComplete?.()
      }
    } catch (error) {
      console.error('Failed to parse SSE message:', error)
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE connection error', {
      readyState: eventSource.readyState,
      url: eventSource.url,
      error,
    })
    onError?.(error)
    eventSource.close()
  }

  return eventSource
}

export const closeSSE = (eventSource: EventSource | null) => {
  if (eventSource) {
    eventSource.close()
  }
}
