import { useAuth } from "@/features/auth/hooks/useAuth"
import { ReactNode } from "react"
import { Navigate, useLocation } from "react-router-dom"

export function AuthGuard({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading, user } = useAuth()
  const location = useLocation()
  if (isLoading) return null
  if (!isAuthenticated) {
    const returnTo = `${location.pathname}${location.search}`
    return (
      <Navigate
        to={`/login?returnTo=${encodeURIComponent(returnTo)}`}
        state={{ from: location }}
        replace
      />
    )
  }
  if (user?.emailVerificationStatus === "UNVERIFIED") {
    const destination = `${location.pathname}${location.search}`
    if (destination.startsWith("/organizer/invitations/"))
      sessionStorage.setItem("skillproof.auth.returnTo", destination)
    return <Navigate to="/verify-email" replace />
  }
  return <>{children}</>
}
