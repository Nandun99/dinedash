export type Role = 'CUSTOMER' | 'RESTAURANT_OWNER' | 'DRIVER' | 'ADMIN'

export interface User {
  id: number
  email: string
  fullName: string
  phone: string | null
  role: Role
  enabled: boolean
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload extends LoginPayload {
  fullName: string
  phone?: string
  role: Exclude<Role, 'ADMIN'>
}

export interface ApiError {
  status: number
  error: string
  message: string
  path: string
  details: string[]
}
