import axios from 'axios'
import type { MenuItem, Restaurant } from '../types/restaurant'
import { api } from './client'

export interface RestaurantPayload {
  name: string
  description?: string
  cuisines: string[]
  phone: string
  address: { street: string; city: string; postalCode?: string }
  latitude: number
  longitude: number
  opensAt?: string
  closesAt?: string
  deliveryFee: number
  minimumOrder: number
  averagePrepMinutes: number
}

export interface MenuItemPayload {
  name: string
  description?: string
  category: string
  price: number
  vegetarian: boolean
  spicy: boolean
}

export type ImageTarget = 'LOGO' | 'COVER' | 'MENU_ITEM'

const base = '/owner/restaurants'

export const ownerApi = {
  restaurants: () => api.get<Restaurant[]>(base).then((r) => r.data),
  createRestaurant: (p: RestaurantPayload) => api.post<Restaurant>(base, p).then((r) => r.data),
  updateRestaurant: (id: string, p: RestaurantPayload) => api.put<Restaurant>(`${base}/${id}`, p).then((r) => r.data),
  setAcceptingOrders: (id: string, value: boolean) =>
    api.patch<Restaurant>(`${base}/${id}/accepting-orders`, null, { params: { value } }).then((r) => r.data),

  menu: (id: string) => api.get<MenuItem[]>(`${base}/${id}/menu-items`).then((r) => r.data),
  createItem: (id: string, p: MenuItemPayload) => api.post<MenuItem>(`${base}/${id}/menu-items`, p).then((r) => r.data),
  updateItem: (id: string, itemId: string, p: MenuItemPayload) =>
    api.put<MenuItem>(`${base}/${id}/menu-items/${itemId}`, p).then((r) => r.data),
  setItemAvailable: (id: string, itemId: string, value: boolean) =>
    api.patch<MenuItem>(`${base}/${id}/menu-items/${itemId}/available`, null, { params: { value } }).then((r) => r.data),
  deleteItem: (id: string, itemId: string) => api.delete(`${base}/${id}/menu-items/${itemId}`),

  /** Gets a pre-signed URL, uploads the file straight to S3, then attaches it. */
  async uploadImage(id: string, target: ImageTarget, file: File, menuItemId?: string) {
    const { data: ticket } = await api.post<{ uploadUrl: string; key: string }>(`${base}/${id}/images/upload-url`, {
      target,
      contentType: file.type,
    })
    await axios.put(ticket.uploadUrl, file, { headers: { 'Content-Type': file.type } })
    const { data } = await api.put<{ url: string }>(`${base}/${id}/images`, { target, key: ticket.key, menuItemId })
    return data.url
  },
}

export interface KitchenOrder {
  orderId: number
  restaurantId: string
  customerName: string
  items: { menuItemId: string; name: string; quantity: number; unitPrice: number }[]
  total: number
  notes: string | null
  status: 'NEW' | 'ACCEPTED' | 'READY' | 'REJECTED' | 'CANCELLED'
  rejectReason: string | null
  prepMinutes: number | null
  receivedAt: string
  acceptedAt: string | null
  readyAt: string | null
}

export const kitchenApi = {
  orders: (id: string, active = true) =>
    api.get<KitchenOrder[]>(`${base}/${id}/orders`, { params: { active } }).then((r) => r.data),
  accept: (id: string, orderId: number, prepMinutes: number) =>
    api.post<KitchenOrder>(`${base}/${id}/orders/${orderId}/accept`, { prepMinutes }).then((r) => r.data),
  reject: (id: string, orderId: number, reason: string) =>
    api.post<KitchenOrder>(`${base}/${id}/orders/${orderId}/reject`, { reason }).then((r) => r.data),
  ready: (id: string, orderId: number) =>
    api.post<KitchenOrder>(`${base}/${id}/orders/${orderId}/ready`).then((r) => r.data),
}
