import { apiClient } from '@/services/api/apiClient';
export type OrganizationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type Authority = 'MANAGE_PROFILE' | 'MANAGE_MEMBERS' | 'MANAGE_CONTENT' | 'ISSUE_CERTIFICATES';
export interface Organization { id: string; ownerUserId: string; legalName: string; displayName: string; website: string | null; industry: string; country: string; registrationNumber: string | null; contactName: string; contactEmail: string; contactPhone: string | null; status: OrganizationStatus; reviewReason: string | null; createdAt: string; updatedAt: string; }
export interface OrganizationApplication { legalName: string; displayName: string; website?: string; industry: string; country: string; registrationNumber?: string; contactName: string; contactEmail: string; contactPhone?: string; }
export interface Member { user_id: string; email: string; active: boolean; grants: Authority[]; }
export const organizationApi = {
 mine: () => apiClient.get<Organization>('/organizations/mine'),
 resubmit: (data: OrganizationApplication) => apiClient.post<Organization>('/organizations/mine/resubmit',data),
 create: (data: OrganizationApplication) => apiClient.post<Organization>('/organizations', data),
 update: (id: string, data: Pick<OrganizationApplication, 'displayName'|'website'|'industry'|'contactPhone'>) => apiClient.patch<Organization>(`/organizations/${id}`, data),
 members: (id: string) => apiClient.get<Member[]>(`/organizations/${id}/members`),
 addMember: (id: string, userId: string) => apiClient.post<void>(`/organizations/${id}/members`, { userId }),
 grant: (id: string, memberId: string, authority: Authority, active: boolean) => apiClient.patch<void>(`/organizations/${id}/members/${memberId}/grants`, { authority, active }),
 removeMember: (id: string, memberId: string) => apiClient.delete<void>(`/organizations/${id}/members/${memberId}`),
 can: (id: string, authority: Authority) => apiClient.get<{allowed:boolean}>(`/organizations/${id}/authority/${authority}`),
 pending: () => apiClient.get<Organization[]>('/admin/organizations/pending'),
 review: (id: string, decision: 'APPROVED'|'REJECTED', reason?: string) => apiClient.post<Organization>(`/admin/organizations/${id}/review`, { decision, reason }),
};
