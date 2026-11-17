import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import NotFoundPage from './pages/NotFoundPage'
import CartPage from './pages/cart/CartPage'
import ProtectedRoute from './auth/ProtectedRoute'
import RestaurantDetailPage from './pages/restaurants/RestaurantDetailPage'
import RestaurantListPage from './pages/restaurants/RestaurantListPage'

// Role dashboards, checkout (Stripe) and tracking (Leaflet/Firebase) load on demand
const CheckoutPage = lazy(() => import('./pages/cart/CheckoutPage'))
const OrderDetailPage = lazy(() => import('./pages/orders/OrderDetailPage'))
const OrdersPage = lazy(() => import('./pages/orders/OrdersPage'))
const PaymentPage = lazy(() => import('./pages/payment/PaymentPage'))
const OwnerDashboardPage = lazy(() => import('./pages/owner/OwnerDashboardPage'))
const OwnerMenuPage = lazy(() => import('./pages/owner/OwnerMenuPage'))
const KitchenPage = lazy(() => import('./pages/owner/KitchenPage'))
const DriverDashboardPage = lazy(() => import('./pages/driver/DriverDashboardPage'))
const AdminDashboardPage = lazy(() => import('./pages/admin/AdminDashboardPage'))

export default function App() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <main>
        <Suspense fallback={<p className="p-8 text-stone-500">Loading…</p>}>
        <Routes>
          <Route path="/" element={<RestaurantListPage />} />
          <Route path="/restaurants/:id" element={<RestaurantDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/checkout" element={<ProtectedRoute roles={['CUSTOMER']}><CheckoutPage /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute roles={['CUSTOMER']}><OrdersPage /></ProtectedRoute>} />
          <Route path="/orders/:id" element={<ProtectedRoute><OrderDetailPage /></ProtectedRoute>} />
          <Route path="/orders/:id/pay" element={<ProtectedRoute roles={['CUSTOMER']}><PaymentPage /></ProtectedRoute>} />
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
          <Route
            path="/owner/restaurants/:id/orders"
            element={
              <ProtectedRoute roles={['RESTAURANT_OWNER']}>
                <KitchenPage />
              </ProtectedRoute>
            }
          />
          <Route path="/driver" element={<ProtectedRoute roles={['DRIVER']}><DriverDashboardPage /></ProtectedRoute>} />
          <Route path="/admin/*" element={<ProtectedRoute roles={['ADMIN']}><AdminDashboardPage /></ProtectedRoute>} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
        </Suspense>
      </main>
    </div>
  )
}
