import { apiClient } from "@/shared/api/apiClient";
import type { Commercial } from "../types/pricing.types";
const path = (version: string) =>
  `/course-workspace/versions/${encodeURIComponent(version)}`;
export const pricingApi = {
  get: (id: string) => apiClient.get<Commercial>(path(id) + "/commercial"),
  save: (
    id: string,
    input: {
      accessMode: string;
      priceVnd: number;
      certificationPriceVnd: number;
      reason: string;
    },
  ) => apiClient.put(path(id) + "/commercial", input),
  lifecycle: (id: string, state: string) =>
    apiClient.put(path(id) + "/lifecycle", { state }),
  promote: (
    id: string,
    input: { name: string; percent: number; startsAt: string; endsAt: string },
  ) => apiClient.post(path(id) + "/promotions", input),
  cancel: (id: string, promotion: string) =>
    apiClient.delete(path(id) + `/promotions/${encodeURIComponent(promotion)}`),
};
