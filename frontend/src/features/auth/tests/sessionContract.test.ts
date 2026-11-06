import { afterEach, describe, expect, it, vi } from "vitest"
import { httpAuthRepository } from "../repositories/HttpAuthRepository"
import { setAccessToken } from "@/shared/api/apiClient"
afterEach(() => {
  vi.restoreAllMocks()
  setAccessToken(null)
})
describe("server onboarding status", () => {
  it.each(["NOT_STARTED", "IN_PROGRESS", "COMPLETED"] as const)(
    "preserves %s on login",
    async (status) => {
      vi.spyOn(globalThis, "fetch").mockResolvedValue(
        new Response(
          JSON.stringify({
            accessToken: "test",
            expiresIn: 60,
            user: {
              id: "learner",
              email: "l@example.org",
              displayName: "Learner",
              role: "LEARNER",
              status: "ACTIVE",
              onboardingStatus: status,
            },
          }),
          { headers: { "Content-Type": "application/json" } },
        ),
      )
      const result = await httpAuthRepository.login({ email: "l@example.org", password: "test" })
      expect(result.user.onboardingStatus).toBe(status)
    },
  )
})
