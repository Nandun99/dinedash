import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/useAuth'
import { useCart } from '../../cart/useCart'
import Button from '../../components/ui/Button'
import { formatPrice } from '../../utils/format'

export default function CartPage() {
  const { cart, subtotal, setQuantity, clear } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  if (!cart) {
    return (
      <section className="mx-auto max-w-2xl px-4 py-16 text-center">
        <h1 className="text-2xl font-bold">Your cart is empty</h1>
        <Link to="/" className="mt-4 inline-block text-brand-600 hover:underline">Find something to eat</Link>
      </section>
    )
  }

  return (
    <section className="mx-auto max-w-2xl px-4 py-8">
      <h1 className="text-2xl font-bold">Your cart</h1>
      <p className="text-sm text-stone-500">
        From <Link to={`/restaurants/${cart.restaurantId}`} className="text-brand-600 hover:underline">{cart.restaurantName}</Link>
      </p>
      <ul className="mt-6 divide-y divide-stone-200 rounded-2xl bg-white shadow-sm">
        {cart.lines.map((line) => (
          <li key={line.menuItemId} className="flex items-center justify-between gap-4 p-4">
            <div>
              <p className="font-medium">{line.name}</p>
              <p className="text-sm text-stone-500">{formatPrice(line.price)}</p>
            </div>
            <div className="flex items-center gap-2">
              <button onClick={() => setQuantity(line.menuItemId, line.quantity - 1)} className="h-8 w-8 rounded-full border border-stone-300" aria-label="Decrease">−</button>
              <span className="w-6 text-center">{line.quantity}</span>
              <button onClick={() => setQuantity(line.menuItemId, line.quantity + 1)} className="h-8 w-8 rounded-full border border-stone-300" aria-label="Increase">+</button>
              <span className="w-24 text-right font-medium">{formatPrice(line.price * line.quantity)}</span>
            </div>
          </li>
        ))}
      </ul>
      <div className="mt-6 space-y-1 rounded-2xl bg-white p-4 text-sm shadow-sm">
        <div className="flex justify-between"><span>Subtotal</span><span>{formatPrice(subtotal)}</span></div>
        <div className="flex justify-between"><span>Delivery</span><span>{formatPrice(cart.deliveryFee)}</span></div>
        <div className="flex justify-between border-t border-stone-200 pt-2 text-base font-semibold">
          <span>Total</span><span>{formatPrice(subtotal + cart.deliveryFee)}</span>
        </div>
        <p className="text-xs text-stone-500">Final prices are confirmed by the restaurant at checkout.</p>
      </div>
      <div className="mt-6 flex justify-between">
        <Button variant="secondary" onClick={clear}>Clear cart</Button>
        <Button onClick={() => navigate(user ? '/checkout' : '/login', { state: { from: '/checkout' } })}>
          {user ? 'Checkout' : 'Log in to checkout'}
        </Button>
      </div>
    </section>
  )
}
