import { useState, type FormEvent } from 'react'
import type { MenuItemPayload } from '../../api/owner'
import Button from '../../components/ui/Button'
import TextField from '../../components/ui/TextField'
import type { MenuItem } from '../../types/restaurant'

interface Props {
  initial?: MenuItem
  submitting: boolean
  onSubmit: (payload: MenuItemPayload, image: File | null) => void
  onCancel: () => void
}

export default function MenuItemForm({ initial, submitting, onSubmit, onCancel }: Props) {
  const [name, setName] = useState(initial?.name ?? '')
  const [description, setDescription] = useState(initial?.description ?? '')
  const [category, setCategory] = useState(initial?.category ?? '')
  const [price, setPrice] = useState(String(initial?.price ?? ''))
  const [vegetarian, setVegetarian] = useState(initial?.vegetarian ?? false)
  const [spicy, setSpicy] = useState(initial?.spicy ?? false)
  const [image, setImage] = useState<File | null>(null)

  const submit = (e: FormEvent) => {
    e.preventDefault()
    onSubmit({ name, description: description || undefined, category, price: Number(price), vegetarian, spicy }, image)
  }

  return (
    <form onSubmit={submit} className="grid gap-4 rounded-2xl bg-white p-5 shadow-sm sm:grid-cols-2">
      <TextField label="Name" required value={name} onChange={(e) => setName(e.target.value)} />
      <TextField label="Category" placeholder="e.g. Kottu, Rice, Drinks" required value={category} onChange={(e) => setCategory(e.target.value)} />
      <TextField label="Price (LKR)" type="number" min={1} step="0.01" required value={price} onChange={(e) => setPrice(e.target.value)} />
      <div className="space-y-1">
        <label className="block text-sm font-medium text-stone-700">Photo</label>
        <input type="file" accept="image/jpeg,image/png,image/webp" onChange={(e) => setImage(e.target.files?.[0] ?? null)} className="text-sm" />
      </div>
      <div className="sm:col-span-2">
        <label className="block text-sm font-medium text-stone-700">Description</label>
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={2} className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm" />
      </div>
      <div className="flex gap-6 text-sm">
        <label className="flex items-center gap-2"><input type="checkbox" checked={vegetarian} onChange={(e) => setVegetarian(e.target.checked)} className="accent-brand-600" /> Vegetarian</label>
        <label className="flex items-center gap-2"><input type="checkbox" checked={spicy} onChange={(e) => setSpicy(e.target.checked)} className="accent-brand-600" /> Spicy</label>
      </div>
      <div className="flex gap-3 sm:col-span-2">
        <Button type="submit" loading={submitting}>{initial ? 'Save item' : 'Add item'}</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  )
}
