import { apiClient } from "@/shared/api/apiClient";
import type { AccessGrant, ProductType } from "../types/access.types";
export const accessApi = {
  list: (org: string, page = 0, query = "", status = "") =>
    apiClient.get<{
      content: AccessGrant[];
      totalPages: number;
      totalElements: number;
    }>(
      `/access/organizations/${encodeURIComponent(org)}/grants?${new URLSearchParams({ page: String(page), query, status })}`,
    ),
  bulk: (
    org: string,
    input: {
      learnerIds: string[];
      type: ProductType;
      productId: string;
      expiresAt: string | null;
    },
  ) =>
    apiClient.post<
      {
        learnerId: string;
        success: boolean;
        error?: string;
        grant?: AccessGrant;
      }[]
    >(`/access/organizations/${encodeURIComponent(org)}/grants/bulk`, input),
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
