import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { adminApi } from '../../api/admin'
import { statusLabel, type OrderStatus } from '../../types/order'
import { formatDateTime, formatPrice } from '../../utils/format'
import StatusBadge from '../orders/StatusBadge'

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl bg-white p-5 shadow-sm">
      <p className="text-xs uppercase tracking-wide text-stone-500">{label}</p>
      <p className="mt-1 text-2xl font-bold">{value}</p>
    </div>
  )
}

export default function AdminOverview() {
  const stats = useQuery({ queryKey: ['admin', 'stats'], queryFn: adminApi.stats, refetchInterval: 30_000 })
  const orders = useQuery({ queryKey: ['admin', 'orders'], queryFn: () => adminApi.orders(), refetchInterval: 30_000 })
  const s = stats.data
  const live = s ? (['PAID', 'ACCEPTED', 'READY_FOR_PICKUP', 'PICKED_UP'] as OrderStatus[]).reduce((n, k) => n + (s.byStatus[k] ?? 0), 0) : 0

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-3">
        <Stat label="Orders today" value={s?.ordersToday ?? '–'} />
        <Stat label="In progress" value={s ? live : '–'} />
        <Stat label="Delivered revenue today" value={s ? formatPrice(s.deliveredRevenueToday) : '–'} />
      </div>
      {s && (
        <div className="flex flex-wrap gap-2 text-xs">
          {Object.entries(s.byStatus).map(([k, v]) => (
            <span key={k} className="rounded-full bg-white px-3 py-1 shadow-sm">{statusLabel[k as OrderStatus]}: {v}</span>
          ))}
        </div>
      )}
      <div className="rounded-2xl bg-white shadow-sm">
        <h2 className="border-b border-stone-100 px-5 py-3 font-semibold">Latest orders</h2>
        <ul className="divide-y divide-stone-100">
          {orders.data?.content.map((o) => (
            <li key={o.id} className="flex items-center justify-between px-5 py-3 text-sm">
              <Link to={`/orders/${o.id}`} className="hover:text-brand-600">#{o.id} · {o.restaurantName}</Link>
              <span className="flex items-center gap-3">
                <span className="text-stone-500">{formatDateTime(o.placedAt)}</span>
                <span className="font-medium">{formatPrice(o.total)}</span>
                <StatusBadge status={o.status} />
              </span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
