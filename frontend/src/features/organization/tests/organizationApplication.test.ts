import { describe, expect, it } from "vitest"
import { normalizeApplication, validateApplication } from "../validation/organizationApplication"
const valid = {
  legalName: "SkillProof",
  displayName: "SkillProof",
  industry: "Education",
  country: "VN",
  contactName: "Owner",
  contactEmail: "owner@example.org",
}
describe("Application input boundaries", () => {
  it("accepts a complete application without optional fields", () =>
    expect(validateApplication(valid)).toEqual({}))
  it("rejects whitespace-only required details and oversized input", () => {
    const errors = validateApplication({
      ...valid,
      legalName: "   ",
      country: "x".repeat(121),
    })
    expect(errors).toHaveProperty("legalName")
    expect(errors).toHaveProperty("country")
  })
  it("rejects unsupported website protocols", () =>
    expect(validateApplication({ ...valid, website: "ftp://example.org" })).toHaveProperty(
      "website",
    ))
  it("trims before sending input to the backend", () =>
    expect(normalizeApplication({ ...valid, legalName: "  SkillProof  " }).legalName).toBe(
      "SkillProof",
    ))
})
