export interface Page<T> {
  content: T[]
  page: number
  size: number
  hasNext: boolean
}

export interface RestaurantCard {
  id: string
  name: string
  cuisines: string[]
  city: string | null
  logoUrl: string | null
  coverImageUrl: string | null
  openNow: boolean
  deliveryFee: number
  averagePrepMinutes: number
  distanceKm: number | null
}

export interface Restaurant {
  id: string
  ownerId: number
  name: string
  description: string | null
  cuisines: string[]
  phone: string
  address: { street: string; city: string; postalCode: string | null }
  latitude: number
  longitude: number
  logoUrl: string | null
  coverImageUrl: string | null
  opensAt: string | null
  closesAt: string | null
  acceptingOrders: boolean
  openNow: boolean
  deliveryFee: number
  minimumOrder: number
  averagePrepMinutes: number
  approvalStatus?: 'PENDING_APPROVAL' | 'ACTIVE' | 'SUSPENDED' | null
}

export interface MenuItem {
  id: string
  restaurantId: string
  name: string
  description: string | null
  category: string
  price: number
  imageUrl: string | null
  available: boolean
  vegetarian: boolean
  spicy: boolean
}

export interface RestaurantSearch {
  q?: string
  cuisine?: string
  lat?: number
  lng?: number
  page?: number
}
