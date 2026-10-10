import { Elements } from '@stripe/react-stripe-js'
import { loadStripe, type Stripe } from '@stripe/stripe-js'
import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { paymentApi } from '../../api/payments'
import Alert from '../../components/ui/Alert'
import PaymentForm from './PaymentForm'

const stripeCache = new Map<string, Promise<Stripe | null>>()
const stripeFor = (key: string) => {
  if (!stripeCache.has(key)) stripeCache.set(key, loadStripe(key))
  return stripeCache.get(key)!
}

export default function PaymentPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const intent = useQuery({
    queryKey: ['payment-intent', id],
    queryFn: () => paymentApi.startPayment(id),
    staleTime: Infinity,
    // Right after checkout the payment record may not exist yet (409); retry a few times
    retry: (count, err) => axios.isAxiosError(err) && err.response?.status === 409 && count < 5,
    retryDelay: 1_000,
  })
  const stripePromise = useMemo(() => (intent.data ? stripeFor(intent.data.publishableKey) : null), [intent.data])

  return (
    <section className="mx-auto max-w-md px-4 py-10">
      <h1 className="text-2xl font-bold">Pay for order #{id}</h1>
      <div className="mt-6 rounded-2xl bg-white p-6 shadow-sm">
        {intent.isLoading && <p className="text-stone-500">Preparing secure payment…</p>}
        {intent.isError && <Alert>Could not start the payment. Please try again from your order page.</Alert>}
        {intent.data && stripePromise && (
          <Elements stripe={stripePromise} options={{ clientSecret: intent.data.clientSecret, appearance: { theme: 'stripe' } }}>
            <PaymentForm orderId={intent.data.orderId} amount={intent.data.amount} onPaid={() => navigate(`/orders/${id}`, { replace: true })} />
          </Elements>
        )}
      </div>
    </section>
  )
}
