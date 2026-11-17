import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../../api/client'
import { ownerApi, type MenuItemPayload } from '../../api/owner'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import type { MenuItem } from '../../types/restaurant'
import MenuItemRow from '../restaurants/MenuItemRow'
import MenuItemForm from './MenuItemForm'

export default function OwnerMenuPage() {
  const { id = '' } = useParams()
  const qc = useQueryClient()
  const [editing, setEditing] = useState<MenuItem | 'new' | null>(null)
  const key = ['owner', 'menu', id]
  const { data: items, isLoading } = useQuery({ queryKey: key, queryFn: () => ownerApi.menu(id) })
  // Also drop the customer-facing menu cached in this browser
  const refresh = () => {
    qc.invalidateQueries({ queryKey: key })
    qc.invalidateQueries({ queryKey: ['menu', id] })
  }

  const save = useMutation({
    mutationFn: async ({ payload, image }: { payload: MenuItemPayload; image: File | null }) => {
      const item = editing && editing !== 'new'
        ? await ownerApi.updateItem(id, editing.id, payload)
        : await ownerApi.createItem(id, payload)
      if (image) await ownerApi.uploadImage(id, 'MENU_ITEM', image, item.id)
      return item
    },
    onSuccess: () => {
      setEditing(null)
      refresh()
    },
  })
  const toggle = useMutation({
    mutationFn: (item: MenuItem) => ownerApi.setItemAvailable(id, item.id, !item.available),
    onSuccess: refresh,
  })
  const remove = useMutation({ mutationFn: (item: MenuItem) => ownerApi.deleteItem(id, item.id), onSuccess: refresh })

  const uploadBrand = async (target: 'LOGO' | 'COVER', file?: File) => {
    if (file) await ownerApi.uploadImage(id, target, file)
    qc.invalidateQueries({ queryKey: ['owner', 'restaurants'] })
  }

  return (
    <section className="mx-auto max-w-5xl px-4 py-8">
      <Link to="/owner" className="text-sm text-brand-600 hover:underline">← My restaurants</Link>
      <div className="mt-2 flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Menu</h1>
        <div className="flex flex-wrap gap-3 text-sm">
          <label className="cursor-pointer rounded-lg border border-stone-300 px-3 py-2 hover:bg-stone-100">
            Upload logo
            <input type="file" accept="image/*" className="hidden" onChange={(e) => uploadBrand('LOGO', e.target.files?.[0])} />
          </label>
          <label className="cursor-pointer rounded-lg border border-stone-300 px-3 py-2 hover:bg-stone-100">
            Upload cover
            <input type="file" accept="image/*" className="hidden" onChange={(e) => uploadBrand('COVER', e.target.files?.[0])} />
          </label>
          {!editing && <Button onClick={() => setEditing('new')}>Add item</Button>}
        </div>
      </div>

      {save.isError && <div className="mt-4"><Alert>{errorMessage(save.error)}</Alert></div>}
      {editing && (
        <div className="mt-6">
          <MenuItemForm
            key={editing === 'new' ? 'new' : editing.id}
            initial={editing === 'new' ? undefined : editing}
            submitting={save.isPending}
            onSubmit={(payload, image) => save.mutate({ payload, image })}
            onCancel={() => setEditing(null)}
          />
        </div>
      )}

      {isLoading && <p className="mt-6 text-stone-500">Loading…</p>}
      <div className="mt-6 grid gap-3 sm:grid-cols-2">
        {items?.map((item) => (
          <div key={item.id} className={item.available ? '' : 'opacity-60'}>
            <MenuItemRow
              item={item}
              action={
                <div className="flex gap-2 text-xs">
                  <button onClick={() => setEditing(item)} className="text-brand-600 hover:underline">Edit</button>
                  <button onClick={() => toggle.mutate(item)} className="hover:underline">{item.available ? 'Mark sold out' : 'Mark available'}</button>
                  <button onClick={() => confirm(`Delete ${item.name}?`) && remove.mutate(item)} className="text-red-600 hover:underline">Delete</button>
                </div>
              }
            />
          </div>
        ))}
      </div>
    </section>
  )
}
