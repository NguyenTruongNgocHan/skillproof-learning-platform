import { apiClient } from "@/shared/api/apiClient";
import type { Notice, Audit, Page } from "../types/operations.types";
export const operationsApi = {
  notices: (page = 0) =>
    apiClient.get<Page<Notice>>(`/workspace/notifications?page=${page}`),
  read: (id: string) =>
    apiClient.post(`/workspace/notifications/${encodeURIComponent(id)}/read`),
  audit: (org: string, page = 0) =>
    apiClient.get<Page<Audit>>(
      `/workspace/organizations/${encodeURIComponent(org)}/audit?page=${page}`,
    ),
};
