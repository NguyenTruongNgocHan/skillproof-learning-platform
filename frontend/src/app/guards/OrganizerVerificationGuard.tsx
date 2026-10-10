import { useAuth } from "@/features/auth/hooks/useAuth"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { OrganizerWorkspaceState } from "@/features/organization/components/OrganizerWorkspaceState"
import type { ReactNode } from "react"
import { Navigate } from "react-router-dom"

export function OrganizerVerificationGuard({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const { organization, pending, loading, error, refresh } = useOrganizationContext()

  if (!user) return <Navigate to="/login" replace />
  if (user.role !== "ORGANIZER") return <Navigate to="/" replace />
  if (loading || error) {
    return <OrganizerWorkspaceState loading={loading} error={error} retry={refresh} />
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
