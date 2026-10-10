import type {
  CompletionEvidence,
  EligibilityResult,
} from "@/features/eligibility/types/eligibility.types"
import { apiClient } from "@/shared/api/apiClient"

export const eligibilityApi = {
  evaluateCompletion: (enrollmentId: string) =>
    apiClient.post<CompletionEvidence>(
      `/learning/me/enrollments/${enrollmentId}/completion/evaluate`,
    ),

  evaluateEligibility: (programId: string, enrollmentId: string) =>
    apiClient.post<EligibilityResult>(`/certification-programs/${programId}/eligibility/evaluate`, {
      enrollmentId,
    }),

  getLatestEligibility: (programId: string, learnerId: string) =>
    apiClient.get<EligibilityResult>(
      `/certification-programs/${programId}/eligibility/${learnerId}`,
    ),
}
