import type { AuthResponse, LoginPayload, RegisterPayload, User } from '../types/auth'
import { api } from './client'

export const authApi = {
  login: (payload: LoginPayload) => api.post<AuthResponse>('/auth/login', payload).then((r) => r.data),
  register: (payload: RegisterPayload) => api.post<User>('/auth/register', payload).then((r) => r.data),
  logout: (refreshToken: string) => api.post('/auth/logout', { refreshToken }),
  me: () => api.get<User>('/users/me').then((r) => r.data),
}
