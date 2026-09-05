import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import NotFoundPage from './pages/NotFoundPage'
import RestaurantListPage from './pages/restaurants/RestaurantListPage'

export default function App() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<RestaurantListPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </div>
  )
}
