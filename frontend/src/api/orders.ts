import type { Page } from '../types/restaurant'
import type { Address, Order, OrderSummary, PlaceOrderPayload } from '../types/order'
import { api } from './client'

export const orderApi = {
  place: (p: PlaceOrderPayload) => api.post<Order>('/orders', p).then((r) => r.data),
  mine: (page = 0) =>
    api.get<Page<OrderSummary> & { number: number; last: boolean }>('/orders', { params: { page } }).then((r) => r.data),
  get: (id: number | string) => api.get<Order>(`/orders/${id}`).then((r) => r.data),
  cancel: (id: number, reason?: string) => api.post<Order>(`/orders/${id}/cancel`, { reason }).then((r) => r.data),
}

export const addressApi = {
  list: () => api.get<Address[]>('/users/me/addresses').then((r) => r.data),
  create: (a: Omit<Address, 'id'>) => api.post<Address>('/users/me/addresses', a).then((r) => r.data),
}
