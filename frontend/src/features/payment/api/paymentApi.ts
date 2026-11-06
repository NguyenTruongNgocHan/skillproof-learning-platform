import { searchParams, type PageResult } from "@/shared/api/page"
import { apiClient } from "@/shared/api/apiClient"
import type { Checkout, PaymentOrder } from "../types/payment.types"
export const paymentApi = {
  search: (status: string, page: number) =>
    apiClient.get<PageResult<PaymentOrder>>(
      `/payments/me/orders/search?${searchParams({ status, page, size: 20 })}`,
    ),
  detail: (id: string) => apiClient.get<PaymentOrder>(`/payments/orders/${encodeURIComponent(id)}`),
  mine: () => apiClient.get<PaymentOrder[]>("/payments/me/orders"),
  checkout: (type: string, productId: string, key: string) =>
    apiClient.post<Checkout>("/payments/orders", { type, productId }, { "Idempotency-Key": key }),
}
