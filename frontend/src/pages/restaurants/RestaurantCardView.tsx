import { useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { cacheTimes, queryKeys } from '../../api/queryKeys'
import { restaurantApi } from '../../api/restaurants'
import type { RestaurantCard } from '../../types/restaurant'
import { formatPrice } from '../../utils/format'

export default function RestaurantCardView({ restaurant }: { restaurant: RestaurantCard }) {
  const qc = useQueryClient()
  // Warm the cache on hover so the restaurant page opens instantly
  const prefetch = () => {
    qc.prefetchQuery({ queryKey: queryKeys.restaurant(restaurant.id), queryFn: () => restaurantApi.get(restaurant.id), ...cacheTimes.restaurant })
    qc.prefetchQuery({ queryKey: queryKeys.menu(restaurant.id), queryFn: () => restaurantApi.menu(restaurant.id), ...cacheTimes.menu })
  }
  return (
    <Link
      to={`/restaurants/${restaurant.id}`}
      onMouseEnter={prefetch}
      onFocus={prefetch}
      className="group overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-stone-200 transition hover:shadow-md"
    >
      <div className="relative h-36 bg-stone-200">
        {restaurant.coverImageUrl && (
          <img src={restaurant.coverImageUrl} alt="" className="h-full w-full object-cover" loading="lazy" />
        )}
        {!restaurant.openNow && (
          <span className="absolute left-3 top-3 rounded-full bg-stone-900/80 px-2 py-0.5 text-xs text-white">Closed</span>
        )}
      </div>
      <div className="space-y-1 p-4">
        <h3 className="font-semibold group-hover:text-brand-600">{restaurant.name}</h3>
        <p className="text-xs capitalize text-stone-500">{restaurant.cuisines.join(' · ')}</p>
        <p className="flex flex-wrap gap-x-3 text-xs text-stone-600">
          <span>{restaurant.averagePrepMinutes}–{restaurant.averagePrepMinutes + 15} min</span>
          <span>{restaurant.deliveryFee > 0 ? `${formatPrice(restaurant.deliveryFee)} delivery` : 'Free delivery'}</span>
          {restaurant.distanceKm != null && <span>{restaurant.distanceKm} km</span>}
        </p>
      </div>
    </Link>
  )
}
