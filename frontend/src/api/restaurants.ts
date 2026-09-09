import type { MenuItem, Page, Restaurant, RestaurantCard, RestaurantSearch } from '../types/restaurant'
import { api } from './client'

export const restaurantApi = {
  search: (params: RestaurantSearch) =>
    api.get<Page<RestaurantCard>>('/restaurants', { params: { size: 12, ...params } }).then((r) => r.data),
  get: (id: string) => api.get<Restaurant>(`/restaurants/${id}`).then((r) => r.data),
  menu: (id: string) => api.get<MenuItem[]>(`/restaurants/${id}/menu`).then((r) => r.data),
}
