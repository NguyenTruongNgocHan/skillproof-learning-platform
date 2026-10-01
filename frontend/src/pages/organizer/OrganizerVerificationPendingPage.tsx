import MediaPanel from "@/features/media/MediaPanel"
import { useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { Clock3, CircleCheck, CircleX, LogOut, RefreshCw } from "lucide-react"
import BrandLogo from "@/components/ui/BrandLogo"
import ThemeSwitcher from "@/components/ui/ThemeSwitcher"
import Button from "@/components/ui/Button"
import { useAuth } from "@/features/auth/hooks/useAuth"
import {
  organizationApi,
  type Organization,
} from "@/features/organization/organizationApi"
import { ApiError } from "@/services/api/apiClient"

export default function OrganizerVerificationPendingPage() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [org, setOrg] = useState<Organization | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  async function load() {
    setLoading(true)
    setError("")
    try {
      const value = await organizationApi.mine()
      setOrg(value)
      if (value.status === "APPROVED") navigate("/organizer", { replace: true })
    } catch (reason) {
      if (reason instanceof ApiError && reason.status === 404) setOrg(null)
      else
        setError(
          reason instanceof Error
            ? reason.message
            : "Could not check your application.",
        )
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [])
  return (
    <div className="org-application-page">
      <header className="org-application-header">
        <BrandLogo />
        <ThemeSwitcher compact />
      </header>
      <main className="org-status-card">
        {loading ? (
          <p role="status">Checking your application…</p>
        ) : error ? (
          <>
            <h1>Could not check your status</h1>
            <p role="alert">{error}</p>
            <Button onClick={() => void load()}>
              <RefreshCw size={17} /> Retry
            </Button>
          </>
        ) : !org ? (
          <>
            <Clock3 size={38} />
            <h1>Start your organization application.</h1>
            <p>
              Submit organization details for administrator review before
              accessing an organizer workspace.
            </p>
            <Button asChild>
              <Link to="/onboarding/organizer">Apply now</Link>
            </Button>
          </>
        ) : org.status === "REJECTED" ? (
          <>
            <CircleX size={42} className="org-status-rejected" />
            <h1>Your application needs changes.</h1>
            <p>
              {org.reviewReason ||
                "The administrator requested changes before another review."}
            </p>
            <Button asChild>
              <Link to="/onboarding/organizer">Update and resubmit</Link>
            </Button>
          </>
        ) : (
          <>
            <CircleCheck size={42} className="org-status-pending" />
            <span className="auth-intro__eyebrow">APPLICATION RECEIVED</span>
            <h1>{org.displayName} is under review.</h1>
            <p>
              Your application was received. We will show the decision here once
              an administrator has reviewed it.
            </p>
            <p>Submitted {new Date(org.createdAt).toLocaleDateString()}</p>
            <Button variant="outline" onClick={() => void load()}>
              <RefreshCw size={17} /> Check status
            </Button>
          </>
        )}
        {org && <MediaPanel scope="ORGANIZATION" target={org.id} writable />}
        <Button
          variant="ghost"
          onClick={async () => {
            await logout()
            navigate("/login", { replace: true })
          }}
        >
          <LogOut size={16} /> Sign out
        </Button>
      </main>
    </div>
  )
}
