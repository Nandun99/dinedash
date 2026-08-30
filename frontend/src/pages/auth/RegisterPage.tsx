import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { errorMessage } from '../../api/client'
import { homePathFor } from '../../auth/roleRoutes'
import { useAuth } from '../../auth/useAuth'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import TextField from '../../components/ui/TextField'
import type { RegisterPayload } from '../../types/auth'

const roles: { value: RegisterPayload['role']; title: string; description: string }[] = [
  { value: 'CUSTOMER', title: 'Order food', description: 'Browse restaurants and get food delivered' },
  { value: 'RESTAURANT_OWNER', title: 'List my restaurant', description: 'Manage menus and incoming orders' },
  { value: 'DRIVER', title: 'Deliver orders', description: 'Pick up and deliver to customers' },
]

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState<RegisterPayload>({ email: '', password: '', fullName: '', phone: '', role: 'CUSTOMER' })
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const update = (field: keyof RegisterPayload) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }))

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const user = await register({ ...form, phone: form.phone || undefined })
      navigate(homePathFor(user.role), { replace: true })
    } catch (err) {
      setError(errorMessage(err, 'Could not create your account'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="mx-auto max-w-lg px-4 py-16">
      <h1 className="text-2xl font-bold">Create your account</h1>
      <form onSubmit={onSubmit} className="mt-8 space-y-4 rounded-2xl bg-white p-6 shadow-sm">
        {error && <Alert>{error}</Alert>}
        <fieldset className="grid gap-2 sm:grid-cols-3">
          <legend className="mb-2 text-sm font-medium text-stone-700">I want to</legend>
          {roles.map((r) => (
            <label
              key={r.value}
              className={`cursor-pointer rounded-xl border p-3 text-sm transition ${
                form.role === r.value ? 'border-brand-500 bg-brand-50' : 'border-stone-200 hover:border-stone-300'
              }`}
            >
              <input type="radio" name="role" value={r.value} checked={form.role === r.value} onChange={() => setForm((f) => ({ ...f, role: r.value }))} className="sr-only" />
              <span className="block font-semibold">{r.title}</span>
              <span className="text-xs text-stone-500">{r.description}</span>
            </label>
          ))}
        </fieldset>
        <TextField label="Full name" name="fullName" required value={form.fullName} onChange={update('fullName')} />
        <TextField label="Email" name="email" type="email" autoComplete="email" required value={form.email} onChange={update('email')} />
        <TextField label="Phone (optional)" name="phone" type="tel" placeholder="+94771234567" value={form.phone} onChange={update('phone')} />
        <TextField label="Password" name="password" type="password" autoComplete="new-password" minLength={8} required value={form.password} onChange={update('password')} />
        <Button type="submit" loading={loading} className="w-full">
          Sign up
        </Button>
        <p className="text-center text-sm text-stone-600">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline">
            Log in
          </Link>
        </p>
      </form>
    </section>
  )
}
