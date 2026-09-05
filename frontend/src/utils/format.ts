const lkr = new Intl.NumberFormat('en-LK', { style: 'currency', currency: 'LKR', maximumFractionDigits: 2 })

export const formatPrice = (value: number) => lkr.format(value)

export const formatDateTime = (iso: string) =>
  new Date(iso).toLocaleString('en-LK', { dateStyle: 'medium', timeStyle: 'short' })
