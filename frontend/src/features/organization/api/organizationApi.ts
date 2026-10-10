import type {
  ApplicationRevision,
  Authority,
  Invitation,
  Member,
  Organization,
  OrganizationApplication,
} from "@/features/organization/types/organization.types"
import { apiClient } from "@/shared/api/apiClient"
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
    apiClient.post<void>(`/organizations/invitations/${encodeURIComponent(token)}/accept`),
  mine: () => apiClient.get<Organization>("/organizations/mine"),
  memberships: () => apiClient.get<Organization[]>("/organizations/mine/all"),
  resubmit: (data: OrganizationApplication) =>
    apiClient.post<Organization>("/organizations/mine/resubmit", data),
  create: (data: OrganizationApplication) => apiClient.post<Organization>("/organizations", data),
  update: (
    id: string,
    data: Pick<OrganizationApplication, "displayName" | "website" | "industry" | "contactPhone">,
  ) => apiClient.patch<Organization>(`/organizations/${id}`, data),
  applicationRevisions: (id: string) =>
    apiClient.get<ApplicationRevision[]>(
      `/organizations/${encodeURIComponent(id)}/application-revisions`,
    ),
  members: (id: string) => apiClient.get<Member[]>(`/organizations/${id}/members`),
  grant: (id: string, memberId: string, authority: Authority, active: boolean) =>
    apiClient.patch<void>(`/organizations/${id}/members/${memberId}/grants`, {
      authority,
      active,
    }),
  removeMember: (id: string, memberId: string) =>
    apiClient.delete<void>(`/organizations/${id}/members/${memberId}`),
  invitations: (id: string) =>
    apiClient.get<Invitation[]>(`/organizations/${encodeURIComponent(id)}/invitations`),
  invite: (id: string, email: string) =>
    apiClient.post<Invitation>(`/organizations/${encodeURIComponent(id)}/invitations`, { email }),
  revokeInvitation: (id: string) =>
    apiClient.delete<void>(`/organizations/invitations/${encodeURIComponent(id)}`),
  resendInvitation: (id: string) =>
    apiClient.post<Invitation>(`/organizations/invitations/${encodeURIComponent(id)}/resend`),
  can: (id: string, authority: Authority) =>
    apiClient.get<{ allowed: boolean }>(`/organizations/${id}/authority/${authority}`),
}
