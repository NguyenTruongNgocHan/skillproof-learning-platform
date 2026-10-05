import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
import { organizationApi } from "@/features/organization/organizationApi"
import Button from "@/components/ui/Button"
import "@/styles/organizer.css"
export default function AcceptInvitationPage() {
  const { token } = useParams()
  const navigate = useNavigate()
  const { refresh, select } = useOrganizationContext()
  const [invitation, setInvitation] = useState<Awaited<
    ReturnType<typeof organizationApi.previewInvitation>
  > | null>(null)
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const [done, setDone] = useState(false)
  const [loading, setLoading] = useState(true)
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    setLoading(true)
    setError("")
    if (token)
      organizationApi
        .previewInvitation(token)
        .then((r) => {
          if (active) setInvitation(r)
        })
        .catch((e) => {
          if (active) setError(e.message)
        })
        .finally(() => {
          if (active) setLoading(false)
        })
    return () => {
      active = false
    }
  }, [token, retry])
  async function accept() {
    if (!token || busy) return
    setBusy(true)
    setError("")
    try {
      await organizationApi.acceptInvitation(token)
      await refresh()
      if (invitation) select(invitation.organizationId)
      setInvitation((previous) =>
        previous ? { ...previous, status: "ACCEPTED" } : null,
      )
      setDone(true)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to accept invitation")
    } finally {
      setBusy(false)
    }
  }
  return (
    <main className="sporg">
      <section className="sporg-card">
        <p className="sporg-eyebrow">Organization invitation</p>
        <h1>
          {done
            ? "You have joined the organization"
            : (invitation?.organizationName ?? "Review your invitation")}
        </h1>
        {error && (
          <p role="alert" className="sporg-alert">
            {error}
            <Button variant="outline" onClick={() => setRetry((x) => x + 1)}>
              Retry
            </Button>
          </p>
        )}
        {loading && <p role="status">Loading invitation…</p>}
        {invitation && (
          <>
            <p>
              Status: {invitation.status}. Expires{" "}
              {new Date(invitation.expiresAt).toLocaleString()}.
            </p>
            <p>
              Your manager will grant access to the work you are responsible
              for.
            </p>
          </>
        )}
        <div className="sporg-actions">
          {!done && invitation?.status === "PENDING" && (
            <Button disabled={busy} onClick={() => void accept()}>
              {busy ? "Accepting…" : "Accept invitation"}
            </Button>
          )}
          <Button variant="outline" onClick={() => navigate("/organizer")}>
            Open workspace
          </Button>
        </div>
      </section>
    </main>
  )
}
