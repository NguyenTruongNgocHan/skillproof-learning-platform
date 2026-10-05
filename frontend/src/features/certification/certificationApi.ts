import { apiClient } from "@/services/api/apiClient"
export interface Page<T> {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
export interface Program {
  id: string
  organizationId: string
  learningPathVersionId: string
  completionPolicyId: string
  name: string
  status: "DRAFT" | "ACTIVE" | "RETIRED"
  createdAt: string
}
export interface Enrollment {
  id: string
  learnerId: string
  learnerEmail: string
  pathVersionId: string
  versionNo: number
  pathTitle: string
  status: string
  enrolledAt: string
}
export interface Eligibility {
  id: string
  certificationProgramId: string
  learnerUserId: string
  enrollmentId: string
  status: "ELIGIBLE" | "NOT_ELIGIBLE"
  evidenceSnapshotJson: string
  evaluatedAt: string
}
export interface Certificate {
  id: string
  certificationProgramId: string
  learnerUserId: string
  serialNumber: string
  status: "ISSUED" | "REVOKED"
  issuedAt: string
  revokedAt: string | null
  revocationReason: string | null
  organizationId: string | null
  programName: string | null
  learningPathVersionId: string | null
  issuerName: string | null
  learnerEmail: string | null
}
export interface PublicCertificate {
  serialNumber: string
  status: "ISSUED" | "REVOKED"
  issuedAt: string
  revokedAt: string | null
  issuerName: string | null
  programName: string | null
  learningPathVersionId: string | null
  proofStatus: "NOT_ANCHORED"
}
const enc = encodeURIComponent
export const certificationApi = {
  sources: (org: string) =>
    apiClient.get<{ id: string; title: string; versionNo: number }[]>(
      `/organizations/${enc(org)}/certification-program-sources`,
    ),
  programs: (org: string) =>
    apiClient.get<Program[]>(
      `/organizations/${enc(org)}/certification-programs`,
    ),
  create: (
    org: string,
    data: { name: string; learningPathVersionId: string },
  ) =>
    apiClient.post<Program>(
      `/organizations/${enc(org)}/certification-programs`,
      data,
    ),
  retire: (id: string) =>
    apiClient.post<Program>(`/certification-programs/${enc(id)}/retire`),
  enrollments: (program: string, query = "", page = 0) =>
    apiClient.get<Page<Enrollment>>(
      `/certification-programs/${enc(program)}/enrollments?query=${enc(query)}&page=${page}&size=20`,
    ),
  evaluate: (program: string, enrollmentId: string) =>
    apiClient.post<Eligibility>(
      `/certification-programs/${enc(program)}/eligibility/evaluate`,
      { enrollmentId },
    ),
  issue: (id: string) =>
    apiClient.post<Certificate>(
      `/certification-eligibility/${enc(id)}/certificate`,
    ),
  search: (org: string, status = "", learner = "", page = 0, query = "") =>
    apiClient.get<Page<Certificate>>(
      `/organizations/${enc(org)}/certificates/search?${new URLSearchParams({ page: String(page), size: "20", ...(query ? { query } : {}), ...(status ? { status } : {}), ...(learner ? { learnerId: learner } : {}) })}`,
    ),
  detail: (id: string) =>
    apiClient.get<Certificate>(`/certificates/${enc(id)}`),
  revoke: (id: string, reason: string) =>
    apiClient.post<Certificate>(`/certificates/${enc(id)}/revoke`, { reason }),
  verify: (serial: string) =>
    apiClient.get<PublicCertificate>(`/public/certificates/${enc(serial)}`),
}
