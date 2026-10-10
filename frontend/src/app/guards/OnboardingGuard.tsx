import type { ReactNode } from "react"
import { Navigate } from "react-router-dom"

import { useAuth } from "@/features/auth/hooks/useAuth"

export function OnboardingGuard({ children }: { children: ReactNode }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (user.emailVerificationStatus === "UNVERIFIED") {
    return <Navigate to="/verify-email" replace />
  }

  if (user.role === "ORGANIZER" && user.onboardingStatus !== "COMPLETED") {
    return <Navigate to="/onboarding/organizer" replace />
  }

  return <>{children}</>
}
