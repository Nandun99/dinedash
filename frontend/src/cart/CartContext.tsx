import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { MenuItem } from '../types/restaurant'

export interface CartLine {
  menuItemId: string
  name: string
  price: number
  quantity: number
}

export interface Cart {
  restaurantId: string
  restaurantName: string
  deliveryFee: number
  lines: CartLine[]
}

export interface CartContextValue {
  cart: Cart | null
  itemCount: number
  subtotal: number
  add: (restaurant: { id: string; name: string; deliveryFee: number }, item: MenuItem) => boolean
  setQuantity: (menuItemId: string, quantity: number) => void
  clear: () => void
}

const KEY = 'dinedash.cart'

export const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<Cart | null>(() => {
    const raw = localStorage.getItem(KEY)
    return raw ? (JSON.parse(raw) as Cart) : null
  })

  useEffect(() => {
    if (cart && cart.lines.length > 0) localStorage.setItem(KEY, JSON.stringify(cart))
    else localStorage.removeItem(KEY)
  }, [cart])

  // One cart = one restaurant. Returns false if the user declined to start a new cart.
  const add = useCallback<CartContextValue['add']>((restaurant, item) => {
    if (cart && cart.restaurantId !== restaurant.id && cart.lines.length > 0) {
      const ok = window.confirm(`Your cart has items from ${cart.restaurantName}. Start a new cart?`)
      if (!ok) return false
    }
    setCart((prev) => {
      const base: Cart =
        prev && prev.restaurantId === restaurant.id
          ? prev
          : { restaurantId: restaurant.id, restaurantName: restaurant.name, deliveryFee: restaurant.deliveryFee, lines: [] }
      const existing = base.lines.find((l) => l.menuItemId === item.id)
      const lines = existing
        ? base.lines.map((l) => (l.menuItemId === item.id ? { ...l, quantity: Math.min(l.quantity + 1, 50) } : l))
        : [...base.lines, { menuItemId: item.id, name: item.name, price: item.price, quantity: 1 }]
      return { ...base, lines }
    })
    return true
  }, [cart])

  const setQuantity = useCallback((menuItemId: string, quantity: number) => {
    setCart((prev) => {
      if (!prev) return prev
      const lines = quantity <= 0
        ? prev.lines.filter((l) => l.menuItemId !== menuItemId)
        : prev.lines.map((l) => (l.menuItemId === menuItemId ? { ...l, quantity: Math.min(quantity, 50) } : l))
      return lines.length ? { ...prev, lines } : null
    })
  }, [])

  const clear = useCallback(() => setCart(null), [])

  const value = useMemo(() => {
    const lines = cart?.lines ?? []
    return {
      cart,
      itemCount: lines.reduce((n, l) => n + l.quantity, 0),
      subtotal: lines.reduce((sum, l) => sum + l.price * l.quantity, 0),
      add,
      setQuantity,
      clear,
    }
  }, [cart, add, setQuantity, clear])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
