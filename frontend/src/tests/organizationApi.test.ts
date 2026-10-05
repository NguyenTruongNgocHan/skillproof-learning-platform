import { afterEach, describe, expect, it, vi } from "vitest"
import { certificationApi } from "@/features/certification/certificationApi"

describe("organization certification API", () => {
  afterEach(() => vi.restoreAllMocks())

  it("uses the organization-scoped program endpoint and real create payload", async () => {
    const programResponse = () =>
      new Response(JSON.stringify([{ id: "program-1", status: "ACTIVE" }]), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      })
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(programResponse())
      .mockResolvedValueOnce(programResponse())

    await certificationApi.programs("org/1")
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining("/organizations/org%2F1/certification-programs"),
      expect.objectContaining({ credentials: "include" }),
    )

    await certificationApi.create("org-1", {
      name: "Web foundations",
      learningPathVersionId: "version-1",
    })
    expect(fetchMock).toHaveBeenLastCalledWith(
      expect.stringContaining("/organizations/org-1/certification-programs"),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({
          name: "Web foundations",
          learningPathVersionId: "version-1",
        }),
      }),
    )
  })
})
