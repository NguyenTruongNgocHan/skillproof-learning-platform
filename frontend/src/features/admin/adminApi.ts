import { apiClient } from "@/services/api/apiClient"

export interface Account {
  id: string
  email: string
  displayName: string
  role: "LEARNER" | "ORGANIZER" | "ADMIN"
  status: "ACTIVE" | "DISABLED" | "PENDING_VERIFICATION"
}

interface AccountPage {
  content: Account[]
  totalElements: number
}

export const adminApi = {
  accounts: (size = 50) => apiClient.get<AccountPage>(`/admin/accounts?size=${size}`),
  changeRole: (accountId: string, role: Account["role"]) =>
    apiClient.patch(`/admin/accounts/${accountId}/role`, { role }),
  changeStatus: (accountId: string, status: Account["status"]) =>
    apiClient.patch(`/admin/accounts/${accountId}/status`, { status }),
}
