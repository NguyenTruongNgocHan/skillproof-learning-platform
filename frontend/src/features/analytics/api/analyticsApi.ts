import { apiClient } from "@/shared/api/apiClient";
import type { Analytics } from "../types/analytics.types";
export const analyticsApi = {
  dashboard: (
    org: string,
    start: string,
    end: string,
    version = "",
    campaign = "",
  ) =>
    apiClient.get<Analytics>(
      `/organizations/${encodeURIComponent(org)}/analytics?${new URLSearchParams({ start, end, version, campaign })}`,
    ),
  event: (event: {
    organizationId: string;
    versionId: string | null;
    visitorId: string;
    requestId: string;
    kind: string;
    source: string;
    campaign: string;
  }) => apiClient.post("/analytics/events", event),
};
