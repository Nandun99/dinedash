export type OrderStatus =
  | 'PENDING_PAYMENT'
  | 'PAYMENT_FAILED'
  | 'PAID'
  | 'ACCEPTED'
  | 'READY_FOR_PICKUP'
  | 'PICKED_UP'
  | 'DELIVERED'
  | 'REJECTED'
  | 'CANCELLED'

export interface OrderItem {
  menuItemId: string
  name: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export interface Order {
  id: number
  restaurantId: string
  restaurantName: string
  status: OrderStatus
  items: OrderItem[]
  subtotal: number
  deliveryFee: number
  total: number
  currency: string
  deliveryAddress: string
  dropoffLat: number
  dropoffLng: number
  pickupLat: number
  pickupLng: number
  notes: string | null
  driverId: number | null
  driverName: string | null
  cancelReason: string | null
  placedAt: string
  updatedAt: string
  history: { status: OrderStatus; note: string | null; at: string }[]
}

export interface OrderSummary {
  id: number
  restaurantName: string
  status: OrderStatus
  total: number
  itemCount: number
  placedAt: string
}

export interface PlaceOrderPayload {
  restaurantId: string
  items: { menuItemId: string; quantity: number }[]
  contactName: string
  contactPhone?: string
  deliveryAddress: string
  latitude: number
  longitude: number
  notes?: string
}

export interface Address {
  id: number
  label: string
  line1: string
  line2: string | null
  city: string
  latitude: number
  longitude: number
  defaultAddress: boolean
}

export const statusLabel: Record<OrderStatus, string> = {
  PENDING_PAYMENT: 'Waiting for payment',
  PAYMENT_FAILED: 'Payment failed',
  PAID: 'Sent to restaurant',
  ACCEPTED: 'Preparing',
  READY_FOR_PICKUP: 'Ready for pickup',
  PICKED_UP: 'On the way',
  DELIVERED: 'Delivered',
  REJECTED: 'Rejected by restaurant',
  CANCELLED: 'Cancelled',
}
