import type { Profile } from "@/features/profile/types/profile.types"
import { apiClient } from "@/shared/api/apiClient"

export const profileApi = {
  me: () => apiClient.get<Profile>("/me"),
  update: (profile: Profile) => apiClient.patch<Profile>("/me/profile", profile),
}
