import { ReactNode } from "react"
import { Navigate } from "react-router-dom"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"

export function OrganizerVerificationGuard({
  children,
}: {
  children: ReactNode
}) {
  const { user } = useAuth()
  const { organization, pending, loading, error, refresh } =
    useOrganizationContext()

  if (!user) return <Navigate to="/login" replace />
  if (user.role !== "ORGANIZER") return <Navigate to="/" replace />
  if (loading)
    return <main className="org-state">Checking organization access…</main>
  if (error) {
    return (
      <main className="org-state" role="alert">
        {error} <button onClick={() => void refresh()}>Retry</button>
      </main>
    )
  }
  if (!organization) {
    return (
      <Navigate
        to={
          pending && pending.status !== "DRAFT"
            ? "/organizer/verification-pending"
            : "/onboarding/organizer"
        }
        replace
      />
    )
  }
  return <div key={organization.id}>{children}</div>
}
