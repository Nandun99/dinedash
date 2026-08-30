import type { Role } from '../types/auth'

export function homePathFor(role: Role): string {
  switch (role) {
    case 'RESTAURANT_OWNER':
      return '/owner'
    case 'DRIVER':
      return '/driver'
    case 'ADMIN':
      return '/admin'
    default:
      return '/'
  }
}
