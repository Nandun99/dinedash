import { useCallback, useState } from 'react'

export interface Coords {
  lat: number
  lng: number
}

export function useGeolocation() {
  const [coords, setCoords] = useState<Coords | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const locate = useCallback(() => {
    if (!navigator.geolocation) {
      setError('Location is not supported by this browser')
      return
    }
    setLoading(true)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude })
        setError(null)
        setLoading(false)
      },
      () => {
        setError('Could not get your location')
        setLoading(false)
      },
      { enableHighAccuracy: false, timeout: 10_000 },
    )
  }, [])

  return { coords, error, loading, locate, clear: () => setCoords(null) }
}
