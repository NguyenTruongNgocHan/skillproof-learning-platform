import type {
  ApplicationRevision,
  Organization,
} from "@/features/organization/types/organization.types"
import { apiClient } from "@/shared/api/apiClient"
export type ReviewItem = { key: string; checked: boolean; note: string }
export type ReviewSession = {
  submissionId: string
  reviewType: string
  reviewerId: string
  updatedAt: string | null
  items: ReviewItem[]
}
export type OrganizationDashboard = {
  organizationPending: number
  organizationOverdue: number
  generatedAt: string
}
export const adminOrganizationApi = {
  reviewChecklist: (revisionId: string) =>
    apiClient.get<ReviewSession>(
      `/admin/organizations/application-revisions/${encodeURIComponent(revisionId)}/checklist`,
    ),
  saveReviewChecklist: (revisionId: string, items: ReviewItem[]) =>
    apiClient.put<ReviewSession>(
      `/admin/organizations/application-revisions/${encodeURIComponent(revisionId)}/checklist`,
      { items },
    ),
  dashboardSummary: () =>
    apiClient.get<OrganizationDashboard>("/admin/organizations/review-summary"),
  reviews: (id: string) =>
    apiClient.get<
      {
        reviewer_user_id: string
        decision: "APPROVED" | "REJECTED" | "NEEDS_INFORMATION"
        reason: string | null
        reviewed_at: string
      }[]
    >(`/admin/organizations/${encodeURIComponent(id)}/reviews`),
  reviewOrganization: (
    id: string,
    decision: "APPROVED" | "REJECTED" | "NEEDS_INFORMATION",
    reason: string,
  ) =>
    apiClient.post<Organization>(`/admin/organizations/${encodeURIComponent(id)}/review`, {
      decision,
      reason,
    }),
  pending: () => apiClient.get<Organization[]>("/admin/organizations/pending"),
  revisions: (id: string) =>
    apiClient.get<ApplicationRevision[]>(
      `/admin/organizations/${encodeURIComponent(id)}/application-revisions`,
    ),
  review: (id: string, decision: "APPROVED" | "REJECTED" | "NEEDS_INFORMATION", reason: string) =>
    apiClient.post<Organization>(
      `/admin/organizations/application-revisions/${encodeURIComponent(id)}/review`,
      { decision, reason },
    ),
}
