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
