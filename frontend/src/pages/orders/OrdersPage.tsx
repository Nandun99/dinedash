import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { orderApi } from '../../api/orders'
import { formatDateTime, formatPrice } from '../../utils/format'
import StatusBadge from './StatusBadge'

export default function OrdersPage() {
  const { data, isLoading } = useQuery({ queryKey: ['orders'], queryFn: () => orderApi.mine() })

  return (
    <section className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="text-2xl font-bold">My orders</h1>
      {isLoading && <p className="mt-6 text-stone-500">Loading…</p>}
      {data?.content.length === 0 && <p className="mt-6 text-stone-500">No orders yet.</p>}
      <ul className="mt-6 space-y-3">
        {data?.content.map((o) => (
          <li key={o.id}>
            <Link to={`/orders/${o.id}`} className="flex items-center justify-between rounded-2xl bg-white p-4 shadow-sm hover:shadow-md">
              <div>
                <p className="font-medium">{o.restaurantName}</p>
                <p className="text-xs text-stone-500">#{o.id} · {formatDateTime(o.placedAt)} · {o.itemCount} items</p>
              </div>
              <div className="flex flex-col items-end gap-1">
                <StatusBadge status={o.status} />
                <span className="text-sm font-semibold">{formatPrice(o.total)}</span>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}
