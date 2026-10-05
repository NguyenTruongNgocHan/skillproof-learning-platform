import { safeDestination } from "@/utils/authDestination"
import { Navigate, useLocation } from "react-router-dom"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { getNextRouteAfterLogin } from "@/utils/authFlow"
import { ReactNode } from "react"

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
