import type { Account, AccountPage } from "@/features/admin/types/admin.types"
import { apiClient } from "@/shared/api/apiClient"

export const adminApi = {
  accounts: (size = 50) => apiClient.get<AccountPage>(`/admin/accounts?size=${size}`),
  changeRole: (accountId: string, role: Account["role"]) =>
    apiClient.patch(`/admin/accounts/${accountId}/role`, { role }),
  changeStatus: (accountId: string, status: Account["status"]) =>
    apiClient.patch(`/admin/accounts/${accountId}/status`, { status }),
}
