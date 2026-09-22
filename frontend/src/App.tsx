import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import NotFoundPage from './pages/NotFoundPage'
import CartPage from './pages/cart/CartPage'
import OwnerDashboardPage from './pages/owner/OwnerDashboardPage'
import OwnerMenuPage from './pages/owner/OwnerMenuPage'
import ProtectedRoute from './auth/ProtectedRoute'
import RestaurantDetailPage from './pages/restaurants/RestaurantDetailPage'
import RestaurantListPage from './pages/restaurants/RestaurantListPage'

export default function App() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<RestaurantListPage />} />
          <Route path="/restaurants/:id" element={<RestaurantDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/owner"
            element={
              <ProtectedRoute roles={['RESTAURANT_OWNER']}>
                <OwnerDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/owner/restaurants/:id/menu"
            element={
              <ProtectedRoute roles={['RESTAURANT_OWNER']}>
                <OwnerMenuPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </div>
  )
}
