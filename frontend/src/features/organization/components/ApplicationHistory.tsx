import { mediaApi } from "@/features/media/api/mediaApi"
import { organizationApi } from "@/features/organization/api/organizationApi"
import type { ApplicationRevision } from "@/features/organization/types/organization.types"
import Button from "@/shared/ui/Button"
import { useEffect, useState } from "react"
export function ApplicationHistory({
  organizationId,
  admin = false,
  documentsVisible = true,
  refreshKey,
}: {
  organizationId: string
  admin?: boolean
  documentsVisible?: boolean
  refreshKey?: string
}) {
  const [rows, setRows] = useState<ApplicationRevision[]>([])
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    setRows([])
    setLoading(true)
    setError("")
    const request = admin
      ? import("@/features/admin/api/adminOrganizationApi").then((m) =>
          m.adminOrganizationApi.revisions(organizationId),
        )
      : organizationApi.applicationRevisions(organizationId)
    request
      .then((r) => {
        if (active) setRows([...r].sort((a, b) => b.revisionNo - a.revisionNo))
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
  }, [organizationId, admin, retry, refreshKey])
  async function download(id: string) {
    try {
      const blob = await mediaApi.download({
        id,
        name: "document",
        mimeType: "application/octet-stream",
        size: 0,
        createdAt: "",
      })
      const url = URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.download = `submission-document-${id}`
      link.click()
      setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Download failed")
    }
  }
  return (
    <section className="sporg-card">
      <h2>Submission history</h2>
      {loading ? (
        <p role="status">Loading history…</p>
      ) : error ? (
        <p role="alert">
          {error}
          <Button variant="outline" onClick={() => setRetry((x) => x + 1)}>
            Retry history
          </Button>
        </p>
      ) : !rows.length ? (
        <p>No application has been submitted yet.</p>
      ) : (
        rows.map((r) => (
          <details key={r.id}>
            <summary>
              Revision {r.revisionNo} · {r.status} · {new Date(r.submittedAt).toLocaleDateString()}
            </summary>
            <dl>
              <dt>Legal name</dt>
              <dd>{r.legalName}</dd>
              <dt>Public name</dt>
              <dd>{r.displayName}</dd>
              <dt>Industry / country</dt>
              <dd>
                {r.industry} / {r.country}
              </dd>
              <dt>Website</dt>
              <dd>{r.website || "—"}</dd>
              <dt>Registration</dt>
              <dd>{r.registrationNumber || "—"}</dd>
              <dt>Contact</dt>
              <dd>
                {r.contactName} · {r.contactEmail} · {r.contactPhone || "—"}
              </dd>
              <dt>Decision</dt>
              <dd>
                {r.reviewReason || "No review reason"}
                {r.reviewedAt && ` · ${new Date(r.reviewedAt).toLocaleString()}`}
              </dd>
            </dl>
            <h3>Submitted documents</h3>
            {r.documentMediaIds?.length && documentsVisible ? (
              r.documentMediaIds.map((id, i) => (
                <Button key={id} variant="outline" onClick={() => void download(id)}>
                  Download document {i + 1}
                </Button>
              ))
            ) : (
              <p>
                {!documentsVisible
                  ? "Document download requires profile management permission."
                  : "No documents in this snapshot."}
              </p>
            )}
          </details>
        ))
      )}
    </section>
  )
}
