import { QueryClient } from '@tanstack/react-query'
import { createSyncStoragePersister } from '@tanstack/query-sync-storage-persister'
import type { PersistQueryClientOptions } from '@tanstack/react-query-persist-client'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false },
  },
})

/** Only public catalog data is saved; nothing user-specific (orders, profile) touches localStorage. */
const PERSISTED_ROOTS = new Set(['restaurants', 'restaurant', 'menu'])

export const persistOptions: Omit<PersistQueryClientOptions, 'queryClient'> = {
  persister: createSyncStoragePersister({ storage: window.localStorage, key: 'dinedash.query-cache', throttleTime: 2_000 }),
  maxAge: 60 * 60 * 1000,
  // Bump to invalidate everyone's saved cache after an incompatible API change
  buster: 'v1',
  dehydrateOptions: {
    shouldDehydrateQuery: (query) =>
      query.state.status === 'success' && PERSISTED_ROOTS.has(String(query.queryKey[0])),
  },
}
