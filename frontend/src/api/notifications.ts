import { api } from './client'

export interface AppNotification {
  id: string
  title: string
  body: string
  orderId: number | null
  link: string | null
  read: boolean
  createdAt: string
}

export const notificationApi = {
  latest: () => api.get<AppNotification[]>('/notifications').then((r) => r.data),
  unreadCount: () => api.get<{ count: number }>('/notifications/unread-count').then((r) => r.data.count),
  markRead: (id: string) => api.post(`/notifications/${id}/read`),
  markAllRead: () => api.post('/notifications/read-all'),
  registerDevice: (token: string) => api.post('/notifications/devices', { token }),
}
