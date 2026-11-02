import type { Delivery, DriverProfile, VehicleType } from '../types/delivery'
import { api } from './client'

type Coords = { latitude: number; longitude: number }

export const driverApi = {
  me: () => api.get<DriverProfile>('/drivers/me').then((r) => r.data),
  saveProfile: (p: { name: string; phone?: string; vehicleType: VehicleType; vehiclePlate: string }) =>
    api.put<DriverProfile>('/drivers/me', p).then((r) => r.data),
  online: (c: Coords) => api.post<DriverProfile>('/drivers/me/online', c).then((r) => r.data),
  offline: () => api.post<DriverProfile>('/drivers/me/offline').then((r) => r.data),
  location: (c: Coords) => api.put('/drivers/me/location', c),
  current: () => api.get<Delivery | ''>('/drivers/me/deliveries/current').then((r) => (r.data ? r.data : null)),
  history: () => api.get<Delivery[]>('/drivers/me/deliveries').then((r) => r.data),
  pickUp: (id: string) => api.post<Delivery>(`/drivers/me/deliveries/${id}/pickup`).then((r) => r.data),
  complete: (id: string) => api.post<Delivery>(`/drivers/me/deliveries/${id}/complete`).then((r) => r.data),
}

export const trackingApi = {
  forOrder: (orderId: number | string) => api.get<Delivery>(`/deliveries/orders/${orderId}`).then((r) => r.data),
}
