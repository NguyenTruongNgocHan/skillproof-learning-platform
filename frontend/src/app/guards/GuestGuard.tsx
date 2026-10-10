import { useAuth } from "@/features/auth/hooks/useAuth"
import { safeDestination } from "@/features/auth/utils/authDestination"
import { getNextRouteAfterLogin } from "@/features/auth/utils/authFlow"
import { ReactNode } from "react"
import { Navigate, useLocation } from "react-router-dom"

export function GuestGuard({ children }: { children: ReactNode }) {
  const location = useLocation()
  const destination = safeDestination(
    new URLSearchParams(location.search).get("returnTo") ??
      sessionStorage.getItem("skillproof.auth.returnTo"),
  )
  const { isAuthenticated, isLoading, user } = useAuth()
  if (isLoading) return null
  if (isAuthenticated && user)
    return (
      <Navigate
        to={
          user.emailVerificationStatus === "VERIFIED" && destination
            ? destination
            : getNextRouteAfterLogin(user)
        }
        replace
      />
    )
  return <>{children}</>
}
