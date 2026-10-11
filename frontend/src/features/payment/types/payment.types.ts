export interface PaymentOrder {
  id: string;
  productType: string;
  productId: string;
  idempotencyKey: string;
  amountVnd: number;
  status: string;
  createdAt: string;
  expiresAt: string;
}
export interface Checkout {
  orderId: string;
  status: string;
  amountVnd: number;
  expiresAt: string;
  checkoutUrl: string | null;
}
