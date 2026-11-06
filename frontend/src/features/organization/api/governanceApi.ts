import { apiClient } from "@/shared/api/apiClient"
import type { EvidenceDocument, IssuerApplication } from "../types/governance.types"
const e = encodeURIComponent
export interface EvidenceSnapshot {
  evidenceId: string
  mediaId: string
  kind: string
  title: string
  expiresAt: string | null
  status: string
  sha256: string
  reason: string | null
  reviewerId: string | null
  reviewedAt: string | null
}
export const governanceApi = {
  verificationSnapshot: async (
    revision: string,
    organizationId: string,
  ): Promise<EvidenceDocument[]> => {
    const rows = await apiClient.get<EvidenceSnapshot[]>(
      `/organizations/application-revisions/${e(revision)}/evidence-snapshot`,
    )
    return rows.map((row) => ({
      ...row,
      id: row.evidenceId,
      organizationId,
      purpose: "VERIFICATION",
      createdAt: "",
    }))
  },
  legalReverification: (org: string) =>
    apiClient.post(`/organizations/${e(org)}/legal-reverification`),
  evidence: (org: string) => apiClient.get<EvidenceDocument[]>(`/organizations/${e(org)}/evidence`),
  classify: (
    org: string,
    input: {
      mediaId: string
      kind: string
      purpose: string
      title: string
      expiresAt: string | null
    },
  ) => apiClient.post<EvidenceDocument>(`/organizations/${e(org)}/evidence`, input),
  reviewEvidence: (org: string, id: string, input: { status: string; reason: string }) =>
    apiClient.post(`/admin/organizations/${e(org)}/evidence/${e(id)}/review`, input),
  issuers: (org: string) =>
    apiClient.get<IssuerApplication[]>(`/organizations/${e(org)}/issuer-applications`),
  apply: (
    org: string,
    input: {
      scope: string
      evidenceIds: string[]
      scopeMode: string
      courseVersionIds: string[]
    },
  ) => apiClient.post(`/organizations/${e(org)}/issuer-applications`, input),
  pending: () => apiClient.get<IssuerApplication[]>("/admin/issuer-applications"),
  decide: (id: string, input: { status: string; reason: string; validUntil: string | null }) =>
    apiClient.post(`/admin/issuer-applications/${e(id)}/review`, input),
}
