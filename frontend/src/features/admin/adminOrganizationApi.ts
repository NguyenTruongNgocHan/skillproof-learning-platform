import { apiClient } from "@/services/api/apiClient"
import type {
  Organization,
  ApplicationRevision,
} from "@/features/organization/organizationApi"
export const adminOrganizationApi = {
  pending: () => apiClient.get<Organization[]>("/admin/organizations/pending"),
  revisions: (id: string) =>
    apiClient.get<ApplicationRevision[]>(
      `/admin/organizations/${encodeURIComponent(id)}/application-revisions`,
    ),
  review: (id: string, decision: "APPROVED" | "REJECTED", reason: string) =>
    apiClient.post<Organization>(
      `/admin/organizations/application-revisions/${encodeURIComponent(id)}/review`,
      { decision, reason },
    ),
}
