import { ApiError } from "@/services/api/apiClient"
import { useEffect, useState, type FormEvent } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
import {
  organizationApi,
  type OrganizationApplication,
} from "@/features/organization/organizationApi"
import BrandLogo from "@/components/ui/BrandLogo"
import ThemeSwitcher from "@/components/ui/ThemeSwitcher"
import Input from "@/components/ui/Input"
import Button from "@/components/ui/Button"
import MediaPanel from "@/features/media/MediaPanel"
import { ApplicationHistory } from "@/features/organizer/ApplicationHistory"
import { useToast } from "@/components/ui/Toast"
import "@/styles/organizer.css"
const blank: OrganizationApplication = {
  legalName: "",
  displayName: "",
  industry: "",
  country: "",
  contactName: "",
  contactEmail: "",
  website: "",
  registrationNumber: "",
  contactPhone: "",
}
export default function OrganizerOnboardingPage() {
  const {
    owned,
    loading,
    error: contextError,
    refresh,
    setDirty,
  } = useOrganizationContext()
  const { user } = useAuth()
  const navigate = useNavigate()
  const { toast } = useToast()
  const [form, setForm] = useState(blank)
  const [dirty, markDirty] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState("")
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [review, setReview] = useState(false)
  useEffect(() => {
    if (owned) {
      setForm({
        legalName: owned.legalName,
        displayName: owned.displayName,
        website: owned.website ?? "",
        industry: owned.industry,
        country: owned.country,
        registrationNumber: owned.registrationNumber ?? "",
        contactName: owned.contactName,
        contactEmail: owned.contactEmail,
        contactPhone: owned.contactPhone ?? "",
      })
      markDirty(false)
      setReview(false)
    } else
      setForm((previous) => ({ ...previous, contactEmail: user?.email ?? "" }))
  }, [owned?.id, owned?.updatedAt, user?.id])
  useEffect(() => {
    setDirty("application", dirty)
    return () => setDirty("application", false)
  }, [dirty, setDirty])
  useEffect(() => {
    if (owned?.status === "PENDING")
      navigate("/organizer/verification-pending", { replace: true })
    if (owned?.status === "APPROVED") navigate("/organizer", { replace: true })
  }, [owned?.status, navigate])
  async function save(e: FormEvent) {
    e.preventDefault()
    if (busy) return
    setBusy(true)
    setError("")
    setFieldErrors({})
    try {
      if (!owned) await organizationApi.create(form)
      else if (owned.status === "REJECTED") await organizationApi.resubmit(form)
      else await organizationApi.saveDraft(form)
      markDirty(false)
      setDirty("application", false)
      await refresh()
      toast(
        "success",
        "Draft saved. Add documents and review before submitting.",
      )
    } catch (e) {
      setError(e instanceof Error ? e.message : "Save failed")
      if (e instanceof ApiError) setFieldErrors(e.problem.fieldErrors ?? {})
    } finally {
      setBusy(false)
    }
  }
  async function submit() {
    if (!owned || dirty || busy) return
    setBusy(true)
    setError("")
    try {
      await organizationApi.submit()
      await refresh()
      toast("success", "Application submitted for review.")
      navigate("/organizer/verification-pending", { replace: true })
    } catch (e) {
      setError(e instanceof Error ? e.message : "Submit failed")
    } finally {
      setBusy(false)
    }
  }
  const field = (
    key: keyof OrganizationApplication,
    label: string,
    required = false,
    type = "text",
  ) => (
    <Input
      label={label}
      required={required}
      type={type}
      maxLength={
        {
          legalName: 200,
          displayName: 200,
          website: 500,
          industry: 120,
          country: 120,
          registrationNumber: 120,
          contactName: 150,
          contactEmail: 320,
          contactPhone: 60,
        }[key]
      }
      error={fieldErrors[key]}
      disabled={busy}
      value={form[key] ?? ""}
      onChange={(e) => {
        setForm((p) => ({ ...p, [key]: e.target.value }))
        markDirty(true)
        setReview(false)
      }}
    />
  )
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
            Save your details, add supporting documents, then review and submit.
            Submitted records remain unchanged.
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
                <p className="sporg-alert">
                  Previous review: {owned.reviewReason}
                </p>
              )}
              {error && (
                <p role="alert" className="sporg-alert">
                  {error}
                </p>
              )}
              <form className="sporg-form" onSubmit={(e) => void save(e)}>
                <div className="sporg-fields">
                  {field("legalName", "Legal name", true)}
                  {field("displayName", "Display name", true)}
                  {field("industry", "Industry", true)}
                  {field("country", "Country / jurisdiction", true)}
                  {field("registrationNumber", "Registration number")}
                  {field("website", "Website", false, "url")}
                  {field("contactName", "Contact person", true)}
                  {field("contactEmail", "Contact email", true, "email")}
                  {field("contactPhone", "Contact phone", false, "tel")}
                </div>
                <Button type="submit" disabled={busy}>
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
                />
              ) : (
                <p>Save a draft first to upload supporting documents.</p>
              )}
              <h2>3. Review and submit</h2>
              <p>
                Check your contact details and attachments. You will not be able
                to change this submission while it is under review.
              </p>
              <Button
                variant="outline"
                disabled={!owned || owned.status !== "DRAFT" || dirty || busy}
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
                  <Button disabled={busy} onClick={() => void submit()}>
                    {busy ? "Submitting…" : "Submit for review"}
                  </Button>
                </>
              )}
              {dirty && <p>Save your latest changes before reviewing.</p>}
            </section>
            {owned && (
              <div className="sporg-full">
                <ApplicationHistory organizationId={owned.id} />
              </div>
            )}
          </div>
        )}
        <div className="sporg-actions">
          <Button
            variant="ghost"
            onClick={() => {
              if (!dirty || window.confirm("Leave without saving?"))
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
