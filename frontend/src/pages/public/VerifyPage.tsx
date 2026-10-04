import PublicHeader from "@/components/layout/PublicHeader"
import PublicFooter from "@/components/layout/PublicFooter"
import VerificationSection from "@/features/marketing/components/VerificationSection"
import { useParams } from "react-router-dom"
import { useEffect, useState } from "react"
import { organizationApi, type Certificate } from "@/features/organization/organizationApi"

export default function VerifyPage() {
  const { certificateId } = useParams()
  const [certificate, setCertificate] = useState<Certificate | null>(null)
  const [error, setError] = useState("")
  useEffect(() => {
    if (!certificateId) return
    organizationApi.verifyCertificate(certificateId).then(setCertificate).catch((reason) => {
      setError(reason instanceof Error ? reason.message : "Certificate not found")
    })
  }, [certificateId])
  return (
    <div className="min-h-full flex flex-col">
      <PublicHeader />
      <main className="flex-1">
        <VerificationSection />
        {certificateId && (
          <section className="mx-auto max-w-3xl px-6 pb-16" aria-live="polite">
            {error ? <p role="alert">{error}</p> : certificate ? (
              <p>Certificate <strong>{certificate.serialNumber}</strong> is {certificate.status.toLowerCase()}.</p>
            ) : <p>Checking certificate…</p>}
          </section>
        )}
      </main>
      <PublicFooter />
    </div>
  )
}
