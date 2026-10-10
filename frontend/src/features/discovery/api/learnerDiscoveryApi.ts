import type {
  LearnerDiscoveryProfile,
  UpdateLearnerDiscoveryProfile,
} from "@/features/discovery/types/learnerDiscovery.types"
import { apiClient } from "@/shared/api/apiClient"

const ENDPOINT = "/me/learning-preferences"

export const learnerDiscoveryApi = {
  get(): Promise<LearnerDiscoveryProfile> {
    return apiClient.get<LearnerDiscoveryProfile>(ENDPOINT)
  },

  update(input: UpdateLearnerDiscoveryProfile): Promise<LearnerDiscoveryProfile> {
    return apiClient.put<LearnerDiscoveryProfile>(ENDPOINT, input)
  },

  clear(): Promise<void> {
    return apiClient.delete<void>(ENDPOINT)
  },
}
