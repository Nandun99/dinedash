import type { Role, User } from '../types/auth'
import type { OrderStatus, OrderSummary } from '../types/order'
import type { Restaurant } from '../types/restaurant'
import { api } from './client'

export interface SpringPage<T> {
  content: T[]
  number: number
  totalPages: number
  totalElements: number
}

export type ApprovalStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'SUSPENDED'

export interface OrderStats {
  ordersToday: number
  deliveredRevenueToday: number
  byStatus: Partial<Record<OrderStatus, number>>
}

export const adminApi = {
  stats: () => api.get<OrderStats>('/admin/orders/stats').then((r) => r.data),
  orders: (status?: OrderStatus, page = 0) =>
    api.get<SpringPage<OrderSummary>>('/admin/orders', { params: { status, page } }).then((r) => r.data),
  restaurants: (status?: ApprovalStatus, page = 0) =>
    api.get<SpringPage<Restaurant & { approvalStatus: ApprovalStatus }>>('/admin/restaurants', { params: { status, page } }).then((r) => r.data),
  setApproval: (id: string, value: ApprovalStatus) =>
    api.patch(`/admin/restaurants/${id}/approval`, null, { params: { value } }),
  users: (role?: Role, page = 0) =>
    api.get<SpringPage<User>>('/admin/users', { params: { role, page } }).then((r) => r.data),
  setUserEnabled: (id: number, value: boolean) => api.patch(`/admin/users/${id}/enabled`, null, { params: { value } }),
}
