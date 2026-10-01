import type { User, UserRole } from "@/features/auth/types/auth.types"

export function getDefaultRouteForRole(role: UserRole): string {
  switch (role) {
    case "ADMIN":
      return "/admin"
    case "ORGANIZER":
      return "/organizer/verification-pending"
    case "LEARNER":
      return "/app"
  }
}

export function getNextRouteAfterLogin(user: User): string {
  if (user.emailVerificationStatus === "UNVERIFIED") return "/verify-email"
  return getNextRouteAfterVerification(user)
}

export function getNextRouteAfterVerification(user: User): string {
  if (user.role === "LEARNER") {
    return "/app"
  }

  if (user.role === "ORGANIZER") {
    return "/organizer/verification-pending"
  }

  return "/admin"
}

export function getNextRouteForUserState(user: User | null): string {
  if (!user) return "/login"
  return getNextRouteAfterLogin(user)
}

export function canAccessRole(
  user: User | null,
  requiredRole: UserRole,
): boolean {
  return !!user && user.role === requiredRole
}
