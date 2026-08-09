import { Link } from 'react-router-dom'

export default function Navbar() {
  return (
    <header className="border-b border-stone-200 bg-white">
      <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
        <Link to="/" className="text-xl font-bold text-brand-600">
          DineDash
        </Link>
        <div className="flex items-center gap-4 text-sm font-medium">
          <Link to="/login" className="hover:text-brand-600">
            Log in
          </Link>
          <Link
            to="/register"
            className="rounded-lg bg-brand-600 px-4 py-2 text-white hover:bg-brand-700"
          >
            Sign up
          </Link>
        </div>
      </nav>
    </header>
  )
}
