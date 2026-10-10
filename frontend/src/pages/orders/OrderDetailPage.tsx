import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../../api/client'
import { orderApi } from '../../api/orders'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import { statusLabel } from '../../types/order'
import { formatDateTime, formatPrice } from '../../utils/format'
import StatusBadge from './StatusBadge'

const ACTIVE = ['PENDING_PAYMENT', 'PAID', 'ACCEPTED', 'READY_FOR_PICKUP', 'PICKED_UP']

export default function OrderDetailPage() {
  const { id = '' } = useParams()
  const qc = useQueryClient()
  const { data: order, isError } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderApi.get(id),
    // Poll while the order is moving; live push notifications come later
    refetchInterval: (q) => (q.state.data && ACTIVE.includes(q.state.data.status) ? 10_000 : false),
  })
  const cancel = useMutation({
    mutationFn: () => orderApi.cancel(Number(id)),
    onSuccess: (o) => qc.setQueryData(['order', id], o),
  })

  if (isError) return <div className="mx-auto max-w-3xl p-8"><Alert>Order not found.</Alert></div>
  if (!order) return <p className="p-8 text-stone-500">Loading…</p>
  const cancellable = ['PENDING_PAYMENT', 'PAYMENT_FAILED', 'PAID'].includes(order.status)

  return (
    <section className="mx-auto max-w-3xl px-4 py-8">
      <Link to="/orders" className="text-sm text-brand-600 hover:underline">← My orders</Link>
      <div className="mt-2 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Order #{order.id}</h1>
          <p className="text-sm text-stone-500">{order.restaurantName} · {formatDateTime(order.placedAt)}</p>
        </div>
        <StatusBadge status={order.status} />
      </div>

      {order.cancelReason && <div className="mt-4"><Alert kind="info">{order.cancelReason}</Alert></div>}
      {cancel.isError && <div className="mt-4"><Alert>{errorMessage(cancel.error)}</Alert></div>}
      {order.driverName && <p className="mt-4 text-sm">Your driver: <span className="font-medium">{order.driverName}</span></p>}

      <div className="mt-6 grid gap-6 md:grid-cols-2">
        <div className="rounded-2xl bg-white p-5 shadow-sm">
          <h2 className="font-semibold">Items</h2>
          <ul className="mt-3 space-y-2 text-sm">
            {order.items.map((i) => (
              <li key={i.menuItemId} className="flex justify-between">
                <span>{i.quantity} × {i.name}</span><span>{formatPrice(i.lineTotal)}</span>
              </li>
            ))}
          </ul>
          <div className="mt-3 space-y-1 border-t border-stone-200 pt-3 text-sm">
            <div className="flex justify-between"><span>Delivery</span><span>{formatPrice(order.deliveryFee)}</span></div>
            <div className="flex justify-between font-semibold"><span>Total</span><span>{formatPrice(order.total)}</span></div>
          </div>
          <p className="mt-3 text-xs text-stone-500">Deliver to: {order.deliveryAddress}</p>
        </div>
        <div className="rounded-2xl bg-white p-5 shadow-sm">
          <h2 className="font-semibold">Progress</h2>
          <ol className="mt-3 space-y-3 border-l-2 border-brand-100 pl-4 text-sm">
            {order.history.map((h, idx) => (
              <li key={idx}>
                <p className="font-medium">{statusLabel[h.status]}</p>
                <p className="text-xs text-stone-500">{formatDateTime(h.at)}{h.note ? ` · ${h.note}` : ''}</p>
              </li>
            ))}
          </ol>
        </div>
      </div>

      {cancellable && (
        <div className="mt-6 flex gap-3">
          {['PENDING_PAYMENT', 'PAYMENT_FAILED'].includes(order.status) && (
            <Link to={`/orders/${order.id}/pay`} className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700">
              {order.status === 'PAYMENT_FAILED' ? 'Try another card' : 'Pay now'}
            </Link>
          )}
          <Button variant="danger" loading={cancel.isPending} onClick={() => confirm('Cancel this order?') && cancel.mutate()}>
            Cancel order
          </Button>
        </div>
      )}
    </section>
  )
}
