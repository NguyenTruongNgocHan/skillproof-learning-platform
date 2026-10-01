import { apiClient } from "@/services/api/apiClient"

export interface Profile {
  email: string
  displayName: string
  role: string
  headline?: string
  bio?: string
  avatarUrl?: string
  locale: string
  timezone: string
}

export const profileApi = {
  me: () => apiClient.get<Profile>("/me"),
  update: (profile: Profile) => apiClient.patch<Profile>("/me/profile", profile),
}
