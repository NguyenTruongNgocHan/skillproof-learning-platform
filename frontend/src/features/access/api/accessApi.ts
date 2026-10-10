import { apiClient } from "@/shared/api/apiClient";
import type { AccessGrant, ProductType } from "../types/access.types";
export const accessApi = {
  grant: (
    org: string,
    input: {
      learnerId: string;
      type: ProductType;
      productId: string;
      expiresAt: string | null;
    },
  ) =>
    apiClient.post<AccessGrant>(
      `/access/organizations/${encodeURIComponent(org)}/grants`,
      input,
    ),
  revoke: (org: string, id: string) =>
    apiClient.delete(
      `/access/organizations/${encodeURIComponent(org)}/grants/${encodeURIComponent(id)}`,
    ),
};
