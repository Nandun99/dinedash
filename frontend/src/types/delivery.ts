export type DeliveryStatus = 'WAITING_FOR_FOOD' | 'SEARCHING' | 'ASSIGNED' | 'PICKED_UP' | 'DELIVERED' | 'CANCELLED'

export interface Delivery {
  id: string
  orderId: number
  status: DeliveryStatus
  restaurantName: string
  pickupLat: number
  pickupLng: number
  dropoffLat: number
  dropoffLng: number
  dropoffAddress: string
  customerName: string
  customerPhone: string | null
  notes: string | null
  driverId: number | null
  driverName: string | null
  driverLat: number | null
  driverLng: number | null
  assignedAt: string | null
  pickedUpAt: string | null
  deliveredAt: string | null
}

export type VehicleType = 'BICYCLE' | 'MOTORBIKE' | 'CAR'

export interface DriverProfile {
  id: number
  name: string
  phone: string | null
  vehicleType: VehicleType
  vehiclePlate: string
  status: 'OFFLINE' | 'AVAILABLE' | 'BUSY'
  latitude: number | null
  longitude: number | null
  lastSeenAt: string | null
  currentDeliveryId: string | null
}
