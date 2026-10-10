import type {
  ApplicationRevision,
  Organization,
} from "@/features/organization/types/organization.types"
import { apiClient } from "@/shared/api/apiClient"
export const adminOrganizationApi = {
  reviews: (id: string) =>
    apiClient.get<
      {
        reviewer_user_id: string
        decision: "APPROVED" | "REJECTED"
        reason: string | null
        reviewed_at: string
      }[]
    >(`/admin/organizations/${encodeURIComponent(id)}/reviews`),
  reviewOrganization: (id: string, decision: "APPROVED" | "REJECTED", reason: string) =>
    apiClient.post<Organization>(`/admin/organizations/${encodeURIComponent(id)}/review`, {
      decision,
      reason,
    }),
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
