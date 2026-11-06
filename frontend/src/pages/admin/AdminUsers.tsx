import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { adminApi } from '../../api/admin'
import type { Role } from '../../types/auth'
import { formatDateTime } from '../../utils/format'

const roles: (Role | undefined)[] = [undefined, 'CUSTOMER', 'RESTAURANT_OWNER', 'DRIVER', 'ADMIN']

export default function AdminUsers() {
  const qc = useQueryClient()
  const [role, setRole] = useState<Role | undefined>()
  const [page, setPage] = useState(0)
  const { data } = useQuery({ queryKey: ['admin', 'users', role, page], queryFn: () => adminApi.users(role, page) })
  const toggle = useMutation({
    mutationFn: ({ id, value }: { id: number; value: boolean }) => adminApi.setUserEnabled(id, value),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'users'] }),
  })

  return (
    <div>
      <select value={role ?? ''} onChange={(e) => { setPage(0); setRole((e.target.value || undefined) as Role | undefined) }} className="rounded-lg border border-stone-300 px-3 py-1.5 text-sm">
        {roles.map((r) => <option key={r ?? 'all'} value={r ?? ''}>{r ? r.replace('_', ' ').toLowerCase() : 'All roles'}</option>)}
      </select>
      <table className="mt-4 w-full overflow-hidden rounded-2xl bg-white text-sm shadow-sm">
        <thead className="bg-stone-50 text-left text-xs uppercase text-stone-500">
          <tr><th className="p-3">Name</th><th className="p-3">Email</th><th className="p-3">Role</th><th className="p-3">Joined</th><th className="p-3" /></tr>
        </thead>
        <tbody className="divide-y divide-stone-100">
          {data?.content.map((u) => (
            <tr key={u.id} className={u.enabled ? '' : 'text-stone-400'}>
              <td className="p-3">{u.fullName}</td>
              <td className="p-3">{u.email}</td>
              <td className="p-3 capitalize">{u.role.replace('_', ' ').toLowerCase()}</td>
              <td className="p-3">{formatDateTime(u.createdAt)}</td>
              <td className="p-3 text-right">
                {u.role !== 'ADMIN' && (
                  <button onClick={() => toggle.mutate({ id: u.id, value: !u.enabled })} className={u.enabled ? 'text-red-600 hover:underline' : 'text-brand-600 hover:underline'}>
                    {u.enabled ? 'Disable' : 'Enable'}
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {data && data.totalPages > 1 && (
        <div className="mt-4 flex justify-center gap-3 text-sm">
          <button disabled={page === 0} onClick={() => setPage((p) => p - 1)} className="disabled:opacity-40">Previous</button>
          <span>{page + 1} / {data.totalPages}</span>
          <button disabled={page + 1 >= data.totalPages} onClick={() => setPage((p) => p + 1)} className="disabled:opacity-40">Next</button>
        </div>
      )}
    </div>
  )
}
