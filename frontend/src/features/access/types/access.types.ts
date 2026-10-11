export type ProductType = "COURSE" | "CERTIFICATION" | "RESOURCE";
export interface AccessGrant {
  id: string;
  learnerId: string;
  productType: ProductType;
  productId: string;
  source: string;
  cohort: string;
  createdAt: string;
  expiresAt: string | null;
  revokedAt: string | null;
}
