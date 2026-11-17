import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { cacheTimes, queryKeys } from '../../api/queryKeys'
import { restaurantApi } from '../../api/restaurants'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import { useGeolocation } from '../../hooks/useGeolocation'
import RestaurantCardView from './RestaurantCardView'

const cuisines = ['sri lankan', 'indian', 'chinese', 'italian', 'burgers', 'desserts']

export default function RestaurantListPage() {
  const [q, setQ] = useState('')
  const [search, setSearch] = useState('')
  const [cuisine, setCuisine] = useState<string | undefined>()
  const [page, setPage] = useState(0)
  const geo = useGeolocation()

  const params = { q: search || undefined, cuisine, page, lat: geo.coords?.lat, lng: geo.coords?.lng }
  const { data, isLoading, isError } = useQuery({
    queryKey: queryKeys.restaurants(params),
    queryFn: () => restaurantApi.search(params),
    placeholderData: keepPreviousData,
    ...cacheTimes.restaurants,
  })

  return (
    <section className="mx-auto max-w-6xl px-4 py-8">
      <form
        className="flex flex-col gap-3 sm:flex-row"
        onSubmit={(e) => {
          e.preventDefault()
          setPage(0)
          setSearch(q.trim())
        }}
      >
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search restaurants"
          className="flex-1 rounded-lg border border-stone-300 px-4 py-2 outline-none focus:ring-2 focus:ring-brand-500"
        />
        <Button type="submit">Search</Button>
        <Button type="button" variant="secondary" loading={geo.loading} onClick={geo.coords ? geo.clear : geo.locate}>
          {geo.coords ? 'Clear location' : 'Near me'}
        </Button>
      </form>
      {geo.error && <div className="mt-3"><Alert kind="info">{geo.error}</Alert></div>}

      <div className="mt-4 flex flex-wrap gap-2">
        {cuisines.map((c) => (
          <button
            key={c}
            onClick={() => {
              setPage(0)
              setCuisine(cuisine === c ? undefined : c)
            }}
            className={`rounded-full border px-3 py-1 text-sm capitalize ${
              cuisine === c ? 'border-brand-500 bg-brand-50 text-brand-700' : 'border-stone-300 bg-white'
            }`}
          >
            {c}
          </button>
        ))}
      </div>

      {isError && <div className="mt-6"><Alert>Could not load restaurants. Please try again.</Alert></div>}
      {isLoading && <p className="mt-8 text-stone-500">Loading restaurants…</p>}
      {data && data.content.length === 0 && <p className="mt-8 text-stone-500">No restaurants found.</p>}

      <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {data?.content.map((r) => <RestaurantCardView key={r.id} restaurant={r} />)}
      </div>

      {data && (data.page > 0 || data.hasNext) && (
        <div className="mt-8 flex justify-center gap-3">
          <Button variant="secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </Button>
          <Button variant="secondary" disabled={!data.hasNext} onClick={() => setPage((p) => p + 1)}>
            Next
          </Button>
        </div>
      )}
    </section>
  )
}
