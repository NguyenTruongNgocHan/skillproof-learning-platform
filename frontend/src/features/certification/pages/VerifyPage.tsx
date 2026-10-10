import { certificationApi } from "@/features/certification/api/certificationApi"
import type { PublicCertificate } from "@/features/certification/types/certification.types"
import "@/features/organization/styles/organizer.css"
import PublicFooter from "@/shared/components/layout/PublicFooter"
import PublicHeader from "@/shared/components/layout/PublicHeader"
import Button from "@/shared/ui/Button"
import Input from "@/shared/ui/Input"
import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
export default function VerifyPage() {
  const { certificateId } = useParams()
  const navigate = useNavigate()
  const [serial, setSerial] = useState(certificateId ?? ""),
    [certificate, setCertificate] = useState<PublicCertificate | null>(null),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(false),
    [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    setCertificate(null)
    setError("")
    setSerial(certificateId ?? "")
    setLoading(Boolean(certificateId))
    if (certificateId)
      certificationApi
        .verify(certificateId)
        .then((value) => {
          if (active) setCertificate(value)
        })
        .catch((e) => {
          if (active) setError(e instanceof Error ? e.message : "Certificate not found")
        })
        .finally(() => {
          if (active) setLoading(false)
        })
    return () => {
      active = false
    }
  }, [certificateId, retry])
  return (
    <div className="min-h-full flex flex-col">
      <PublicHeader />
      <main className="flex-1">
        <div className="sporg">
          <header className="sporg-heading">
            <p className="sporg-eyebrow">Public certificate verification</p>
            <h1>Check a credential.</h1>
            <p>
              Check the issuer's recorded certificate status without signing in. Personal learner
              information is kept private.
            </p>
          </header>
          <section className="sporg-card">
            <form
              className="sporg-form"
              onSubmit={(e) => {
                e.preventDefault()
                if (serial.trim() === certificateId) setRetry((r) => r + 1)
                else navigate(`/verify/${encodeURIComponent(serial.trim())}`)
              }}
            >
              <Input
                label="Certificate serial number"
                value={serial}
                required
                maxLength={80}
                onChange={(e) => setSerial(e.target.value)}
                placeholder="SP-…"
              />
              <Button type="submit" disabled={loading || !serial.trim()}>
                {loading ? "Checking…" : "Verify certificate"}
              </Button>
            </form>
          </section>
          {loading && <p role="status">Loading the issuer's certificate record…</p>}
          {error && (
            <section className="sporg-alert" role="alert">
              <p>{error}</p>
              <Button variant="outline" onClick={() => setRetry((r) => r + 1)}>
                Retry lookup
              </Button>
            </section>
          )}
          {certificate && (
            <section className="sporg-card" aria-live="polite">
              <p className="sporg-eyebrow">{certificate.status}</p>
              <h2>{certificate.programName ?? "Certificate record"}</h2>
              <dl>
                <dt>Serial number</dt>
                <dd>{certificate.serialNumber}</dd>
                <dt>Issuer snapshot</dt>
                <dd>{certificate.issuerName ?? "Historical issuer snapshot unavailable"}</dd>
                <dt>Issued</dt>
                <dd>{new Date(certificate.issuedAt).toLocaleString()}</dd>
                {certificate.revokedAt && (
                  <>
                    <dt>Revoked</dt>
                    <dd>{new Date(certificate.revokedAt).toLocaleString()}</dd>
                  </>
                )}
              </dl>
              <p>
                {certificate.status === "REVOKED"
                  ? "This certificate has been revoked by its issuer."
                  : "The issuer's record marks this certificate as issued."}
              </p>
              <p>
                Blockchain proof: not anchored. This lookup reports the platform's business record.
              </p>
            </section>
          )}
        </div>
      </main>
      <PublicFooter />
    </div>
  )
}
