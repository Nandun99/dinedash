import { PaymentElement, useElements, useStripe } from '@stripe/react-stripe-js'
import { useState, type FormEvent } from 'react'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import { formatPrice } from '../../utils/format'

interface Props {
  orderId: number
  amount: number
  onPaid: () => void
}

export default function PaymentForm({ orderId, amount, onPaid }: Props) {
  const stripe = useStripe()
  const elements = useElements()
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    if (!stripe || !elements) return
    setSubmitting(true)
    setError(null)
    const result = await stripe.confirmPayment({
      elements,
      confirmParams: { return_url: `${window.location.origin}/orders/${orderId}` },
      redirect: 'if_required',
    })
    setSubmitting(false)
    if (result.error) {
      setError(result.error.message ?? 'Payment failed')
      return
    }
    // The order is marked paid when Stripe's webhook reaches payment-service
    onPaid()
  }

  return (
    <form onSubmit={submit} className="space-y-4">
      {error && <Alert>{error}</Alert>}
      <PaymentElement />
      <Button type="submit" className="w-full" loading={submitting} disabled={!stripe}>
        Pay {formatPrice(amount)}
      </Button>
      <p className="text-center text-xs text-stone-500">Test mode: use card 4242 4242 4242 4242, any future date and CVC.</p>
    </form>
  )
}
