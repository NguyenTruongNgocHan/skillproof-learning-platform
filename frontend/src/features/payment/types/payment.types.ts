export interface PaymentOrder {
  id: string
  productType: string
  productId: string
  idempotencyKey: string
  amountVnd: number
  status: "PENDING" | "PAID" | "FAILED" | "CANCELLED" | "EXPIRED"
  createdAt: string
  expiresAt: string
}
export interface Checkout {
  orderId: string
  status: "PENDING" | "PAID" | "FAILED" | "CANCELLED" | "EXPIRED"
  amountVnd: number
  expiresAt: string
  checkoutUrl: string | null
}
