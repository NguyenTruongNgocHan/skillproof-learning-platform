import { ApiError } from "@/shared/api/apiClient"
import AdminPageHeader from "../components/AdminPageHeader"
import AdminStatusBadge from "../components/AdminStatusBadge"
import { useEffect, useState } from "react"
import AppShell from "@/shared/components/layout/AppShell"
import { adminOrganizationApi } from "../api/adminOrganizationApi"
import type {
  ApplicationRevision,
  Organization,
} from "@/features/organization/types/organization.types"
import EvidencePreview from "../components/EvidencePreview"
import { governanceApi } from "@/features/organization/api/governanceApi"
import type { EvidenceDocument } from "@/features/organization/types/governance.types"
import ReviewChecklist from "../components/ReviewChecklist"
import AdminTable, { type Column } from "../components/AdminTable"
import AdminDialog from "../components/AdminDialog"
import "../styles/admin-console.css"
const columns: Column<Organization>[] = [
  {
    key: "name",
    label: "Organization",
    render: (o) => (
      <>
        <span className="adm-primary-cell">{o.displayName}</span>
        <span className="adm-secondary-cell">{o.legalName}</span>
      </>
    ),
    sortValue: (o) => o.displayName,
  },
  {
    key: "country",
    label: "Jurisdiction",
    render: (o) => o.country || "—",
    sortValue: (o) => o.country || "",
  },
  {
    key: "industry",
    label: "Industry",
    render: (o) => o.industry || "—",
    sortValue: (o) => o.industry || "",
  },
  { key: "status", label: "Status", render: (o) => <AdminStatusBadge value={o.status} /> },
]
export default function AdminOrganizationReviewPage() {
  const [rows, setRows] = useState<Organization[]>([])
  const [selected, setSelected] = useState<Organization | null>(null)
  const [revision, setRevision] = useState<ApplicationRevision | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [reason, setReason] = useState("")
  const [feedback, setFeedback] = useState("")
  const [complete, setComplete] = useState(false)
  const [busy, setBusy] = useState(false)
  const [refresh, setRefresh] = useState(0)
  const [evidenceRows, setEvidenceRows] = useState<EvidenceDocument[]>([])
  const [evidenceLoading, setEvidenceLoading] = useState(false)
  const [evidenceReason, setEvidenceReason] = useState<Record<string, string>>({})
  const [evidenceBusy, setEvidenceBusy] = useState<string | null>(null)
  useEffect(() => {
    let active = true
    setLoading(true)
    adminOrganizationApi
      .pending()
      .then((r) => {
        if (active) setRows(r)
      })
      .catch((e) => {
        if (active) setError(e instanceof Error ? e.message : "Cannot load queue")
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [refresh])
  useEffect(() => {
    let active = true
    setRevision(null)
    setEvidenceRows([])
    setEvidenceReason({})
    setError("")
    setReason("")
    setFeedback("")
    setComplete(false)
    if (selected) {
      setEvidenceLoading(true)
      adminOrganizationApi
        .revisions(selected.id)
        .then(async (revisions) => {
          const latest = [...revisions].sort((a, b) => b.revisionNo - a.revisionNo)[0] ?? null
          const documents = latest
            ? await governanceApi.verificationSnapshot(latest.id, selected.id)
            : []
          if (active) {
            setRevision(latest)
            setEvidenceRows(documents)
          }
        })
        .catch((e) => {
          if (active) setError(e instanceof Error ? e.message : "Cannot load submission evidence")
        })
        .finally(() => {
          if (active) setEvidenceLoading(false)
        })
    }
    return () => {
      active = false
    }
  }, [selected])
  const submittedEvidence = revision
    ? evidenceRows.filter(
        (d) => d.purpose === "VERIFICATION" && revision.documentMediaIds.includes(d.mediaId),
      )
    : []
  const requiredKinds = ["LEGAL_IDENTITY", "REPRESENTATIVE"]
  const acceptedImages = new Set(
    submittedEvidence
      .filter(
        (d) =>
          d.kind === "COMPANY_IMAGE" &&
          d.status === "ACCEPTED" &&
          (!d.expiresAt || new Date(d.expiresAt) > new Date()),
      )
      .map((d) => d.sha256)
      .filter((hash): hash is string => Boolean(hash)),
  ).size
  const missingKinds = requiredKinds.filter(
    (kind) =>
      !submittedEvidence.some(
        (d) =>
          d.kind === kind &&
          d.status === "ACCEPTED" &&
          (!d.expiresAt || new Date(d.expiresAt) > new Date()),
      ),
  )
  const evidenceReady =
    Boolean(revision) && !evidenceLoading && acceptedImages >= 3 && missingKinds.length === 0
  async function reviewEvidence(
    document: EvidenceDocument,
    status: "ACCEPTED" | "NEEDS_INFORMATION" | "REJECTED",
  ) {
    if (!selected || !revision || revision.status !== "PENDING" || evidenceBusy || busy) return
    const note = (evidenceReason[document.id] ?? "").trim()
    if (status !== "ACCEPTED" && !note) {
      setError("Provide a reason when requesting information or rejecting evidence.")
      return
    }
    setEvidenceBusy(document.id)
    try {
      await governanceApi.reviewEvidence(selected.id, document.id, {
        status,
        reason: note || "Verified against the submitted application",
      })
      const updated = await governanceApi.verificationSnapshot(revision!.id, selected.id)
      setEvidenceRows(updated)
      setError("")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Evidence review failed")
      if (e instanceof ApiError && [403, 409].includes(e.status)) {
        setSelected(null)
        setRefresh((x) => x + 1)
      }
    } finally {
      setEvidenceBusy(null)
    }
  }
  async function decide(decision: "APPROVED" | "REJECTED" | "NEEDS_INFORMATION") {
    if (
      !revision ||
      revision.status !== "PENDING" ||
      Boolean(evidenceBusy) ||
      busy ||
      !reason.trim() ||
      (decision === "APPROVED" && (!complete || !evidenceReady))
    )
      return
    const explanation =
      decision === "NEEDS_INFORMATION"
        ? [feedback, reason.trim()].filter(Boolean).join("\n")
        : reason.trim()
    if (explanation.length > 1000) {
      setError("Feedback exceeds 1000 characters. Shorten the notes before submitting.")
      return
    }
    if (
      !window.confirm(
        `Confirm ${decision.toLowerCase().replaceAll("_", " ")} for revision ${revision.revisionNo}?`,
      )
    )
      return
    setBusy(true)
    try {
      await adminOrganizationApi.review(revision.id, decision, explanation)
      setSelected(null)
      setRefresh((x) => x + 1)
      setError("")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Decision failed")
      setSelected(null)
      setRefresh((x) => x + 1)
    } finally {
      setBusy(false)
    }
  }
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Organization governance"
          title="Application review"
          description="Inspect the submitted revision, review evidence and record a decision."
          actions={
            <button className="adm-btn" onClick={() => setRefresh((x) => x + 1)}>
              Refresh queue
            </button>
          }
        />
        {error && (
          <div role="alert" className="adm-error">
            {error}
          </div>
        )}
        {loading ? (
          <p className="adm-muted">Loading applications…</p>
        ) : (
          <AdminTable
            rows={rows}
            columns={columns}
            searchText={(o) => `${o.displayName} ${o.legalName} ${o.country} ${o.industry}`}
            filters={[
              { key: "status", label: "Status", value: (o) => o.status },
              { key: "country", label: "Jurisdiction", value: (o) => o.country || "Unknown" },
            ]}
            onOpen={setSelected}
            empty="No applications awaiting review."
          />
        )}
        {selected && (
          <AdminDialog
            title={selected.displayName}
            subtitle={`${selected.legalName} · ${selected.country} · Application review`}
            onClose={() => {
              if (!busy && !evidenceBusy) setSelected(null)
            }}
            footer={
              <>
                <button
                  className="adm-btn"
                  disabled={busy || Boolean(evidenceBusy)}
                  onClick={() => setSelected(null)}
                >
                  Close
                </button>
                <button
                  className="adm-btn"
                  disabled={
                    busy ||
                    Boolean(evidenceBusy) ||
                    revision?.status !== "PENDING" ||
                    !reason.trim()
                  }
                  onClick={() => void decide("NEEDS_INFORMATION")}
                >
                  Request information
                </button>
                <button
                  className="adm-btn"
                  disabled={
                    busy ||
                    Boolean(evidenceBusy) ||
                    revision?.status !== "PENDING" ||
                    !reason.trim()
                  }
                  onClick={() => void decide("REJECTED")}
                >
                  Reject
                </button>
                <button
                  className="adm-btn adm-btn-primary"
                  disabled={
                    busy ||
                    Boolean(evidenceBusy) ||
                    revision?.status !== "PENDING" ||
                    !reason.trim() ||
                    !complete ||
                    !evidenceReady
                  }
                  onClick={() => void decide("APPROVED")}
                >
                  Approve
                </button>
              </>
            }
          >
            {!revision ? (
              <p className="adm-muted">Loading submitted revision…</p>
            ) : (
              <>
                <div className="adm-panel-head">
                  <h3>Submission snapshot</h3>
                  <span className="adm-pill">
                    Revision {revision.revisionNo} ·{" "}
                    {new Date(revision.submittedAt).toLocaleString()}
                  </span>
                </div>
                <dl className="adm-detail-list">
                  <dt>Legal name</dt>
                  <dd>{revision.legalName}</dd>
                  <dt>Registration</dt>
                  <dd>{revision.registrationNumber || "Not provided"}</dd>
                  <dt>Contact</dt>
                  <dd>
                    {revision.contactName} · {revision.contactEmail}
                  </dd>
                  <dt>Website</dt>
                  <dd>{revision.website || "Not provided"}</dd>
                </dl>
                <div className="adm-dialog-grid" style={{ marginTop: 24 }}>
                  <section>
                    <h3>Submitted evidence</h3>
                    <p className="adm-muted">Preview or download documents from this revision.</p>
                    {evidenceLoading && <p className="adm-muted">Loading classified evidence…</p>}
                    <div className="adm-evidence-progress" role="status">
                      Company images accepted: <strong>{acceptedImages}/3</strong> · Legal identity:{" "}
                      {missingKinds.includes("LEGAL_IDENTITY") ? "Pending" : "Accepted"} ·
                      Representative:{" "}
                      {missingKinds.includes("REPRESENTATIVE") ? "Pending" : "Accepted"}
                    </div>
                    {revision.documentMediaIds.map((id, i) => {
                      const docs = submittedEvidence.filter((d) => d.mediaId === id)
                      return (
                        <div key={id} className="adm-evidence-review-item">
                          <EvidencePreview id={id} label={docs[0]?.title || `Document ${i + 1}`} />
                          {docs.length === 0 ? (
                            <p className="adm-error">
                              This submitted file has no active verification classification. Check
                              the original submission.
                            </p>
                          ) : (
                            docs.map((doc) => (
                              <div key={doc.id} className="adm-evidence-review-controls">
                                <div className="adm-evidence-review-meta">
                                  <strong>{doc.kind.replaceAll("_", " ")}</strong>
                                  <AdminStatusBadge value={doc.status} />
                                  {doc.expiresAt && (
                                    <span className="adm-muted">
                                      Expires {new Date(doc.expiresAt).toLocaleDateString()}
                                    </span>
                                  )}
                                </div>
                                {doc.reason && (
                                  <p className="adm-muted">Previous review: {doc.reason}</p>
                                )}
                                <label className="adm-evidence-note-label">
                                  Evidence review note
                                  <textarea
                                    className="adm-review-note"
                                    rows={2}
                                    maxLength={1000}
                                    value={evidenceReason[doc.id] ?? ""}
                                    onChange={(e) =>
                                      setEvidenceReason((prev) => ({
                                        ...prev,
                                        [doc.id]: e.target.value,
                                      }))
                                    }
                                    placeholder="Reason for this evidence decision"
                                  />
                                </label>
                                <div className="adm-evidence-review-actions">
                                  <button
                                    className="adm-btn"
                                    disabled={
                                      Boolean(evidenceBusy) || busy || revision.status !== "PENDING"
                                    }
                                    onClick={() => void reviewEvidence(doc, "ACCEPTED")}
                                  >
                                    Accept evidence
                                  </button>
                                  <button
                                    className="adm-btn"
                                    disabled={
                                      Boolean(evidenceBusy) || busy || revision.status !== "PENDING"
                                    }
                                    onClick={() => void reviewEvidence(doc, "NEEDS_INFORMATION")}
                                  >
                                    Needs information
                                  </button>
                                  <button
                                    className="adm-btn"
                                    disabled={
                                      Boolean(evidenceBusy) || busy || revision.status !== "PENDING"
                                    }
                                    onClick={() => void reviewEvidence(doc, "REJECTED")}
                                  >
                                    Reject evidence
                                  </button>
                                </div>
                              </div>
                            ))
                          )}
                        </div>
                      )
                    })}
                    {!revision.documentMediaIds.length && (
                      <p className="adm-muted">No attached documents.</p>
                    )}
                  </section>
                  <section>
                    <ReviewChecklist
                      key={revision.id}
                      type="ORGANIZATION"
                      id={revision.id}
                      onComplete={setComplete}
                      onFeedback={setFeedback}
                    />
                  </section>
                </div>
                <section style={{ marginTop: 20 }}>
                  <h3>Decision summary</h3>
                  <p className="adm-muted">
                    For information requests, saved criterion comments are included in the
                    applicant-facing reason. Save your review progress before deciding. Approval
                    additionally requires three accepted company images, accepted legal identity and
                    accepted representative authorization.
                  </p>
                  <textarea
                    className="adm-review-note"
                    aria-label="Decision reason"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    maxLength={1000}
                    placeholder="Explain your final decision…"
                  />
                </section>
              </>
            )}
          </AdminDialog>
        )}
      </section>
    </AppShell>
  )
}
