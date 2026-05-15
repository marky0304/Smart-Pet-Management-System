import { ref, onUnmounted } from 'vue'

export function useNotificationWebSocket() {
  const lastNotification = ref(null)
  const unreadCount = ref(0)
  const connected = ref(false)

  let ws = null
  let reconnectTimer = null

  function getUserId() {
    try {
      const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return info.id || null
    } catch {
      return null
    }
  }

  function getWsBaseUrl() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.hostname
    const port = import.meta.env.VITE_BACKEND_PORT || '8080'
    return `${protocol}//${host}:${port}/ws/notification`
  }

  function connect() {
    const userId = getUserId()
    if (!userId) return
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return

    try {
      ws = new WebSocket(`${getWsBaseUrl()}/${userId}`)

      ws.onopen = () => {
        connected.value = true
        if (reconnectTimer) {
          clearTimeout(reconnectTimer)
          reconnectTimer = null
        }
      }

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          lastNotification.value = {
            type: data.type,
            content: data.content,
            timestamp: Date.now()
          }
          unreadCount.value++
        } catch { /* ignore parse errors */ }
      }

      ws.onclose = () => {
        connected.value = false
        scheduleReconnect()
      }

      ws.onerror = () => {
        ws?.close()
      }
    } catch { /* WebSocket construction failed */ }
  }

  function scheduleReconnect() {
    if (reconnectTimer) return
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, 5000)
  }

  function disconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.onclose = null
      ws.onerror = null
      ws.onmessage = null
      ws.close()
      ws = null
    }
    connected.value = false
  }

  function resetCount() {
    unreadCount.value = 0
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    lastNotification,
    unreadCount,
    connected,
    connect,
    disconnect,
    resetCount
  }
}
