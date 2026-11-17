import type { RestaurantSearch } from '../types/restaurant'

/** Central query keys and cache lifetimes so related data is cached and invalidated consistently. */
export const queryKeys = {
  restaurants: (params: RestaurantSearch) => ['restaurants', params] as const,
  restaurant: (id: string) => ['restaurant', id] as const,
  menu: (id: string) => ['menu', id] as const,
}

const minute = 60_000

export const cacheTimes = {
  /** Listings change often (open/closed), keep them briefly. */
  restaurants: { staleTime: minute, gcTime: 10 * minute },
  /** Restaurant details and menus rarely change; the server also caches them. */
  restaurant: { staleTime: 5 * minute, gcTime: 30 * minute },
  menu: { staleTime: 5 * minute, gcTime: 30 * minute },
}
