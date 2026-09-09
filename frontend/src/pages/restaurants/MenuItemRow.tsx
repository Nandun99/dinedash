import type { ReactNode } from 'react'
import type { MenuItem } from '../../types/restaurant'
import { formatPrice } from '../../utils/format'

export default function MenuItemRow({ item, action }: { item: MenuItem; action?: ReactNode }) {
  return (
    <div className="flex gap-4 rounded-xl bg-white p-4 ring-1 ring-stone-200">
      <div className="flex-1">
        <h4 className="font-medium">
          {item.name}
          {item.vegetarian && <span className="ml-2 rounded bg-green-100 px-1.5 text-xs text-green-700">Veg</span>}
          {item.spicy && <span className="ml-1 rounded bg-red-100 px-1.5 text-xs text-red-700">Spicy</span>}
        </h4>
        {item.description && <p className="mt-1 line-clamp-2 text-sm text-stone-500">{item.description}</p>}
        <p className="mt-2 text-sm font-semibold">{formatPrice(item.price)}</p>
      </div>
      <div className="flex flex-col items-end justify-between gap-2">
        {item.imageUrl && <img src={item.imageUrl} alt="" className="h-20 w-20 rounded-lg object-cover" loading="lazy" />}
        {action}
      </div>
    </div>
  )
}
