import { useQuery } from '@tanstack/react-query'
import { useMemo } from 'react'
import { useParams } from 'react-router-dom'
import { restaurantApi } from '../../api/restaurants'
import Alert from '../../components/ui/Alert'
import type { MenuItem } from '../../types/restaurant'
import { formatPrice } from '../../utils/format'
import MenuItemRow from './MenuItemRow'

export default function RestaurantDetailPage() {
  const { id = '' } = useParams()
  const restaurant = useQuery({ queryKey: ['restaurant', id], queryFn: () => restaurantApi.get(id) })
  const menu = useQuery({ queryKey: ['menu', id], queryFn: () => restaurantApi.menu(id) })

  const byCategory = useMemo(() => {
    const groups = new Map<string, MenuItem[]>()
    for (const item of menu.data ?? []) {
      groups.set(item.category, [...(groups.get(item.category) ?? []), item])
    }
    return [...groups.entries()]
  }, [menu.data])

  if (restaurant.isError) return <div className="mx-auto max-w-4xl p-8"><Alert>Restaurant not found.</Alert></div>
  if (!restaurant.data) return <p className="p-8 text-stone-500">Loading…</p>
  const r = restaurant.data

  return (
    <section className="mx-auto max-w-4xl px-4 pb-16">
      <div className="h-48 overflow-hidden rounded-b-3xl bg-stone-200 sm:h-64">
        {r.coverImageUrl && <img src={r.coverImageUrl} alt="" className="h-full w-full object-cover" />}
      </div>
      <div className="-mt-10 flex items-end gap-4 px-2">
        <div className="h-20 w-20 overflow-hidden rounded-2xl bg-white ring-4 ring-white">
          {r.logoUrl && <img src={r.logoUrl} alt="" className="h-full w-full object-cover" />}
        </div>
        <div className="pb-1">
          <h1 className="text-2xl font-bold">{r.name}</h1>
          <p className="text-sm capitalize text-stone-500">{r.cuisines.join(' · ')} · {r.address.city}</p>
        </div>
      </div>

      {r.description && <p className="mt-4 text-stone-700">{r.description}</p>}
      <div className="mt-4 flex flex-wrap gap-2 text-xs">
        <span className={`rounded-full px-3 py-1 ${r.openNow ? 'bg-green-100 text-green-700' : 'bg-stone-200 text-stone-600'}`}>
          {r.openNow ? 'Open now' : 'Not taking orders'}
        </span>
        {r.opensAt && r.closesAt && (
          <span className="rounded-full bg-stone-100 px-3 py-1">{r.opensAt.slice(0, 5)} – {r.closesAt.slice(0, 5)}</span>
        )}
        <span className="rounded-full bg-stone-100 px-3 py-1">Delivery {formatPrice(r.deliveryFee)}</span>
        {r.minimumOrder > 0 && <span className="rounded-full bg-stone-100 px-3 py-1">Min. order {formatPrice(r.minimumOrder)}</span>}
      </div>

      <div className="mt-8 space-y-8">
        {menu.isLoading && <p className="text-stone-500">Loading menu…</p>}
        {byCategory.map(([category, items]) => (
          <div key={category}>
            <h2 className="mb-3 text-lg font-semibold">{category}</h2>
            <div className="grid gap-3 sm:grid-cols-2">
              {items.map((item) => <MenuItemRow key={item.id} item={item} />)}
            </div>
          </div>
        ))}
        {menu.data?.length === 0 && <p className="text-stone-500">This restaurant has not added a menu yet.</p>}
      </div>
    </section>
  )
}
