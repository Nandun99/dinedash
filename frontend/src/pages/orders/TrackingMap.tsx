import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useEffect } from 'react'
import { MapContainer, Marker, Polyline, TileLayer, Tooltip, useMap } from 'react-leaflet'

type LatLng = [number, number]

const dot = (color: string) =>
  L.divIcon({
    className: '',
    html: `<span style="display:block;width:16px;height:16px;border-radius:9999px;background:${color};border:3px solid white;box-shadow:0 1px 4px rgba(0,0,0,.4)"></span>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8],
  })

const icons = { restaurant: dot('#ea580c'), home: dot('#0f766e'), driver: dot('#1d4ed8') }

function FitBounds({ points }: { points: LatLng[] }) {
  const map = useMap()
  useEffect(() => {
    if (points.length) map.fitBounds(L.latLngBounds(points), { padding: [40, 40], maxZoom: 16 })
    // Only refit when the set of points changes meaningfully, not on every driver move
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map, points.length])
  return null
}

interface Props {
  pickup: LatLng
  dropoff: LatLng
  driver?: LatLng | null
}

export default function TrackingMap({ pickup, dropoff, driver }: Props) {
  const points = driver ? [pickup, dropoff, driver] : [pickup, dropoff]
  return (
    <MapContainer center={pickup} zoom={14} scrollWheelZoom={false} className="h-72 w-full rounded-2xl">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Marker position={pickup} icon={icons.restaurant}><Tooltip>Restaurant</Tooltip></Marker>
      <Marker position={dropoff} icon={icons.home}><Tooltip>You</Tooltip></Marker>
      {driver && <Marker position={driver} icon={icons.driver}><Tooltip permanent direction="top">Driver</Tooltip></Marker>}
      <Polyline positions={[pickup, dropoff]} pathOptions={{ color: '#a8a29e', dashArray: '6 8', weight: 3 }} />
      <FitBounds points={points} />
    </MapContainer>
  )
}
