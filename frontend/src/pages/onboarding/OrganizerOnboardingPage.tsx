import { useEffect, useState, type FormEvent } from "react"
import { Link, useNavigate } from "react-router-dom"
import { ArrowLeft, Building2, CheckCircle2, ShieldCheck } from "lucide-react"
import BrandLogo from "@/components/ui/BrandLogo"
import ThemeSwitcher from "@/components/ui/ThemeSwitcher"
import Button from "@/components/ui/Button"
import Input from "@/components/ui/Input"
import { ApiError } from "@/services/api/apiClient"
import {
  organizationApi,
  type Organization,
  type OrganizationApplication,
} from "@/features/organization/organizationApi"

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
  const navigate = useNavigate()
  const [existing, setExisting] = useState<Organization | null>(null)
  const [form, setForm] = useState<OrganizationApplication>(blank)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    let active = true
    organizationApi
      .mine()
      .then((org) => {
        if (!active) return
        setExisting(org)
        if (org.status === "APPROVED") navigate("/organizer", { replace: true })
        else if (org.status === "PENDING")
          navigate("/organizer/verification-pending", { replace: true })
        else
          setForm({
            legalName: org.legalName,
            displayName: org.displayName,
            website: org.website ?? "",
            industry: org.industry,
            country: org.country,
            registrationNumber: org.registrationNumber ?? "",
            contactName: org.contactName,
            contactEmail: org.contactEmail,
            contactPhone: org.contactPhone ?? "",
          })
      })
      .catch((e) => {
        if (active && !(e instanceof ApiError && e.status === 404))
          setError(
            e instanceof Error ? e.message : "Could not load your application.",
          )
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [navigate])

  function field(
    key: keyof OrganizationApplication,
    label: string,
    opts: { required?: boolean; type?: string; hint?: string } = {},
  ) {
    return (
      <Input
        label={label}
        type={opts.type ?? "text"}
        required={opts.required}
        hint={opts.hint}
        value={form[key] ?? ""}
        onChange={(e) =>
          setForm((previous) => ({ ...previous, [key]: e.target.value }))
        }
      />
    )
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (busy) return
    setBusy(true)
    setError("")
    try {
      const normalized = Object.fromEntries(
        Object.entries(form).map(([key, value]) => [key, value?.trim()]),
      ) as unknown as OrganizationApplication
      if (existing?.status === "REJECTED")
        await organizationApi.resubmit(normalized)
      else await organizationApi.create(normalized)
      navigate("/organizer/verification-pending", { replace: true })
    } catch (reason) {
      setError(
        reason instanceof Error
          ? reason.message
          : "The application could not be submitted. Please try again.",
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="org-application-page">
      <header className="org-application-header">
        <BrandLogo />
        <ThemeSwitcher compact />
      </header>
      <main className="org-application-main">
        <Link to="/" className="auth-back">
          <ArrowLeft size={16} /> Back to home
        </Link>
        <div className="org-application-heading">
          <span className="auth-intro__eyebrow">
            <Building2 size={16} /> ORGANIZATION APPLICATION
          </span>
          <h1>
            {existing?.status === "REJECTED"
              ? "Update your application."
              : "Build trust from day one."}
          </h1>
          <p>
            Tell us about the organization you represent. An administrator
            reviews your application before organization permissions become
            available.
          </p>
        </div>
        {loading ? (
          <p role="status">Loading your application…</p>
        ) : (
          <>
            {existing?.status === "REJECTED" && (
              <div className="org-application-notice" role="status">
                <strong>Previous review:</strong>{" "}
                {existing.reviewReason ||
                  "Please update the application and submit it again."}
              </div>
            )}
            {error && (
              <div className="org-error" role="alert">
                {error}{" "}
                <button type="button" onClick={() => window.location.reload()}>
                  Retry loading
                </button>
              </div>
            )}
            {(!error || existing?.status === "REJECTED") && (
              <form onSubmit={submit} className="org-application-form">
                <section>
                  <h2>
                    <CheckCircle2 size={20} /> Organization details
                  </h2>
                  <div className="org-application-grid">
                    {field("legalName", "Registered legal name", {
                      required: true,
                    })}
                    {field("displayName", "Public display name", {
                      required: true,
                    })}
                    {field("industry", "Industry", { required: true })}
                    {field("country", "Country or jurisdiction", {
                      required: true,
                    })}
                    {field(
                      "registrationNumber",
                      "Registration number (optional)",
                    )}
                    {field("website", "Website (optional)", { type: "url" })}
                  </div>
                </section>
                <section>
                  <h2>
                    <ShieldCheck size={20} /> Review contact
                  </h2>
                  <p>
                    These details are shared with platform administrators for
                    application review.
                  </p>
                  <div className="org-application-grid">
                    {field("contactName", "Contact person", { required: true })}
                    {field("contactEmail", "Contact email", {
                      required: true,
                      type: "email",
                    })}
                    {field("contactPhone", "Contact phone (optional)", {
                      type: "tel",
                    })}
                  </div>
                </section>
                <p className="org-application-footnote">
                  Submit only information you are authorized to share. After
                  submitting, you may attach supporting documents on the
                  application status page. They remain private to your
                  organization and platform administrators.
                </p>
                <Button type="submit" size="lg" disabled={busy}>
                  {busy
                    ? "Submitting…"
                    : existing?.status === "REJECTED"
                      ? "Resubmit for review"
                      : "Submit for review"}
                </Button>
              </form>
            )}
          </>
        )}
      </main>
    </div>
  )
}
