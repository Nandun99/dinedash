import { useState, type FormEvent } from 'react'
import Button from '../../components/ui/Button'
import TextField from '../../components/ui/TextField'
import type { VehicleType } from '../../types/delivery'

interface Props {
  defaultName: string
  submitting: boolean
  onSubmit: (p: { name: string; phone?: string; vehicleType: VehicleType; vehiclePlate: string }) => void
}

export default function DriverProfileForm({ defaultName, submitting, onSubmit }: Props) {
  const [name, setName] = useState(defaultName)
  const [phone, setPhone] = useState('')
  const [vehicleType, setVehicleType] = useState<VehicleType>('MOTORBIKE')
  const [vehiclePlate, setVehiclePlate] = useState('')

  const submit = (e: FormEvent) => {
    e.preventDefault()
    onSubmit({ name, phone: phone || undefined, vehicleType, vehiclePlate })
  }

  return (
    <form onSubmit={submit} className="space-y-4 rounded-2xl bg-white p-6 shadow-sm">
      <h2 className="font-semibold">Set up your driver profile</h2>
      <TextField label="Name" required value={name} onChange={(e) => setName(e.target.value)} />
      <TextField label="Phone" value={phone} onChange={(e) => setPhone(e.target.value)} />
      <div className="space-y-1">
        <label className="block text-sm font-medium text-stone-700">Vehicle</label>
        <select value={vehicleType} onChange={(e) => setVehicleType(e.target.value as VehicleType)} className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
          <option value="MOTORBIKE">Motorbike</option>
          <option value="BICYCLE">Bicycle</option>
          <option value="CAR">Car</option>
        </select>
      </div>
      <TextField label="Vehicle number" required placeholder="WP BAB-1234" value={vehiclePlate} onChange={(e) => setVehiclePlate(e.target.value)} />
      <Button type="submit" loading={submitting}>Save profile</Button>
    </form>
  )
}
