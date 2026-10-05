import { useEffect, useState } from "react"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import { adminOrganizationApi } from "@/features/admin/adminOrganizationApi"
import { ApplicationHistory } from "@/features/organizer/ApplicationHistory"
import { mediaApi } from "@/features/media/mediaApi"
import type {
  Organization,
  ApplicationRevision,
} from "@/features/organization/organizationApi"
import "@/styles/organizer.css"

export default function AdminOrganizationReviewPage() {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [selected, setSelected] = useState<Organization | null>(null)
  const [revision, setRevision] = useState<ApplicationRevision | null>(null)
  const [reason, setReason] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [reload, setReload] = useState(0)
  useEffect(() => {
    let live = true
    setLoading(true)
    adminOrganizationApi
      .pending()
      .then((rows) => {
        if (live) {
          setOrganizations(rows)
          setError("")
        }
      })
      .catch((e) => {
        if (live) setError(e.message)
      })
      .finally(() => {
        if (live) setLoading(false)
      })
    return () => {
      live = false
    }
  }, [reload])
  useEffect(() => {
    let live = true
    setRevision(null)
    setReason("")
    setError("")
    if (selected)
      adminOrganizationApi
        .revisions(selected.id)
        .then((rows) => {
          if (live)
            setRevision(
              [...rows].sort((a, b) => b.revisionNo - a.revisionNo)[0] ?? null,
            )
        })
        .catch((e) => {
          if (live) setError(e.message)
        })
    return () => {
      live = false
    }
  }, [selected])
  async function review(decision: "APPROVED" | "REJECTED") {
    if (!revision || busy || (decision === "REJECTED" && !reason.trim())) return
    if (
      !window.confirm(
        `${decision === "APPROVED" ? "Approve" : "Reject"} revision ${revision.revisionNo} of ${revision.displayName}?`,
      )
    )
      return
    setBusy(true)
    setError("")
    try {
      await adminOrganizationApi.review(revision.id, decision, reason.trim())
      setSelected(null)
      setReload((x) => x + 1)
    } catch (e) {
      setError(
        e instanceof Error
          ? e.message
          : "Review failed; refresh to check the current revision.",
      )
    } finally {
      setBusy(false)
    }
  }
  async function download(id: string) {
    try {
      await mediaApi.save({
        id,
        name: `submission-document-${id}`,
        mimeType: "application/octet-stream",
        size: 0,
        createdAt: "",
      })
    } catch (e) {
      setError(e instanceof Error ? e.message : "Download failed")
    }
  }
  return (
    <AppShell>
      <main className="sporg">
        <header className="sporg-heading">
          <p className="sporg-eyebrow">Organization governance</p>
          <h1>Application review</h1>
          <p>
            Decide on a saved submission. The applicant's later drafts never
            replace this evidence.
          </p>
        </header>
        {error && (
          <div className="sporg-alert" role="alert">
            {error}
            <Button
              variant="outline"
              onClick={() => {
                setSelected(null)
                setReload((x) => x + 1)
              }}
            >
              Refresh applications
            </Button>
          </div>
        )}
        <div className="sporg-grid">
          <section className="sporg-card">
            <h2>Awaiting review</h2>
            {loading ? (
              <p role="status">Loading applications…</p>
            ) : !organizations.length ? (
              <p>No pending applications.</p>
            ) : (
              organizations.map((o) => (
                <div className="sporg-row" key={o.id}>
                  <div>
                    <strong>{o.displayName}</strong>
                    <small>
                      {o.legalName} · {o.country}
                    </small>
                  </div>
                  <Button
                    variant="outline"
                    disabled={busy}
                    onClick={() => setSelected(o)}
                  >
                    Review
                  </Button>
                </div>
              ))
            )}
          </section>
          {selected && (
            <section className="sporg-card">
              <h2>{selected.displayName}</h2>
              {!revision ? (
                <p role="status">Loading submission snapshot…</p>
              ) : (
                <>
                  <p>
                    Revision {revision.revisionNo} · {revision.status} ·{" "}
                    {new Date(revision.submittedAt).toLocaleString()}
                  </p>
                  <dl>
                    <dt>Legal name</dt>
                    <dd>{revision.legalName}</dd>
                    <dt>Industry / country</dt>
                    <dd>
                      {revision.industry} / {revision.country}
                    </dd>
                    <dt>Registration number</dt>
                    <dd>{revision.registrationNumber || "Not provided"}</dd>
                    <dt>Website</dt>
                    <dd>{revision.website || "Not provided"}</dd>
                    <dt>Contact</dt>
                    <dd>
                      {revision.contactName} · {revision.contactEmail} ·{" "}
                      {revision.contactPhone || "—"}
                    </dd>
                  </dl>
                  <h3>Submitted evidence</h3>
                  {revision.documentMediaIds.map((id, i) => (
                    <Button
                      variant="outline"
                      key={id}
                      onClick={() => void download(id)}
                    >
                      Document {i + 1}
                    </Button>
                  ))}
                  {!revision.documentMediaIds.length && (
                    <p>No documents submitted.</p>
                  )}
                  <label>
                    Review reason
                    <textarea
                      value={reason}
                      maxLength={1000}
                      onChange={(e) => setReason(e.target.value)}
                      disabled={busy}
                    />
                  </label>
                  <div className="sporg-actions">
                    <Button
                      disabled={busy || revision.status !== "PENDING"}
                      onClick={() => void review("APPROVED")}
                    >
                      Approve
                    </Button>
                    <Button
                      variant="destructive"
                      disabled={
                        busy || !reason.trim() || revision.status !== "PENDING"
                      }
                      onClick={() => void review("REJECTED")}
                    >
                      Reject
                    </Button>
                    <Button
                      variant="ghost"
                      disabled={busy}
                      onClick={() => setSelected(null)}
                    >
                      Close
                    </Button>
                  </div>
                </>
              )}
            </section>
          )}
        </div>
        {selected && (
          <ApplicationHistory
            key={selected.id}
            organizationId={selected.id}
            admin
          />
        )}
      </main>
    </AppShell>
  )
}
