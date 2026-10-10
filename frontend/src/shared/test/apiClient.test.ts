import { apiClient, setAccessToken } from "@/shared/api/apiClient"
import { afterEach, describe, expect, it, vi } from "vitest"

function response(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  })
}

describe("apiClient", () => {
  afterEach(() => {
    setAccessToken(null)
    vi.restoreAllMocks()
  })

  it("preserves login errors without attempting refresh", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      response(401, {
        code: "IDENTITY_INVALID_CREDENTIALS",
        message: "Email or password is incorrect.",
      }),
    )

    await expect(
      apiClient.post("/auth/login", {
        email: "learner@example.com",
        password: "wrong",
      }),
    ).rejects.toMatchObject({
      status: 401,
      problem: { code: "IDENTITY_INVALID_CREDENTIALS" },
    })

    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it("refreshes once and retries an authenticated request", async () => {
    setAccessToken("expired-access-token")

    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(
        response(401, {
          code: "IDENTITY_UNAUTHORIZED",
          message: "Authentication is required.",
        }),
      )
      .mockResolvedValueOnce(
        response(200, {
          accessToken: "new-access-token",
          expiresIn: 900,
          user: {
            id: "user-1",
            email: "learner@example.com",
            displayName: "Learner",
            role: "LEARNER",
          },
        }),
      )
      .mockResolvedValueOnce(response(200, { email: "learner@example.com" }))

    await expect(apiClient.get("/me")).resolves.toEqual({
      email: "learner@example.com",
    })
    expect(fetchMock).toHaveBeenCalledTimes(3)
  })
})

describe("Concurrent refresh across JSON and media clients", () => {
  it("shares one refresh request for all callers", async () => {
    const { refreshAccessToken } = await import("@/shared/api/apiClient")
    const fetcher = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          accessToken: "new-token",
          expiresIn: 900,
          user: {
            id: "owner",
            email: "owner@example.org",
            displayName: "Owner",
            role: "ORGANIZER",
            status: "ACTIVE",
          },
        }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    )
    const [first, second] = await Promise.all([refreshAccessToken(), refreshAccessToken()])
    expect(fetcher).toHaveBeenCalledOnce()
    expect(first.accessToken).toBe("new-token")
    expect(second).toBe(first)
  })
})
