import { apiClient } from "@/services/api/apiClient"

export type InterestSource =
  | "USER_SELECTED"
  | "USER_CONFIRMED_AI"

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

const ENDPOINT = "/me/learning-preferences"

export const learnerDiscoveryApi = {
  get(): Promise<LearnerDiscoveryProfile> {
    return apiClient.get<LearnerDiscoveryProfile>(ENDPOINT)
  },

  update(
    input: UpdateLearnerDiscoveryProfile,
  ): Promise<LearnerDiscoveryProfile> {
    return apiClient.put<LearnerDiscoveryProfile>(ENDPOINT, input)
  },

  clear(): Promise<void> {
    return apiClient.delete<void>(ENDPOINT)
  },
}