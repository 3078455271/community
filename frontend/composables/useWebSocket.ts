type WebSocketHandler<T = unknown> = (payload: T) => void

type WebSocketEvent = {
  type: string
  payload: unknown
}

const RECONNECT_DELAY_MS = 3000

let socket: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let shouldReconnect = false
let reconnectAttempts = 0
const listeners = new Map<string, Set<WebSocketHandler>>()

export const useWebSocket = () => {
  const buildUrl = () => {
    const config = useRuntimeConfig()
    const apiBase = config.public.apiBase?.trim() || ''
    if (apiBase.startsWith('https://')) {
      return `${apiBase.replace('https://', 'wss://')}/api/ws`
    }
    if (apiBase.startsWith('http://')) {
      return `${apiBase.replace('http://', 'ws://')}/api/ws`
    }
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${protocol}//${window.location.host}/api/ws`
  }

  const emit = (event: WebSocketEvent) => {
    listeners.get(event.type)?.forEach(handler => handler(event.payload))
  }

  const clearReconnectTimer = () => {
    if (!reconnectTimer) return
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }

  const scheduleReconnect = () => {
    if (!shouldReconnect || reconnectTimer) return
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      reconnectAttempts += 1
      connect()
    }, RECONNECT_DELAY_MS)
  }

  const connect = () => {
    if (!import.meta.client) return
    shouldReconnect = true
    if (socket && [WebSocket.OPEN, WebSocket.CONNECTING].includes(socket.readyState)) return

    socket = new WebSocket(buildUrl())
    socket.onopen = () => {
      emit({ type: 'websocket.connected', payload: { reconnected: reconnectAttempts > 0 } })
      reconnectAttempts = 0
    }
    socket.onmessage = (message) => {
      try {
        emit(JSON.parse(message.data) as WebSocketEvent)
      } catch {
        // 忽略无法识别的服务端消息
      }
    }
    socket.onclose = () => {
      socket = null
      scheduleReconnect()
    }
    socket.onerror = () => {
      socket?.close()
    }
  }

  const disconnect = () => {
    shouldReconnect = false
    clearReconnectTimer()
    const currentSocket = socket
    socket = null
    if (currentSocket) {
      currentSocket.onclose = null
      currentSocket.onerror = null
      currentSocket.close()
    }
  }

  const subscribe = <T = unknown>(type: string, handler: WebSocketHandler<T>) => {
    const handlers = listeners.get(type) ?? new Set<WebSocketHandler>()
    handlers.add(handler as WebSocketHandler)
    listeners.set(type, handlers)

    return () => {
      handlers.delete(handler as WebSocketHandler)
      if (handlers.size === 0) {
        listeners.delete(type)
      }
    }
  }

  return {
    connect,
    disconnect,
    subscribe,
  }
}