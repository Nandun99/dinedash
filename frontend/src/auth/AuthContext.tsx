import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../api/auth'
import { queryClient } from '../lib/queryClient'
import type { LoginPayload, RegisterPayload, User } from '../types/auth'
import { tokenStorage } from './tokenStorage'

export interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  login: (payload: LoginPayload) => Promise<User>
  register: (payload: RegisterPayload) => Promise<User>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => tokenStorage.load()?.user ?? null)

  useEffect(() => {
    const onForcedLogout = () => setUser(null)
    window.addEventListener('dinedash:logout', onForcedLogout)
    return () => window.removeEventListener('dinedash:logout', onForcedLogout)
  }, [])

  const login = useCallback(async (payload: LoginPayload) => {
    const response = await authApi.login(payload)
    tokenStorage.save(response)
    setUser(response.user)
    return response.user
  }, [])

  const register = useCallback(
    async (payload: RegisterPayload) => {
      await authApi.register(payload)
      return login({ email: payload.email, password: payload.password })
    },
    [login],
  )

  const logout = useCallback(async () => {
    const session = tokenStorage.load()
    tokenStorage.clear()
    setUser(null)
    // Drop cached private data (orders, profile, notifications) of the previous user
    queryClient.removeQueries({ predicate: (q) => !['restaurants', 'restaurant', 'menu'].includes(String(q.queryKey[0])) })
    if (session) await authApi.logout(session.refreshToken).catch(() => undefined)
  }, [])

  const value = useMemo(
    () => ({ user, isAuthenticated: user !== null, login, register, logout }),
    [user, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
