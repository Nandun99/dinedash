import { NavLink, Route, Routes } from 'react-router-dom'
import AdminOverview from './AdminOverview'
import AdminRestaurants from './AdminRestaurants'
import AdminUsers from './AdminUsers'

const tab = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-1.5 text-sm font-medium ${isActive ? 'bg-brand-600 text-white' : 'hover:bg-stone-200'}`

export default function AdminDashboardPage() {
  return (
    <section className="mx-auto max-w-6xl px-4 py-8">
      <h1 className="text-2xl font-bold">Admin</h1>
      <nav className="mt-4 flex gap-2">
        <NavLink to="/admin" end className={tab}>Overview</NavLink>
        <NavLink to="/admin/restaurants" className={tab}>Restaurants</NavLink>
        <NavLink to="/admin/users" className={tab}>Users</NavLink>
      </nav>
      <div className="mt-6">
        <Routes>
          <Route index element={<AdminOverview />} />
          <Route path="restaurants" element={<AdminRestaurants />} />
          <Route path="users" element={<AdminUsers />} />
        </Routes>
      </div>
    </section>
  )
}
