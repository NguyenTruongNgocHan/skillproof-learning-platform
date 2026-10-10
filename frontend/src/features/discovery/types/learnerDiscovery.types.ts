export type InterestSource = "USER_SELECTED" | "USER_CONFIRMED_AI"

export interface LearnerInterest {
  label: string
  source: InterestSource
}

export interface LearnerDiscoveryProfile {
  configured: boolean
  personalizationEnabled: boolean
  explorationMode: boolean
  goalText: string | null
  experienceLevel: string | null
  interests: LearnerInterest[]
  updatedAt: string | null
}

export interface UpdateLearnerDiscoveryProfile {
  personalizationEnabled?: boolean
  explorationMode?: boolean
  goalText?: string | null
  experienceLevel?: string | null
  interests?: string[]
}
