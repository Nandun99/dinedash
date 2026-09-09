import { useState, type FormEvent } from 'react'
import type { RestaurantPayload } from '../../api/owner'
import Button from '../../components/ui/Button'
import TextField from '../../components/ui/TextField'
import type { Restaurant } from '../../types/restaurant'

interface Props {
  initial?: Restaurant
  submitting: boolean
  onSubmit: (payload: RestaurantPayload) => void
  onCancel?: () => void
}

export default function RestaurantForm({ initial, submitting, onSubmit, onCancel }: Props) {
  const [f, setF] = useState({
    name: initial?.name ?? '',
    description: initial?.description ?? '',
    cuisines: initial?.cuisines.join(', ') ?? '',
    phone: initial?.phone ?? '',
    street: initial?.address.street ?? '',
    city: initial?.address.city ?? '',
    latitude: String(initial?.latitude ?? ''),
    longitude: String(initial?.longitude ?? ''),
    opensAt: initial?.opensAt?.slice(0, 5) ?? '',
    closesAt: initial?.closesAt?.slice(0, 5) ?? '',
    deliveryFee: String(initial?.deliveryFee ?? 250),
    minimumOrder: String(initial?.minimumOrder ?? 0),
    averagePrepMinutes: String(initial?.averagePrepMinutes ?? 20),
  })
  const set = (k: keyof typeof f) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setF((prev) => ({ ...prev, [k]: e.target.value }))

  const submit = (e: FormEvent) => {
    e.preventDefault()
    onSubmit({
      name: f.name,
      description: f.description || undefined,
      cuisines: f.cuisines.split(',').map((c) => c.trim()).filter(Boolean),
      phone: f.phone,
      address: { street: f.street, city: f.city },
      latitude: Number(f.latitude),
      longitude: Number(f.longitude),
      opensAt: f.opensAt || undefined,
      closesAt: f.closesAt || undefined,
      deliveryFee: Number(f.deliveryFee),
      minimumOrder: Number(f.minimumOrder),
      averagePrepMinutes: Number(f.averagePrepMinutes),
    })
  }

  const useMyLocation = () =>
    navigator.geolocation?.getCurrentPosition((p) =>
      setF((prev) => ({ ...prev, latitude: p.coords.latitude.toFixed(6), longitude: p.coords.longitude.toFixed(6) })),
    )

  return (
    <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
      <TextField label="Name" required value={f.name} onChange={set('name')} />
      <TextField label="Cuisines (comma separated)" required value={f.cuisines} onChange={set('cuisines')} />
      <div className="sm:col-span-2">
        <label className="block text-sm font-medium text-stone-700">Description</label>
        <textarea value={f.description} onChange={set('description')} rows={3} className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm" />
      </div>
      <TextField label="Phone" required value={f.phone} onChange={set('phone')} />
      <TextField label="Street" required value={f.street} onChange={set('street')} />
      <TextField label="City" required value={f.city} onChange={set('city')} />
      <div className="grid grid-cols-2 gap-2">
        <TextField label="Latitude" required value={f.latitude} onChange={set('latitude')} />
        <TextField label="Longitude" required value={f.longitude} onChange={set('longitude')} />
        <button type="button" onClick={useMyLocation} className="col-span-2 text-left text-xs text-brand-600 hover:underline">
          Use my current location
        </button>
      </div>
      <TextField label="Opens at" type="time" value={f.opensAt} onChange={set('opensAt')} />
      <TextField label="Closes at" type="time" value={f.closesAt} onChange={set('closesAt')} />
      <TextField label="Delivery fee (LKR)" type="number" min={0} value={f.deliveryFee} onChange={set('deliveryFee')} />
      <TextField label="Minimum order (LKR)" type="number" min={0} value={f.minimumOrder} onChange={set('minimumOrder')} />
      <TextField label="Average prep time (minutes)" type="number" min={5} max={120} value={f.averagePrepMinutes} onChange={set('averagePrepMinutes')} />
      <div className="flex gap-3 sm:col-span-2">
        <Button type="submit" loading={submitting}>{initial ? 'Save changes' : 'Create restaurant'}</Button>
        {onCancel && <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>}
      </div>
    </form>
  )
}
