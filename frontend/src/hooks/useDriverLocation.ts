import { useQuery } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { trackingApi } from '../api/delivery'
import { firebaseApp, firebaseEnabled } from '../lib/firebase'

type LatLng = [number, number]

/**
 * Live driver position. Uses Firebase Realtime Database when configured (the backend
 * hands out a token scoped to this order); otherwise polls the delivery API.
 */
export function useDriverLocation(orderId: number, active: boolean): LatLng | null {
  const [live, setLive] = useState<LatLng | null>(null)
  const [liveFailed, setLiveFailed] = useState(false)
  const useLive = firebaseEnabled && !liveFailed

  useEffect(() => {
    if (!active || !useLive) return
    let unsubscribe: (() => void) | undefined
    let cancelled = false
    ;(async () => {
      try {
        const { data } = await api.get<{ token: string }>(`/deliveries/orders/${orderId}/tracking-token`)
        const app = await firebaseApp()
        const [{ getAuth, signInWithCustomToken }, { getDatabase, ref, onValue }] = await Promise.all([
          import('firebase/auth'),
          import('firebase/database'),
        ])
        await signInWithCustomToken(getAuth(app), data.token)
        if (cancelled) return
        unsubscribe = onValue(ref(getDatabase(app), `deliveries/${orderId}/driverLocation`), (snap) => {
          const v = snap.val() as { lat: number; lng: number } | null
          setLive(v ? [v.lat, v.lng] : null)
        })
      } catch {
        setLiveFailed(true)
      }
    })()
    return () => {
      cancelled = true
      unsubscribe?.()
    }
  }, [orderId, active, useLive])

  const { data } = useQuery({
    queryKey: ['tracking', orderId],
    queryFn: () => trackingApi.forOrder(orderId),
    enabled: active && !useLive,
    refetchInterval: 5_000,
    retry: false,
  })

  if (useLive) return live
  if (!data || data.driverLat == null || data.driverLng == null) return null
  return [data.driverLat, data.driverLng]
}
