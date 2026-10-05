import { Navigate } from "react-router-dom"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"

export default function OrganizerEntryPage() {
  const { user } = useAuth()
  const { organization, pending, loading, error, refresh } = useOrganizationContext()
  if (!user) return <Navigate to="/login" replace />
  if (loading) return <main className="org-state">Loading your organization workspace…</main>
  if (error) {
    return <main className="org-state" role="alert">{error} <button onClick={() => void refresh()}>Retry</button></main>
  }
  if (organization) return <Navigate to="/organizer/workspace" replace />
  if (pending?.status === "DRAFT") return <Navigate to="/onboarding/organizer" replace />
  if (pending) return <Navigate to="/organizer/verification-pending" replace />
  return <Navigate to="/onboarding/organizer" replace />
}
