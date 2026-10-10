import type { Organization } from "../types/organization.types"

export function selectOrganizations(
  all: Organization[],
  accountId: string,
  preferredId: string | null,
) {
  const approved = all.filter((item) => item.status === "APPROVED")
  const owned = all.find((item) => item.ownerUserId === accountId) ?? null
  return {
    approved,
    owned,
    pending: owned && owned.status !== "APPROVED" ? owned : null,
    selected: approved.find((item) => item.id === preferredId) ?? approved[0] ?? null,
  }
}
