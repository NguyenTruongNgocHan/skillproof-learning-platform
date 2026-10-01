import { apiClient } from "@/services/api/apiClient"

export interface CompletionEvidence {
  evaluationId: string
  enrollmentId: string
  learnerId: string
  pathVersionId: string
  completionPolicyId: string
  totalResources: number
  completedResources: number
  requiredAssessments: number
  passedAssessments: number
  completed: boolean
}

export interface EligibilityResult {
  id: string
  certificationProgramId: string
  learnerUserId: string
  enrollmentId: string
  status: "ELIGIBLE" | "NOT_ELIGIBLE"
  evidenceSnapshotJson: string
  evaluatedAt: string
}

export const eligibilityApi = {
  evaluateCompletion: (enrollmentId: string) =>
    apiClient.post<CompletionEvidence>(
      `/learning/me/enrollments/${enrollmentId}/completion/evaluate`,
    ),

  evaluateEligibility: (programId: string, enrollmentId: string) =>
    apiClient.post<EligibilityResult>(
      `/certification-programs/${programId}/eligibility/evaluate`,
      { enrollmentId },
    ),

  getLatestEligibility: (programId: string, learnerId: string) =>
    apiClient.get<EligibilityResult>(
      `/certification-programs/${programId}/eligibility/${learnerId}`,
    ),
}
