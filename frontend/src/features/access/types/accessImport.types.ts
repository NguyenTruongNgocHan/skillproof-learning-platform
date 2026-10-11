import type { ProductType, AccessGrant } from "./access.types";
export interface AccessImportInput {
  entries: string[];
  type: ProductType;
  productId: string;
  expiresAt: string | null;
  cohort: string;
}
export interface ImportRow {
  row: number;
  identity: string;
  learnerId: string | null;
  eligible: boolean;
  error: string;
}
export interface ImportResult {
  row: number;
  identity: string;
  learnerId: string | null;
  success: boolean;
  error?: string;
  grant?: AccessGrant;
}
