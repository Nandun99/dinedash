import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { adminApi, type ApprovalStatus } from '../../api/admin'
import Button from '../../components/ui/Button'

const filters: { value?: ApprovalStatus; label: string }[] = [
  { value: 'PENDING_APPROVAL', label: 'Pending' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'SUSPENDED', label: 'Suspended' },
  { value: undefined, label: 'All' },
]

export default function AdminRestaurants() {
  const qc = useQueryClient()
  const [status, setStatus] = useState<ApprovalStatus | undefined>('PENDING_APPROVAL')
  const { data } = useQuery({ queryKey: ['admin', 'restaurants', status], queryFn: () => adminApi.restaurants(status) })
  const update = useMutation({
    mutationFn: ({ id, value }: { id: string; value: ApprovalStatus }) => adminApi.setApproval(id, value),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'restaurants'] }),
  })

  return (
    <div>
      <div className="flex gap-2">
        {filters.map((f) => (
          <button key={f.label} onClick={() => setStatus(f.value)} className={`rounded-full border px-3 py-1 text-sm ${status === f.value ? 'border-brand-500 bg-brand-50' : 'border-stone-300 bg-white'}`}>
            {f.label}
          </button>
        ))}
      </div>
      <ul className="mt-4 space-y-3">
        {data?.content.length === 0 && <li className="text-sm text-stone-500">Nothing here.</li>}
        {data?.content.map((r) => (
          <li key={r.id} className="flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-white p-4 shadow-sm">
            <div>
              <p className="font-semibold">{r.name}</p>
              <p className="text-sm text-stone-500">{r.address.street}, {r.address.city} · {r.phone} · owner #{r.ownerId}</p>
            </div>
            <div className="flex gap-2">
              {r.approvalStatus !== 'ACTIVE' && <Button onClick={() => update.mutate({ id: r.id, value: 'ACTIVE' })}>Approve</Button>}
              {r.approvalStatus !== 'SUSPENDED' && (
                <Button variant="danger" onClick={() => update.mutate({ id: r.id, value: 'SUSPENDED' })}>Suspend</Button>
              )}
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
