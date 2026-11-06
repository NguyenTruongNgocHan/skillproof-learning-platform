import { useState, useCallback } from "react"
import AppShell from "@/shared/components/layout/AppShell"
import AdminPageHeader from "../components/AdminPageHeader"
import "../styles/admin-console.css"
import { apiClient } from "@/shared/api/apiClient"
import { useRemote } from "@/shared/hooks/useRemote"
import { RemoteState } from "@/shared/ui/RemoteState"
import type { Certificate } from "@/features/certification/types/certification.types"
import { CertificateProvenancePanel } from "@/features/certification/components/CertificateProvenancePanel"
import Input from "@/shared/ui/Input"
import Button from "@/shared/ui/Button"
export default function AdminCertificateGovernancePage() {
  const [lookup, setLookup] = useState(""),
    [id, setId] = useState("")
  const remote = useRemote(
    useCallback(
      () =>
        id
          ? apiClient.get<Certificate>(`/admin/certificates/${encodeURIComponent(id)}`)
          : Promise.resolve(null),
      [id],
    ),
  )
  return (
    <AppShell>
      <section className="adm-page adm-governance">
        <AdminPageHeader
          eyebrow="Credential integrity"
          title="Certificate governance"
          description="Inspect off-chain credential provenance. Revocation belongs to the issuing organization."
        />
        <form
          className="adm-lookup"
          onSubmit={(e) => {
            e.preventDefault()
            setId(lookup.trim())
          }}
        >
          <Input
            label="Certificate UUID"
            required
            value={lookup}
            onChange={(e) => setLookup(e.target.value)}
          />
          <Button type="submit">Find certificate</Button>
        </form>
        <RemoteState {...remote} retry={remote.reload} />
        {remote.data && (
          <section className="adm-panel adm-governance-detail">
            <h2>{remote.data.programName}</h2>
            <p>
              {remote.data.serialNumber} · {remote.data.status}
            </p>
            <p>
              {remote.data.issuerName} · {remote.data.learnerEmail}
            </p>
            <CertificateProvenancePanel serial={remote.data.serialNumber} />
            <p>Only the issuing organization may revoke this certificate.</p>
          </section>
        )}
      </section>
    </AppShell>
  )
}
