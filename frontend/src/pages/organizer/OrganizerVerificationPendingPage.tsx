import { Navigate, Link } from "react-router-dom"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
import { useAuth } from "@/features/auth/hooks/useAuth"
import BrandLogo from "@/components/ui/BrandLogo"
import ThemeSwitcher from "@/components/ui/ThemeSwitcher"
import Button from "@/components/ui/Button"
import { ApplicationHistory } from "@/features/organizer/ApplicationHistory"
import "@/styles/organizer.css"
export default function OrganizerVerificationPendingPage() {
  const { owned, organization, loading, error, refresh } =
    useOrganizationContext()
  const { logout } = useAuth()
  if (loading)
    return (
      <main className="sporg" role="status">
        Checking your application…
      </main>
    )
  if (error)
    return (
      <main className="sporg">
        <p role="alert">{error}</p>
        <Button onClick={() => void refresh()}>Retry</Button>
      </main>
    )
  if (owned?.status === "APPROVED") return <Navigate to="/organizer" replace />
  if (!owned || owned.status === "DRAFT")
    return <Navigate to="/onboarding/organizer" replace />
  return (
    <div className="sporg-shell">
      <header className="sporg-shell-header">
        <BrandLogo />
        <ThemeSwitcher compact />
      </header>
      <main className="sporg">
        <header className="sporg-heading">
          <p className="sporg-eyebrow">{owned.status}</p>
          <h1>
            {owned.status === "REJECTED"
              ? "Your application needs changes"
              : "Your application is under review"}
          </h1>
          <p>
            {owned.reviewReason ??
              "An administrator will review your submitted information and documents."}
          </p>
        </header>
        <div className="sporg-actions">
          {owned.status === "REJECTED" && (
            <Button asChild>
              <Link to="/onboarding/organizer">Prepare another submission</Link>
            </Button>
          )}
          {organization && (
            <Button asChild variant="outline">
              <Link to="/organizer">Open approved workspace</Link>
            </Button>
          )}
          <Button variant="outline" onClick={() => void refresh()}>
            Check status
          </Button>
          <Button variant="ghost" onClick={() => void logout()}>
            Sign out
          </Button>
        </div>
        <ApplicationHistory organizationId={owned.id} />
      </main>
    </div>
  )
}
