import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { tokenStorage } from '../auth/tokenStorage'
import type { ApiError, AuthResponse } from '../types/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api'

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const session = tokenStorage.load()
  if (session) config.headers.Authorization = `Bearer ${session.accessToken}`
  return config
})

// Share one refresh call between all requests that fail at the same time.
let refreshing: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const session = tokenStorage.load()
  if (!session) return null
  try {
    const { data } = await axios.post<AuthResponse>(`${baseURL}/auth/refresh`, {
      refreshToken: session.refreshToken,
    })
    tokenStorage.save(data)
    return data.accessToken
  } catch {
    tokenStorage.clear()
    window.dispatchEvent(new Event('dinedash:logout'))
    return null
  }
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const isAuthCall = original?.url?.startsWith('/auth/')
    if (error.response?.status === 401 && original && !original._retry && !isAuthCall) {
      original._retry = true
      refreshing ??= refreshAccessToken().finally(() => {
        refreshing = null
      })
      const token = await refreshing
      if (token) {
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      }
    }
    return Promise.reject(error)
  },
)

export function errorMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError<ApiError>(error)) {
    const body = error.response?.data
    if (body?.details?.length) return body.details.join(', ')
    if (body?.message) return body.message
  }
  return fallback
}
