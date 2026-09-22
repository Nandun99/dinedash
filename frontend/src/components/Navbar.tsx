import { Link, useNavigate } from 'react-router-dom'
import { homePathFor } from '../auth/roleRoutes'
import { useAuth } from '../auth/useAuth'
import { useCart } from '../cart/useCart'

const roleLabel = {
  CUSTOMER: 'Customer',
  RESTAURANT_OWNER: 'Restaurant',
  DRIVER: 'Driver',
  ADMIN: 'Admin',
} as const

export default function Navbar() {
  const { user, logout } = useAuth()
  const { itemCount } = useCart()
  const navigate = useNavigate()

  const onLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <header className="border-b border-stone-200 bg-white">
      <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
        <Link to={user ? homePathFor(user.role) : '/'} className="text-xl font-bold text-brand-600">
          DineDash
        </Link>
        {(!user || user.role === 'CUSTOMER') && itemCount > 0 && (
          <Link to="/cart" className="ml-auto mr-4 rounded-full bg-brand-50 px-3 py-1 text-sm font-medium text-brand-700">
            Cart · {itemCount}
          </Link>
        )}
        {user ? (
          <div className="flex items-center gap-4 text-sm">
            <span className="hidden text-stone-600 sm:inline">
              {user.fullName}
              <span className="ml-2 rounded-full bg-stone-100 px-2 py-0.5 text-xs">{roleLabel[user.role]}</span>
            </span>
            <button onClick={onLogout} className="font-medium hover:text-brand-600">
              Log out
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-4 text-sm font-medium">
            <Link to="/login" className="hover:text-brand-600">
              Log in
            </Link>
            <Link to="/register" className="rounded-lg bg-brand-600 px-4 py-2 text-white hover:bg-brand-700">
              Sign up
            </Link>
          </div>
        )}
      </nav>
    </header>
  )
}
