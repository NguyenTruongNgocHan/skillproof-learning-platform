import { certificationApi } from "@/features/certification/api/certificationApi"
import type { Certificate } from "@/features/certification/types/certification.types"
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { organizerError } from "@/features/organization/utils/errorMessage"
import Button from "@/shared/ui/Button"
import Input from "@/shared/ui/Input"
import { useToast } from "@/shared/ui/Toast"
import { useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
export default function CertificateDetailPage() {
  const { id } = useParams()
  const { organization } = useOrganizationContext()
  const [certificate, setCertificate] = useState<Certificate | null>(null)
  const [reason, setReason] = useState("")
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const [retry, setRetry] = useState(0)
  const { toast } = useToast()
  useEffect(() => {
    let active = true
    setCertificate(null)
    setError("")
    if (id)
      certificationApi
        .detail(id)
        .then((c) => {
          if (active) {
            if (c.organizationId !== organization?.id)
              throw new Error("Switch to this certificate's organization before opening it.")
            setCertificate(c)
          }
        })
        .catch((e) => {
          if (active) setError(e.message)
        })
    return () => {
      active = false
    }
  }, [id, organization?.id, retry])
  async function revoke() {
    if (
      !certificate ||
      busy ||
      !reason.trim() ||
      !window.confirm("Revoke this certificate? This action is permanent.")
    )
      return
    setBusy(true)
    try {
      setCertificate(await certificationApi.revoke(certificate.id, reason.trim()))
      setReason("")
      toast("success", "Certificate revoked.")
    } catch (e) {
      setError(organizerError(e, "Revoke failed"))
    } finally {
      setBusy(false)
    }
  }
  return (
    <OrganizerSurface title="Certificate detail" authority="ISSUE_CERTIFICATES">
      <Link to="/organizer/certifications">Back to certifications</Link>
      {error && (
        <p role="alert" className="sporg-alert">
          {error}
          <Button onClick={() => setRetry((r) => r + 1)}>Retry</Button>
        </p>
      )}
      {!certificate && !error ? (
        <p role="status">Loading certificate…</p>
      ) : (
        certificate && (
          <section className="sporg-card">
            <h2>{certificate.serialNumber}</h2>
            <dl>
              <dt>Status</dt>
              <dd>{certificate.status}</dd>
              <dt>Program</dt>
              <dd>{certificate.programName ?? "Legacy snapshot unavailable"}</dd>
              <dt>Issuer</dt>
              <dd>{certificate.issuerName ?? "Legacy snapshot unavailable"}</dd>
              <dt>Learner</dt>
              <dd>{certificate.learnerEmail ?? certificate.learnerUserId}</dd>
              <dt>Issued</dt>
              <dd>{new Date(certificate.issuedAt).toLocaleString()}</dd>
              {certificate.revokedAt && (
                <>
                  <dt>Revoked</dt>
                  <dd>
                    {new Date(certificate.revokedAt).toLocaleString()} ·{" "}
                    {certificate.revocationReason}
                  </dd>
                </>
              )}
            </dl>
            <p>Blockchain proof is not yet anchored.</p>
            <Button asChild variant="outline">
              <Link to={`/verify/${encodeURIComponent(certificate.serialNumber)}`}>
                Open public verification
              </Link>
            </Button>
            {certificate.status === "ISSUED" && (
              <form
                className="sporg-form"
                onSubmit={(e) => {
                  e.preventDefault()
                  void revoke()
                }}
              >
                <h3>Revoke certificate</h3>
                <Input
                  label="Reason for revocation"
                  required
                  maxLength={1000}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                />
                <Button variant="destructive" type="submit" disabled={busy || !reason.trim()}>
                  {busy ? "Revoking…" : "Revoke permanently"}
                </Button>
              </form>
            )}
          </section>
        )
      )}
    </OrganizerSurface>
  )
}
