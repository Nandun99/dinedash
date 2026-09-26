import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage } from '../../api/client'
import { ownerApi, type RestaurantPayload } from '../../api/owner'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import RestaurantForm from './RestaurantForm'

export default function OwnerDashboardPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data: restaurants, isLoading } = useQuery({ queryKey: ['owner', 'restaurants'], queryFn: ownerApi.restaurants })

  const create = useMutation({
    mutationFn: (p: RestaurantPayload) => ownerApi.createRestaurant(p),
    onSuccess: () => {
      setCreating(false)
      qc.invalidateQueries({ queryKey: ['owner', 'restaurants'] })
    },
  })
  const toggle = useMutation({
    mutationFn: ({ id, value }: { id: string; value: boolean }) => ownerApi.setAcceptingOrders(id, value),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['owner', 'restaurants'] }),
  })

  return (
    <section className="mx-auto max-w-5xl px-4 py-8">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">My restaurants</h1>
        {!creating && <Button onClick={() => setCreating(true)}>Add restaurant</Button>}
      </div>

      {creating && (
        <div className="mt-6 rounded-2xl bg-white p-6 shadow-sm">
          {create.isError && <div className="mb-4"><Alert>{errorMessage(create.error)}</Alert></div>}
          <RestaurantForm submitting={create.isPending} onSubmit={create.mutate} onCancel={() => setCreating(false)} />
        </div>
      )}

      {isLoading && <p className="mt-6 text-stone-500">Loading…</p>}
      {restaurants?.length === 0 && !creating && (
        <p className="mt-6 text-stone-500">You have not listed a restaurant yet.</p>
      )}

      <div className="mt-6 space-y-4">
        {restaurants?.map((r) => (
          <div key={r.id} className="flex flex-wrap items-center justify-between gap-4 rounded-2xl bg-white p-5 shadow-sm">
            <div>
              <h2 className="font-semibold">{r.name}</h2>
              <p className="text-sm text-stone-500">{r.address.street}, {r.address.city}</p>
            </div>
            <div className="flex items-center gap-3">
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={r.acceptingOrders}
                  onChange={(e) => toggle.mutate({ id: r.id, value: e.target.checked })}
                  className="h-4 w-4 accent-brand-600"
                />
                Accepting orders
              </label>
              <Link to={`/owner/restaurants/${r.id}/orders`} className="rounded-lg bg-brand-600 px-3 py-1.5 text-sm text-white hover:bg-brand-700">
                Orders
              </Link>
              <Link to={`/owner/restaurants/${r.id}/menu`} className="rounded-lg border border-stone-300 px-3 py-1.5 text-sm hover:bg-stone-100">
                Manage menu
              </Link>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
