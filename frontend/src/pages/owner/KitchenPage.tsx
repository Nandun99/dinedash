import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../../api/client'
import { kitchenApi, type KitchenOrder } from '../../api/owner'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import { formatDateTime, formatPrice } from '../../utils/format'

function minutesAgo(iso: string) {
  return Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 60_000))
}

export default function KitchenPage() {
  const { id = '' } = useParams()
  const qc = useQueryClient()
  const [showHistory, setShowHistory] = useState(false)
  const key = ['kitchen', id, showHistory]
  const { data: orders, isLoading } = useQuery({
    queryKey: key,
    queryFn: () => kitchenApi.orders(id, !showHistory),
    refetchInterval: showHistory ? false : 5_000,
  })
  const refresh = () => qc.invalidateQueries({ queryKey: ['kitchen', id] })

  const accept = useMutation({ mutationFn: (o: KitchenOrder) => kitchenApi.accept(id, o.orderId, 20), onSuccess: refresh })
  const reject = useMutation({
    mutationFn: ({ o, reason }: { o: KitchenOrder; reason: string }) => kitchenApi.reject(id, o.orderId, reason),
    onSuccess: refresh,
  })
  const ready = useMutation({ mutationFn: (o: KitchenOrder) => kitchenApi.ready(id, o.orderId), onSuccess: refresh })
  const error = accept.error ?? reject.error ?? ready.error

  return (
    <section className="mx-auto max-w-5xl px-4 py-8">
      <Link to="/owner" className="text-sm text-brand-600 hover:underline">← My restaurants</Link>
      <div className="mt-2 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{showHistory ? 'Recent orders' : 'Incoming orders'}</h1>
        <Button variant="secondary" onClick={() => setShowHistory((v) => !v)}>
          {showHistory ? 'Show active' : 'Show history'}
        </Button>
      </div>
      {error && <div className="mt-4"><Alert>{errorMessage(error)}</Alert></div>}
      {isLoading && <p className="mt-6 text-stone-500">Loading…</p>}
      {orders?.length === 0 && <p className="mt-6 text-stone-500">No orders right now.</p>}

      <div className="mt-6 grid gap-4 md:grid-cols-2">
        {orders?.map((o) => (
          <div key={o.orderId} className={`rounded-2xl bg-white p-5 shadow-sm ring-2 ${o.status === 'NEW' ? 'ring-brand-500' : 'ring-transparent'}`}>
            <div className="flex items-center justify-between">
              <h2 className="font-semibold">#{o.orderId} · {o.customerName}</h2>
              <span className="text-xs text-stone-500">
                {showHistory ? formatDateTime(o.receivedAt) : `${minutesAgo(o.receivedAt)} min ago`}
              </span>
            </div>
            <ul className="mt-3 space-y-1 text-sm">
              {o.items.map((i) => <li key={i.menuItemId}><span className="font-medium">{i.quantity}×</span> {i.name}</li>)}
            </ul>
            {o.notes && <p className="mt-2 rounded bg-amber-50 px-2 py-1 text-xs text-amber-800">Note: {o.notes}</p>}
            <div className="mt-4 flex items-center justify-between">
              <span className="text-sm font-semibold">{formatPrice(o.total)}</span>
              <div className="flex gap-2">
                {o.status === 'NEW' && (
                  <>
                    <Button variant="secondary" onClick={() => { const r = prompt('Reason for rejecting?'); if (r) reject.mutate({ o, reason: r }) }}>
                      Reject
                    </Button>
                    <Button loading={accept.isPending} onClick={() => accept.mutate(o)}>Accept</Button>
                  </>
                )}
                {o.status === 'ACCEPTED' && <Button loading={ready.isPending} onClick={() => ready.mutate(o)}>Mark ready</Button>}
                {!['NEW', 'ACCEPTED'].includes(o.status) && <span className="text-xs uppercase text-stone-500">{o.status}</span>}
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
