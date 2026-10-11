import { apiClient } from "@/shared/api/apiClient";
import type { Checkout, PaymentOrder } from "../types/payment.types";
export const paymentApi = {
  mine: () => apiClient.get<PaymentOrder[]>("/payments/me/orders"),
  checkout: (type: string, productId: string, key: string) =>
    apiClient.post<Checkout>(
      "/payments/orders",
      { type, productId },
      { "Idempotency-Key": key },
    ),
};
