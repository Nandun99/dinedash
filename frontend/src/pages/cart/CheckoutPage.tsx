import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { errorMessage } from '../../api/client'
import { addressApi, orderApi } from '../../api/orders'
import { useAuth } from '../../auth/useAuth'
import { useCart } from '../../cart/useCart'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import TextField from '../../components/ui/TextField'
import { useGeolocation } from '../../hooks/useGeolocation'
import { formatPrice } from '../../utils/format'

export default function CheckoutPage() {
  const { cart, subtotal, clear } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const geo = useGeolocation()
  const addresses = useQuery({ queryKey: ['addresses'], queryFn: addressApi.list })

  const [addressId, setAddressId] = useState<number | 'new' | null>(null)
  const [newAddress, setNewAddress] = useState({ label: 'Home', line1: '', city: '' })
  const [contactName, setContactName] = useState(user?.fullName ?? '')
  const [contactPhone, setContactPhone] = useState(user?.phone ?? '')
  const [notes, setNotes] = useState('')

  useEffect(() => {
    if (addressId !== null || !addresses.data) return
    const preferred = addresses.data.find((a) => a.defaultAddress) ?? addresses.data[0]
    setAddressId(preferred ? preferred.id : 'new')
  }, [addresses.data, addressId])

  const place = useMutation({
    mutationFn: async () => {
      if (!cart) throw new Error('Cart is empty')
      let address = addresses.data?.find((a) => a.id === addressId)
      if (addressId === 'new') {
        if (!geo.coords) throw new Error('Please share your location so the driver can find you')
        address = await addressApi.create({
          ...newAddress, line2: null, latitude: geo.coords.lat, longitude: geo.coords.lng, defaultAddress: true,
        })
        qc.invalidateQueries({ queryKey: ['addresses'] })
      }
      if (!address) throw new Error('Choose a delivery address')
      return orderApi.place({
        restaurantId: cart.restaurantId,
        items: cart.lines.map((l) => ({ menuItemId: l.menuItemId, quantity: l.quantity })),
        contactName,
        contactPhone: contactPhone || undefined,
        deliveryAddress: [address.line1, address.line2, address.city].filter(Boolean).join(', '),
        latitude: address.latitude,
        longitude: address.longitude,
        notes: notes || undefined,
      })
    },
    onSuccess: (order) => {
      clear()
      navigate(`/orders/${order.id}`, { replace: true })
    },
  })

  if (!cart) return <Navigate to="/cart" replace />

  const submit = (e: FormEvent) => {
    e.preventDefault()
    place.mutate()
  }

  return (
    <section className="mx-auto max-w-2xl px-4 py-8">
      <h1 className="text-2xl font-bold">Checkout</h1>
      <form onSubmit={submit} className="mt-6 space-y-6">
        {place.isError && <Alert>{place.error instanceof Error && !('isAxiosError' in place.error) ? place.error.message : errorMessage(place.error)}</Alert>}

        <div className="space-y-3 rounded-2xl bg-white p-5 shadow-sm">
          <h2 className="font-semibold">Deliver to</h2>
          {addresses.data?.map((a) => (
            <label key={a.id} className="flex cursor-pointer items-start gap-3 text-sm">
              <input type="radio" name="address" checked={addressId === a.id} onChange={() => setAddressId(a.id)} className="mt-1 accent-brand-600" />
              <span><span className="font-medium">{a.label}</span> — {a.line1}, {a.city}</span>
            </label>
          ))}
          <label className="flex cursor-pointer items-center gap-3 text-sm">
            <input type="radio" name="address" checked={addressId === 'new'} onChange={() => setAddressId('new')} className="accent-brand-600" />
            New address
          </label>
          {addressId === 'new' && (
            <div className="grid gap-3 sm:grid-cols-2">
              <TextField label="Label" value={newAddress.label} onChange={(e) => setNewAddress({ ...newAddress, label: e.target.value })} />
              <TextField label="City" required value={newAddress.city} onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })} />
              <div className="sm:col-span-2">
                <TextField label="Street address" required value={newAddress.line1} onChange={(e) => setNewAddress({ ...newAddress, line1: e.target.value })} />
              </div>
              <div className="sm:col-span-2">
                <Button type="button" variant="secondary" loading={geo.loading} onClick={geo.locate}>
                  {geo.coords ? 'Location captured ✓' : 'Use my current location'}
                </Button>
                {geo.error && <p className="mt-1 text-xs text-red-600">{geo.error}</p>}
              </div>
            </div>
          )}
        </div>

        <div className="grid gap-3 rounded-2xl bg-white p-5 shadow-sm sm:grid-cols-2">
          <TextField label="Contact name" required value={contactName} onChange={(e) => setContactName(e.target.value)} />
          <TextField label="Phone" value={contactPhone} onChange={(e) => setContactPhone(e.target.value)} />
          <div className="sm:col-span-2">
            <TextField label="Notes for the restaurant or driver" value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>
        </div>

        <div className="flex items-center justify-between rounded-2xl bg-white p-5 shadow-sm">
          <div>
            <p className="text-sm text-stone-500">{cart.restaurantName}</p>
            <p className="text-lg font-semibold">{formatPrice(subtotal + cart.deliveryFee)}</p>
          </div>
          <Button type="submit" loading={place.isPending}>Place order</Button>
        </div>
      </form>
    </section>
  )
}
