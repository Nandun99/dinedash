import type { AuthResponse, User } from '../types/auth'

const KEY = 'dinedash.session'

export interface Session {
  accessToken: string
  refreshToken: string
  user: User
}

export const tokenStorage = {
  load(): Session | null {
    const raw = localStorage.getItem(KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw) as Session
    } catch {
      localStorage.removeItem(KEY)
      return null
    }
  },
  save(response: AuthResponse) {
    const session: Session = {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      user: response.user,
    }
    localStorage.setItem(KEY, JSON.stringify(session))
    return session
  },
  updateUser(user: User) {
    const session = this.load()
    if (session) localStorage.setItem(KEY, JSON.stringify({ ...session, user }))
  },
  clear() {
    localStorage.removeItem(KEY)
  },
}
