import { safeDestination } from "@/features/auth/utils/authDestination"
import { certificationApi } from "@/features/certification/api/certificationApi"
import { organizationApi } from "@/features/organization/api/organizationApi"
import { setAccessToken } from "@/shared/api/apiClient"
import { afterEach, describe, expect, it, vi } from "vitest"
const ok = () =>
  new Response(JSON.stringify({}), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  })
afterEach(() => {
  vi.restoreAllMocks()
  setAccessToken(null)
})
describe("Organizer contracts", () => {
  it("saves a draft and explicitly submits it", async () => {
    const fetcher = vi.spyOn(globalThis, "fetch").mockImplementation(async () => ok())
    await organizationApi.saveDraft({
      legalName: "Example",
      displayName: "Example",
      industry: "Education",
      country: "VN",
      contactName: "Owner",
      contactEmail: "owner@example.org",
    })
    expect(fetcher).toHaveBeenLastCalledWith(
      expect.stringContaining("/organizations/mine/draft"),
      expect.objectContaining({ method: "PUT" }),
    )
    await organizationApi.submit()
    expect(fetcher).toHaveBeenLastCalledWith(
      expect.stringContaining("/organizations/mine/submit"),
      expect.objectContaining({ method: "POST" }),
    )
  })
  it("preview never accepts an invitation", async () => {
    const fetcher = vi.spyOn(globalThis, "fetch").mockImplementation(async () => ok())
    await organizationApi.previewInvitation("token/one")
    expect(fetcher).toHaveBeenLastCalledWith(
      expect.stringContaining("/organizations/invitations/token%2Fone"),
      expect.objectContaining({ credentials: "include" }),
    )
    expect(fetcher).toHaveBeenCalledOnce()
  })
  it("loads published sources using certificate authority without authoring endpoints", async () => {
    const fetcher = vi.spyOn(globalThis, "fetch").mockImplementation(async () => ok())
    await certificationApi.sources("org/one")
    expect(fetcher).toHaveBeenLastCalledWith(
      expect.stringContaining("/organizations/org%2Fone/certification-program-sources"),
      expect.objectContaining({ credentials: "include" }),
    )
  })
  it("searches certificates with server-side filters and pagination", async () => {
    const fetcher = vi.spyOn(globalThis, "fetch").mockImplementation(async () => ok())
    await certificationApi.search("org/one", "REVOKED", "learner", 2)
    const url = String(fetcher.mock.calls[0][0])
    expect(url).toContain("org%2Fone/certificates/search")
    expect(url).toContain("page=2")
    expect(url).toContain("status=REVOKED")
    expect(url).toContain("learnerId=learner")
  })
  it.each(["https://evil.example", "//evil.example", "/\\evil.example", "/\r\nunsafe"])(
    "rejects unsafe return destination %s",
    (value) => expect(safeDestination(value)).toBeNull(),
  )
  it("retains an internal invitation return path", () =>
    expect(safeDestination("/organizer/invitations/token/accept")).toBe(
      "/organizer/invitations/token/accept",
    ))
})
