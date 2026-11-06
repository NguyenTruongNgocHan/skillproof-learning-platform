import { useCallback, useEffect, useMemo, useState } from "react"
import { EvidenceManager } from "../components/EvidenceManager"
import { OrganizationReviewDialog } from "../components/OrganizationReviewDialog"
import BrandLogo from "@/shared/ui/BrandLogo"
import Button from "@/shared/ui/Button"
import ThemeSwitcher from "@/shared/ui/ThemeSwitcher"
import { useNavigate } from "react-router-dom"
import { ApplicationHistory } from "../components/ApplicationHistory"
import { OrganizationApplicationFields } from "../components/OrganizationApplicationFields"
import { useOrganizationApplication } from "../hooks/useOrganizationApplication"
import { governanceApi } from "../api/governanceApi"
import { mediaApi } from "@/features/media/api/mediaApi"
import type { EvidenceDocument } from "../types/governance.types"
import type { MediaItem } from "@/features/media/types/media.types"
import { validateApplication } from "../validation/organizationApplication"
import "../styles/organizer.css"

export default function OrganizerOnboardingPage() {
  const navigate = useNavigate()
  const {
    owned,
    form,
    dirty,
    busy,
    attachmentsBusy,
    setAttachmentsBusy,
    error,
    submitError,
    fieldErrors,
    review,
    setReview,
    loading,
    contextError,
    refresh,
    changeField,
    save,
    submit,
  } = useOrganizationApplication()
  const [evidence, setEvidence] = useState<EvidenceDocument[]>([])
  const [files, setFiles] = useState<MediaItem[]>([])
  const [syncing, setSyncing] = useState(false)
  const [syncError, setSyncError] = useState("")
  const [revision, setRevision] = useState(0)
  const reloadEvidence = useCallback(async () => {
    if (!owned?.id) {
      setEvidence([])
      setFiles([])
      return false
    }
    setSyncing(true)
    setSyncError("")
    try {
      const [docs, uploaded] = await Promise.all([
        governanceApi.evidence(owned.id),
        mediaApi.list("ORGANIZATION", owned.id),
      ])
      setEvidence(docs.filter((doc) => doc.purpose === "VERIFICATION"))
      setFiles(uploaded)
      return true
    } catch (cause) {
      setSyncError(cause instanceof Error ? cause.message : "Unable to refresh evidence.")
      return false
    } finally {
      setSyncing(false)
    }
  }, [owned?.id])
  useEffect(() => {
    void reloadEvidence()
  }, [reloadEvidence, revision])
  const activeIds = useMemo(() => new Set(files.map((file) => file.id)), [files])
  const activeEvidence = useMemo(
    () =>
      evidence.filter(
        (doc) =>
          activeIds.has(doc.mediaId) &&
          doc.status !== "REJECTED" &&
          (!doc.expiresAt || new Date(doc.expiresAt).getTime() > Date.now()),
      ),
    [evidence, activeIds],
  )
  const imageIds = new Set(
    activeEvidence
      .filter(
        (doc) =>
          doc.kind === "COMPANY_IMAGE" &&
          files.some(
            (file) =>
              file.id === doc.mediaId &&
              ["image/jpeg", "image/png", "image/webp"].includes(file.mimeType),
          ),
      )
      .map((doc) => files.find((file) => file.id === doc.mediaId)?.sha256)
      .filter((hash): hash is string => Boolean(hash)),
  )
  const detailsReady =
    Object.keys(validateApplication(form)).length === 0 && Boolean(owned?.id) && !dirty
  const checks = [
    {
      label: "Organization details saved",
      ok: detailsReady,
      hint: "Save the required organization fields.",
    },
    {
      label: "Company images",
      ok: imageIds.size >= 3,
      hint: `${imageIds.size}/3 distinct classified image contents`,
    },
    {
      label: "Legal identity",
      ok: activeEvidence.some((doc) => doc.kind === "LEGAL_IDENTITY"),
      hint: "Classify a legal identity document.",
    },
    {
      label: "Representative authorization",
      ok: activeEvidence.some((doc) => doc.kind === "REPRESENTATIVE"),
      hint: "Classify a representative document.",
    },
  ]
  const completed = checks.filter((check) => check.ok).length
  const percentage = Math.round((completed / checks.length) * 100)
  const canReview =
    percentage === 100 &&
    owned?.status === "DRAFT" &&
    !busy &&
    !attachmentsBusy &&
    !syncing &&
    !syncError
  const openReview = async () => {
    if (!canReview) return
    const ok = await reloadEvidence()
    if (ok) setReview(true)
  }
  // The backend remains authoritative. The refreshed evidence is rechecked before allowing submission.
  const confirmed = checks.every((check) => check.ok) && !syncError
  return (
    <div className="sporg-shell">
      <header className="sporg-shell-header">
        <BrandLogo />
        <ThemeSwitcher compact />
      </header>
      <main className="sporg sporg-onboarding">
        <header className="sporg-heading">
          <p className="sporg-eyebrow">Organization application</p>
          <h1>
            {["REJECTED", "NEEDS_INFORMATION"].includes(owned?.status ?? "")
              ? "Prepare your next submission"
              : "Build your organization"}
          </h1>
          <p>
            Save a draft anytime. Complete the requirements to review and submit for verification.
          </p>
        </header>
        {loading ? (
          <p role="status">Loading application…</p>
        ) : contextError ? (
          <div className="sporg-alert" role="alert">
            {contextError}
            <Button onClick={() => void refresh()}>Retry</Button>
          </div>
        ) : (
          <>
            <section className="sporg-card sporg-progress" aria-label="Application completeness">
              <div className="sporg-progress-heading">
                <div>
                  <strong>Application completeness</strong>
                  <p>
                    This checklist is preliminary. The server validates evidence contents and expiry
                    at submission.
                  </p>
                </div>
                <strong>{percentage}%</strong>
              </div>
              <div
                className="sporg-progress-track"
                role="progressbar"
                aria-valuenow={percentage}
                aria-valuemin={0}
                aria-valuemax={100}
                aria-label="Application completeness"
              >
                <div style={{ width: `${percentage}%` }} />
              </div>
              <div className="sporg-checklist">
                {checks.map((check) => (
                  <div key={check.label} className="sporg-check-item">
                    <span aria-hidden="true">{check.ok ? "✓" : "○"}</span>
                    <div>
                      <strong>{check.label}</strong>
                      <small>{check.ok ? "Complete" : check.hint}</small>
                    </div>
                  </div>
                ))}
              </div>
              {syncError && (
                <p role="alert" className="sporg-alert">
                  {syncError}{" "}
                  <Button variant="outline" onClick={() => void reloadEvidence()}>
                    Retry
                  </Button>
                </p>
              )}
            </section>
            <div className="sporg-grid sporg-onboarding-grid">
              <section className="sporg-card">
                <h2>1. Organization details</h2>
                {owned?.reviewReason && (
                  <p className="sporg-alert">Previous review: {owned.reviewReason}</p>
                )}
                {error && (
                  <div role="alert" className="sporg-alert">
                    {error}
                    <Button
                      variant="outline"
                      disabled={busy || attachmentsBusy}
                      onClick={() => void refresh()}
                    >
                      Reload application
                    </Button>
                  </div>
                )}
                <form className="sporg-form" onSubmit={(e) => void save(e)}>
                  <OrganizationApplicationFields
                    form={form}
                    fieldErrors={fieldErrors}
                    disabled={busy || attachmentsBusy}
                    onChange={changeField}
                  />
                  <Button type="submit" disabled={busy || attachmentsBusy}>
                    {busy
                      ? "Saving…"
                      : ["REJECTED", "NEEDS_INFORMATION"].includes(owned?.status ?? "")
                        ? "Open and save new draft"
                        : "Save draft"}
                  </Button>
                </form>
              </section>
              <section className="sporg-card" id="organization-supporting-documents">
                <h2>2. Supporting documents</h2>
                {owned?.status === "DRAFT" ? (
                  <EvidenceManager
                    organizationId={owned.id}
                    editable
                    onBusyChange={setAttachmentsBusy}
                    onChanged={() => setRevision((value) => value + 1)}
                  />
                ) : (
                  <p>Save a draft first to upload supporting documents.</p>
                )}
              </section>
            </div>
            <section className="sporg-card sporg-onboarding-actions">
              <div>
                <h2>Ready for verification?</h2>
                <p>
                  {percentage === 100
                    ? "All required items are present. Review your saved application before submitting."
                    : `${checks.length - completed} requirement(s) remaining. You can save a draft and return later.`}
                </p>
              </div>
              <Button disabled={!canReview} onClick={() => void openReview()}>
                Review &amp; submit
              </Button>
            </section>
            {owned && (
              <div className="sporg-onboarding-history">
                <ApplicationHistory organizationId={owned.id} refreshKey={owned.updatedAt} />
              </div>
            )}
          </>
        )}
        <div className="sporg-actions">
          <Button variant="ghost" onClick={() => navigate("/organizer")}>
            Workspace
          </Button>
        </div>
      </main>
      {review && owned && confirmed && (
        <OrganizationReviewDialog
          organization={owned}
          evidence={activeEvidence}
          files={files}
          busy={busy || attachmentsBusy || syncing}
          submitting={busy}
          error={submitError}
          onClose={() => {
            if (!busy) setReview(false)
          }}
          onSubmit={() => void submit()}
        />
      )}
    </div>
  )
}
