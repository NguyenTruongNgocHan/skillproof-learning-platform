import { describe, expect, it } from "vitest"
import type { Organization } from "../types/organization.types"
import { selectOrganizations } from "../utils/organizationSelection"
const item = (id: string, ownerUserId: string, status: Organization["status"]) =>
  ({ id, ownerUserId, status }) as Organization

describe("Owned application and workspace boundaries", () => {
  it("keeps a rejected owned application separate from an approved membership", () => {
    const owned = item("owned", "me", "REJECTED")
    const selected = item("member", "another", "APPROVED")
    const result = selectOrganizations([owned, selected], "me", "member")
    expect(result.owned).toBe(owned)
    expect(result.pending).toBe(owned)
    expect(result.selected).toBe(selected)
    expect(result.approved).toEqual([selected])
  })
  it("does not interpret a member organization as the account's application", () => {
    const result = selectOrganizations([item("member", "another", "APPROVED")], "me", null)
    expect(result.owned).toBeNull()
    expect(result.pending).toBeNull()
    expect(result.selected?.id).toBe("member")
  })
  it("drops a saved workspace which is no longer approved", () => {
    const result = selectOrganizations(
      [item("old", "another", "PENDING"), item("new", "other", "APPROVED")],
      "me",
      "old",
    )
    expect(result.selected?.id).toBe("new")
  })
})
