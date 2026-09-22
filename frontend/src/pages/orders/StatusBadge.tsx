import { statusLabel, type OrderStatus } from '../../types/order'

const tone: Partial<Record<OrderStatus, string>> = {
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-stone-200 text-stone-600',
  REJECTED: 'bg-red-100 text-red-700',
  PAYMENT_FAILED: 'bg-red-100 text-red-700',
  PENDING_PAYMENT: 'bg-amber-100 text-amber-700',
}

export default function StatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${tone[status] ?? 'bg-brand-100 text-brand-700'}`}>
      {statusLabel[status]}
    </span>
  )
}
