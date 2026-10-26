import { useQuery } from '@tanstack/react-query'
import { trackingApi } from '../api/delivery'

/** Polls the delivery API for the driver's position while a delivery is in progress. */
export function useDriverLocation(orderId: number, active: boolean) {
  const { data } = useQuery({
    queryKey: ['tracking', orderId],
    queryFn: () => trackingApi.forOrder(orderId),
    enabled: active,
    refetchInterval: 5_000,
    retry: false,
  })
  if (!data || data.driverLat == null || data.driverLng == null) return null
  return [data.driverLat, data.driverLng] as [number, number]
}
