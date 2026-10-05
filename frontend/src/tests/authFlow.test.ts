import { describe, it, expect } from "vitest"
import {
  getDefaultRouteForRole,
  getNextRouteAfterLogin,
  getNextRouteAfterVerification,
  canAccessRole,
} from "@/utils/authFlow"
import type { User } from "@/features/auth/types/auth.types"

function makeUser(overrides: Partial<User> = {}): User {
  return {
    id: "test-1",
    email: "test@example.com",
    fullName: "Test User",
    role: "LEARNER",
    emailVerificationStatus: "VERIFIED",
    onboardingStatus: "COMPLETED",
    ...overrides,
  }
}

describe("getDefaultRouteForRole", () => {
  it("returns /app for LEARNER", () => {
    expect(getDefaultRouteForRole("LEARNER")).toBe("/app")
  })
  it("routes ORGANIZER through server review status", () => {
    expect(getDefaultRouteForRole("ORGANIZER")).toBe("/organizer")
  })
  it("returns /admin for ADMIN", () => {
    expect(getDefaultRouteForRole("ADMIN")).toBe("/admin")
  })
})

describe("getNextRouteAfterLogin", () => {
  it("redirects to /verify-email if email is UNVERIFIED", () => {
    const user = makeUser({ emailVerificationStatus: "UNVERIFIED" })
    expect(getNextRouteAfterLogin(user)).toBe("/verify-email")
  })

  it("allows learner to enter the app without mandatory onboarding", () => {
    const user = makeUser({ onboardingStatus: "NOT_STARTED" })
    expect(getNextRouteAfterLogin(user)).toBe("/app")
  })

  it("redirects completed learner to /app", () => {
    const user = makeUser()
    expect(getNextRouteAfterLogin(user)).toBe("/app")
  })

  it("checks organizer status on the server even when onboarding is incomplete", () => {
    const user = makeUser({
      role: "ORGANIZER",
      onboardingStatus: "NOT_STARTED",
    })
    expect(getNextRouteAfterLogin(user)).toBe("/organizer")
  })

  it("routes completed organizer to server status check", () => {
    const user = makeUser({ role: "ORGANIZER" })
    expect(getNextRouteAfterLogin(user)).toBe("/organizer")
  })

  it("redirects admin to /admin", () => {
    const user = makeUser({ role: "ADMIN" })
    expect(getNextRouteAfterLogin(user)).toBe("/admin")
  })
})

describe("getNextRouteAfterVerification", () => {
  it("sends learner directly to the app after verification", () => {
    const user = makeUser({ onboardingStatus: "NOT_STARTED" })
    expect(getNextRouteAfterVerification(user)).toBe("/app")
  })

  it("sends learner with COMPLETED onboarding to /app", () => {
    const user = makeUser()
    expect(getNextRouteAfterVerification(user)).toBe("/app")
  })

  it("sends organizer to server-backed status check if NOT_STARTED", () => {
    const user = makeUser({
      role: "ORGANIZER",
      onboardingStatus: "NOT_STARTED",
    })
    expect(getNextRouteAfterVerification(user)).toBe("/organizer")
  })
})

describe("canAccessRole", () => {
  it("returns false for null user", () => {
    expect(canAccessRole(null, "LEARNER")).toBe(false)
  })

  it("returns true when user role matches", () => {
    const user = makeUser({ role: "ADMIN" })
    expect(canAccessRole(user, "ADMIN")).toBe(true)
  })

  it("returns false when role does not match", () => {
    const user = makeUser({ role: "LEARNER" })
    expect(canAccessRole(user, "ADMIN")).toBe(false)
  })
})
