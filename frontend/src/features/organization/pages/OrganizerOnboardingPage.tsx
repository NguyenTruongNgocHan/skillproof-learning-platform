import MediaPanel from "@/features/media/components/MediaPanel"
import BrandLogo from "@/shared/ui/BrandLogo"
import Button from "@/shared/ui/Button"
import ThemeSwitcher from "@/shared/ui/ThemeSwitcher"
import { useNavigate } from "react-router-dom"
import { ApplicationHistory } from "../components/ApplicationHistory"
import { OrganizationApplicationFields } from "../components/OrganizationApplicationFields"
import { useOrganizationApplication } from "../hooks/useOrganizationApplication"
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
  return (
    <div className="sporg-shell">
      <header className="sporg-shell-header">
        <BrandLogo />
        <ThemeSwitcher compact />
      </header>
      <main className="sporg">
        <header className="sporg-heading">
          <p className="sporg-eyebrow">Organization application</p>
          <h1>
            {owned?.status === "REJECTED"
              ? "Prepare your next submission"
              : "Build your organization"}
          </h1>
          <p>
            Save your details, add supporting documents, then review and submit. Submitted records
            remain unchanged.
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
          <div className="sporg-grid">
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
                    : owned?.status === "REJECTED"
                      ? "Open and save new draft"
                      : "Save draft"}
                </Button>
              </form>
            </section>
            <section className="sporg-card">
              <h2>2. Supporting documents</h2>
              {owned?.status === "DRAFT" ? (
                <MediaPanel
                  scope="ORGANIZATION"
                  target={owned.id}
                  writable
                  removable
                  onChanged={() => setReview(false)}
                  onBusyChange={setAttachmentsBusy}
                />
              ) : (
                <p>Save a draft first to upload supporting documents.</p>
              )}
              <h2>3. Review and submit</h2>
              <p>
                Check your contact details and attachments. You will not be able to change this
                submission while it is under review.
              </p>
              <Button
                variant="outline"
                disabled={!owned || owned.status !== "DRAFT" || dirty || busy || attachmentsBusy}
                onClick={() => setReview(true)}
              >
                Review saved application
              </Button>
              {review && owned && (
                <>
                  <dl>
                    <dt>Legal name</dt>
                    <dd>{owned.legalName}</dd>
                    <dt>Display name</dt>
                    <dd>{owned.displayName}</dd>
                    <dt>Industry / country</dt>
                    <dd>
                      {owned.industry} / {owned.country}
                    </dd>
                    <dt>Contact</dt>
                    <dd>
                      {owned.contactName} · {owned.contactEmail}
                    </dd>
                  </dl>
                  <MediaPanel scope="ORGANIZATION" target={owned.id} />
                  <Button
                    disabled={busy || attachmentsBusy || dirty || owned.status !== "DRAFT"}
                    onClick={() => void submit()}
                  >
                    {busy ? "Submitting…" : "Submit for review"}
                  </Button>
                </>
              )}
              {dirty && <p>Save your latest changes before reviewing.</p>}
            </section>
            {owned && (
              <div className="sporg-full">
                <ApplicationHistory organizationId={owned.id} refreshKey={owned.updatedAt} />
              </div>
            )}
          </div>
        )}
        <div className="sporg-actions">
          <Button
            variant="ghost"
            onClick={() => {
              navigate("/organizer")
            }}
          >
            Workspace
          </Button>
        </div>
      </main>
    </div>
  )
}
