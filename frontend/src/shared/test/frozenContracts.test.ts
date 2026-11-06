import { profileApi } from "@/features/profile/api/profileApi"
import { afterEach, describe, expect, it, vi } from "vitest"
import { pricingApi } from "@/features/pricing/api/pricingApi"
import { learnerDiscoveryApi } from "@/features/discovery/api/learnerDiscoveryApi"
import { governanceApi } from "@/features/organization/api/governanceApi"
import { setAccessToken } from "../api/apiClient"
afterEach(() => {
  vi.restoreAllMocks()
  setAccessToken(null)
})
describe("variable-path frozen contracts", () => {
  it.each([
    ["GET", "/course-workspace/versions/v%2F1/commercial", () => pricingApi.get("v/1")],
    [
      "PUT",
      "/course-workspace/versions/v%2F1/commercial",
      () =>
        pricingApi.save("v/1", {
          accessMode: "PUBLIC",
          priceVnd: 10000,
          certificationPriceVnd: 0,
          reason: "Review",
        }),
    ],
    [
      "PUT",
      "/course-workspace/versions/v%2F1/lifecycle",
      () => pricingApi.lifecycle("v/1", "CLOSED"),
    ],
    [
      "POST",
      "/course-workspace/versions/v%2F1/promotions",
      () =>
        pricingApi.promote("v/1", {
          name: "Offer",
          percent: 10,
          startsAt: "2027-01-01T00:00:00Z",
          endsAt: "2027-02-01T00:00:00Z",
        }),
    ],
    [
      "DELETE",
      "/course-workspace/versions/v%2F1/promotions/p%2F1",
      () => pricingApi.cancel("v/1", "p/1"),
    ],
    ["GET", "/me/learning-preferences", () => learnerDiscoveryApi.get()],
    [
      "PUT",
      "/me/learning-preferences",
      () => learnerDiscoveryApi.update({ goalText: "Learn Java", interests: ["Backend"] }),
    ],
    ["DELETE", "/me/learning-preferences", () => learnerDiscoveryApi.clear()],
  ] as const)("uses %s %s", async (method, path, call) => {
    const fetcher = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(new Response("{}", { headers: { "Content-Type": "application/json" } }))
    await call()
    expect(String(fetcher.mock.calls[0][0])).toContain(path)
    expect(fetcher.mock.calls[0][1]?.method ?? "GET").toBe(method)
  })
  it("does not carry previous evidence decisions into a revision adapter", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify([
          {
            evidenceId: "e",
            mediaId: "m",
            kind: "COMPANY_IMAGE",
            status: "UPLOADED",
            sha256: "hash",
            title: "Image",
            expiresAt: null,
            reason: null,
          },
        ]),
        { headers: { "Content-Type": "application/json" } },
      ),
    )
    const rows = await governanceApi.verificationSnapshot("revision", "org")
    expect(rows[0]).toMatchObject({
      id: "e",
      organizationId: "org",
      status: "UPLOADED",
      sha256: "hash",
    })
  })
  it("never sends role or email as profile mutations", async () => {
    const fetcher = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(new Response("{}", { headers: { "Content-Type": "application/json" } }))
    await profileApi.update({
      email: "immutable@example.org",
      role: "ADMIN",
      displayName: "User",
      headline: "Headline",
      bio: "Bio",
      avatarUrl: "",
      locale: "en",
      timezone: "Asia/Ho_Chi_Minh",
    })
    const body = JSON.parse(String(fetcher.mock.calls[0][1]?.body))
    expect(body).not.toHaveProperty("role")
    expect(body).not.toHaveProperty("email")
    expect(body.displayName).toBe("User")
  })
})
