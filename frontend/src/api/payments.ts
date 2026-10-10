import { api } from './client'

export interface PaymentIntentInfo {
  orderId: number
  clientSecret: string
  publishableKey: string
  amount: number
  currency: string
}

export const paymentApi = {
  startPayment: (orderId: number | string) =>
    api.post<PaymentIntentInfo>(`/payments/orders/${orderId}/intent`).then((r) => r.data),
}
