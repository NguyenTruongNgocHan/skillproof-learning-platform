import { useAuth } from "@/features/auth/hooks/useAuth"
import { ApplicationHistory } from "@/features/organization/components/ApplicationHistory"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import "@/features/organization/styles/organizer.css"
import BrandLogo from "@/shared/ui/BrandLogo"
import Button from "@/shared/ui/Button"
import ThemeSwitcher from "@/shared/ui/ThemeSwitcher"
import { useState } from "react"
import { Link, Navigate } from "react-router-dom"
export default function OrganizerVerificationPendingPage() {
  const { owned, organization, loading, error, refresh } = useOrganizationContext()
  const { logout } = useAuth()
  const [signingOut, setSigningOut] = useState(false)
  const [actionError, setActionError] = useState("")
  async function signOut() {
    if (signingOut) return
    setSigningOut(true)
    setActionError("")
    try {
      await logout()
    } catch (cause) {
      setActionError(cause instanceof Error ? cause.message : "Unable to sign out. Please retry.")
    } finally {
      setSigningOut(false)
    }
  }
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
  if (!owned || owned.status === "DRAFT") return <Navigate to="/onboarding/organizer" replace />
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
        {actionError && (
          <p className="sporg-alert" role="alert">
            {actionError}
          </p>
        )}
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
          <Button variant="ghost" disabled={signingOut} onClick={() => void signOut()}>
            {signingOut ? "Signing out…" : "Sign out"}
          </Button>
        </div>
        <section className="sporg-card">
          <h2>Submitted organization</h2>
          <dl>
            <dt>Legal name</dt>
            <dd>{owned.legalName}</dd>
            <dt>Public name</dt>
            <dd>{owned.displayName}</dd>
            <dt>Contact</dt>
            <dd>
              {owned.contactName} · {owned.contactEmail}
            </dd>
            <dt>Industry / country</dt>
            <dd>
              {owned.industry} / {owned.country}
            </dd>
          </dl>
        </section>
        <ApplicationHistory organizationId={owned.id} refreshKey={owned.updatedAt} />
      </main>
    </div>
  )
}
