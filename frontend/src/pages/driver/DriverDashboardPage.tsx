import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { useEffect } from 'react'
import { errorMessage } from '../../api/client'
import { driverApi } from '../../api/delivery'
import { useAuth } from '../../auth/useAuth'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import type { Delivery } from '../../types/delivery'
import { formatDateTime } from '../../utils/format'
import DriverProfileForm from './DriverProfileForm'

const mapsLink = (lat: number, lng: number) => `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`

function currentPosition(): Promise<{ latitude: number; longitude: number }> {
  return new Promise((resolve, reject) =>
    navigator.geolocation.getCurrentPosition(
      (p) => resolve({ latitude: p.coords.latitude, longitude: p.coords.longitude }),
      reject,
      { enableHighAccuracy: true, timeout: 10_000 },
    ),
  )
}

export default function DriverDashboardPage() {
  const { user } = useAuth()
  const qc = useQueryClient()
  const profile = useQuery({
    queryKey: ['driver', 'me'],
    queryFn: driverApi.me,
    retry: (n, err) => !(axios.isAxiosError(err) && err.response?.status === 404) && n < 2,
  })
  const online = profile.data && profile.data.status !== 'OFFLINE'
  const current = useQuery({ queryKey: ['driver', 'current'], queryFn: driverApi.current, enabled: !!online, refetchInterval: 5_000 })
  const history = useQuery({ queryKey: ['driver', 'history'], queryFn: driverApi.history, enabled: !!profile.data })
  const refresh = () => qc.invalidateQueries({ queryKey: ['driver'] })

  // While online, share location so dispatch can find us and customers can track us
  useEffect(() => {
    if (!online || !navigator.geolocation) return
    let last = 0
    const watch = navigator.geolocation.watchPosition(
      (p) => {
        if (Date.now() - last < 8_000) return
        last = Date.now()
        driverApi.location({ latitude: p.coords.latitude, longitude: p.coords.longitude }).catch(() => undefined)
      },
      undefined,
      { enableHighAccuracy: true },
    )
    return () => navigator.geolocation.clearWatch(watch)
  }, [online])

  const save = useMutation({ mutationFn: driverApi.saveProfile, onSuccess: refresh })
  const toggle = useMutation({
    mutationFn: async () => (online ? driverApi.offline() : driverApi.online(await currentPosition())),
    onSuccess: refresh,
  })
  const pickUp = useMutation({ mutationFn: (d: Delivery) => driverApi.pickUp(d.id), onSuccess: refresh })
  const complete = useMutation({ mutationFn: (d: Delivery) => driverApi.complete(d.id), onSuccess: refresh })
  const error = toggle.error ?? pickUp.error ?? complete.error ?? save.error

  const needsProfile = axios.isAxiosError(profile.error) && profile.error.response?.status === 404
  const d = current.data

  return (
    <section className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="text-2xl font-bold">Driver</h1>
      {error && <div className="mt-4"><Alert>{errorMessage(error, 'Could not get your location')}</Alert></div>}
      {needsProfile && (
        <div className="mt-6"><DriverProfileForm defaultName={user?.fullName ?? ''} submitting={save.isPending} onSubmit={save.mutate} /></div>
      )}

      {profile.data && (
        <div className="mt-6 flex items-center justify-between rounded-2xl bg-white p-5 shadow-sm">
          <div>
            <p className="font-semibold">{profile.data.name}</p>
            <p className="text-sm text-stone-500">{profile.data.vehicleType.toLowerCase()} · {profile.data.vehiclePlate}</p>
          </div>
          <Button variant={online ? 'secondary' : 'primary'} loading={toggle.isPending} disabled={profile.data.status === 'BUSY'} onClick={() => toggle.mutate()}>
            {online ? 'Go offline' : 'Go online'}
          </Button>
        </div>
      )}

      {online && !d && <p className="mt-6 text-center text-stone-500">Waiting for a delivery near you…</p>}

      {d && (
        <div className="mt-6 space-y-4 rounded-2xl bg-white p-5 shadow-sm ring-2 ring-brand-500">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold">Order #{d.orderId}</h2>
            <span className="rounded-full bg-brand-100 px-2 py-0.5 text-xs text-brand-700">{d.status.replace('_', ' ')}</span>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <p className="text-xs uppercase text-stone-500">Pick up</p>
              <p className="font-medium">{d.restaurantName}</p>
              <a href={mapsLink(d.pickupLat, d.pickupLng)} target="_blank" rel="noreferrer" className="text-sm text-brand-600 hover:underline">Navigate</a>
            </div>
            <div>
              <p className="text-xs uppercase text-stone-500">Drop off</p>
              <p className="font-medium">{d.customerName}</p>
              <p className="text-sm text-stone-600">{d.dropoffAddress}</p>
              {d.customerPhone && <a href={`tel:${d.customerPhone}`} className="mr-3 text-sm text-brand-600 hover:underline">Call</a>}
              <a href={mapsLink(d.dropoffLat, d.dropoffLng)} target="_blank" rel="noreferrer" className="text-sm text-brand-600 hover:underline">Navigate</a>
            </div>
          </div>
          {d.notes && <p className="rounded bg-amber-50 px-2 py-1 text-xs text-amber-800">Note: {d.notes}</p>}
          {d.status === 'ASSIGNED' && <Button className="w-full" loading={pickUp.isPending} onClick={() => pickUp.mutate(d)}>Picked up</Button>}
          {d.status === 'PICKED_UP' && <Button className="w-full" loading={complete.isPending} onClick={() => complete.mutate(d)}>Delivered</Button>}
        </div>
      )}

      {history.data && history.data.length > 0 && (
        <div className="mt-8">
          <h2 className="font-semibold">Recent deliveries</h2>
          <ul className="mt-3 divide-y divide-stone-200 rounded-2xl bg-white shadow-sm">
            {history.data.map((h) => (
              <li key={h.id} className="flex justify-between p-3 text-sm">
                <span>#{h.orderId} · {h.restaurantName}</span>
                <span className="text-stone-500">{h.deliveredAt ? formatDateTime(h.deliveredAt) : h.status}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}
