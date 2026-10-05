import { apiClient } from "@/services/api/apiClient"
export type OrganizationStatus = "DRAFT" | "PENDING" | "APPROVED" | "REJECTED"
export type Authority =
  "MANAGE_PROFILE" | "MANAGE_MEMBERS" | "MANAGE_CONTENT" | "ISSUE_CERTIFICATES"
export interface Organization {
  id: string
  ownerUserId: string
  legalName: string
  displayName: string
  website: string | null
  industry: string
  country: string
  registrationNumber: string | null
  contactName: string
  contactEmail: string
  contactPhone: string | null
  status: OrganizationStatus
  reviewReason: string | null
  createdAt: string
  updatedAt: string
}
export interface OrganizationApplication {
  legalName: string
  displayName: string
  website?: string
  industry: string
  country: string
  registrationNumber?: string
  contactName: string
  contactEmail: string
  contactPhone?: string
}
export interface ApplicationRevision {
  id: string
  organizationId: string
  revisionNo: number
  legalName: string
  displayName: string
  website: string | null
  industry: string
  country: string
  registrationNumber: string | null
  contactName: string
  contactEmail: string
  contactPhone: string | null
  status: "DRAFT" | "PENDING" | "APPROVED" | "REJECTED"
  reviewerUserId: string | null
  reviewReason: string | null
  submittedAt: string
  documentMediaIds: string[]
  reviewedAt: string | null
}
export interface Member {
  user_id: string
  email: string
  active: boolean
  grants: Authority[]
}
export interface Invitation {
  id: string
  organizationId: string
  email: string
  status: "PENDING" | "ACCEPTED" | "REVOKED" | "EXPIRED"
  expiresAt: string
  createdAt: string
}
export const organizationApi = {
  saveDraft: (data: OrganizationApplication) =>
    apiClient.put<Organization>("/organizations/mine/draft", data),
  submit: () => apiClient.post<Organization>("/organizations/mine/submit"),
  previewInvitation: (token: string) =>
    apiClient.get<{
      organizationId: string
      organizationName: string
      status: string
      expiresAt: string
    }>(`/organizations/invitations/${encodeURIComponent(token)}`),
  acceptInvitation: (token: string) =>
    apiClient.post<void>(
      `/organizations/invitations/${encodeURIComponent(token)}/accept`,
    ),
  mine: () => apiClient.get<Organization>("/organizations/mine"),
  memberships: () => apiClient.get<Organization[]>("/organizations/mine/all"),
  resubmit: (data: OrganizationApplication) =>
    apiClient.post<Organization>("/organizations/mine/resubmit", data),
  create: (data: OrganizationApplication) =>
    apiClient.post<Organization>("/organizations", data),
  update: (
    id: string,
    data: Pick<
      OrganizationApplication,
      "displayName" | "website" | "industry" | "contactPhone"
    >,
  ) => apiClient.patch<Organization>(`/organizations/${id}`, data),
  applicationRevisions: (id: string) =>
    apiClient.get<ApplicationRevision[]>(
      `/organizations/${encodeURIComponent(id)}/application-revisions`,
    ),
  members: (id: string) =>
    apiClient.get<Member[]>(`/organizations/${id}/members`),
  addMember: (id: string, email: string) =>
    apiClient.post<void>(`/organizations/${id}/members`, { email }),
  grant: (
    id: string,
    memberId: string,
    authority: Authority,
    active: boolean,
  ) =>
    apiClient.patch<void>(`/organizations/${id}/members/${memberId}/grants`, {
      authority,
      active,
    }),
  removeMember: (id: string, memberId: string) =>
    apiClient.delete<void>(`/organizations/${id}/members/${memberId}`),
  invitations: (id: string) =>
    apiClient.get<Invitation[]>(
      `/organizations/${encodeURIComponent(id)}/invitations`,
    ),
  invite: (id: string, email: string) =>
    apiClient.post<Invitation>(
      `/organizations/${encodeURIComponent(id)}/invitations`,
      { email },
    ),
  revokeInvitation: (id: string) =>
    apiClient.delete<void>(
      `/organizations/invitations/${encodeURIComponent(id)}`,
    ),
  resendInvitation: (id: string) =>
    apiClient.post<Invitation>(
      `/organizations/invitations/${encodeURIComponent(id)}/resend`,
    ),
  can: (id: string, authority: Authority) =>
    apiClient.get<{ allowed: boolean }>(
      `/organizations/${id}/authority/${authority}`,
    ),
  pending: () => apiClient.get<Organization[]>("/admin/organizations/pending"),
  reviews: (id: string) =>
    apiClient.get<
      {
        reviewer_user_id: string
        decision: "APPROVED" | "REJECTED"
        reason: string | null
        reviewed_at: string
      }[]
    >(`/admin/organizations/${id}/reviews`),
  review: (id: string, decision: "APPROVED" | "REJECTED", reason?: string) =>
    apiClient.post<Organization>(`/admin/organizations/${id}/review`, {
      decision,
      reason,
    }),
}
