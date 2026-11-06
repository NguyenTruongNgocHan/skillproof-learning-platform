import AdminPageHeader from "../components/AdminPageHeader"
import AdminStatusBadge from "../components/AdminStatusBadge"
import { useCallback, useState } from "react"
import AppShell from "@/shared/components/layout/AppShell"
import { governanceApi } from "@/features/organization/api/governanceApi"
import { EvidenceManager } from "@/features/organization/components/EvidenceManager"
import { useRemote, useAction } from "@/shared/hooks/useRemote"
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState"
import AdminTable, { type Column } from "../components/AdminTable"
import AdminDialog from "../components/AdminDialog"
import "../styles/admin-console.css"
type Issuer = Awaited<ReturnType<typeof governanceApi.pending>>[number]
const columns: Column<Issuer>[] = [
  {
    key: "organization",
    label: "Organization",
    render: (r) => <span className="adm-primary-cell">{r.organizationId}</span>,
    sortValue: (r) => r.organizationId,
  },
  { key: "scope", label: "Requested scope", render: (r) => r.scope, sortValue: (r) => r.scope },
  { key: "mode", label: "Scope mode", render: (r) => r.scopeMode, sortValue: (r) => r.scopeMode },
  {
    key: "status",
    label: "Status",
    render: (r) => <AdminStatusBadge value={r.status} />,
    sortValue: (r) => r.status,
  },
]
export default function AdminIssuerReviewPage() {
  const pending = useRemote(useCallback(() => governanceApi.pending(), []))
  const [selected, setSelected] = useState<Issuer | null>(null)
  const [reason, setReason] = useState("")
  const [expiry, setExpiry] = useState("")
  const action = useAction(pending.reload)
  async function decide(status: string) {
    if (
      !selected ||
      !reason.trim() ||
      (status === "AUTHORIZED" && !expiry) ||
      !window.confirm(`Confirm ${status.toLowerCase()}?`)
    )
      return
    await action.run(
      () =>
        governanceApi.decide(selected.id, {
          status,
          reason,
          validUntil: status === "AUTHORIZED" ? new Date(expiry).toISOString() : null,
        }),
      "Issuer decision recorded.",
    )
    setSelected(null)
  }
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Certificate governance"
          title="Issuer authorization"
          description="Verify organization evidence, requested scope and issuance authority."
        />
        <RemoteState {...pending} retry={pending.reload} />
        <ActionNotice {...action} />
        <AdminTable
          rows={pending.data || []}
          columns={columns}
          searchText={(r) => `${r.organizationId} ${r.scope} ${r.status}`}
          filters={[
            { key: "status", label: "Status", value: (r) => r.status },
            { key: "mode", label: "Scope mode", value: (r) => r.scopeMode },
          ]}
          onOpen={(r) => {
            setSelected(r)
            setReason("")
            setExpiry("")
          }}
          empty="No issuer applications awaiting review."
        />
        {selected && (
          <AdminDialog
            title="Issuer authorization"
            subtitle={`${selected.organizationId} · ${selected.status}`}
            onClose={() => setSelected(null)}
            footer={
              <>
                <button className="adm-btn" onClick={() => setSelected(null)}>
                  Close
                </button>
                {(selected.status === "PENDING"
                  ? ["REJECTED", "AUTHORIZED"]
                  : selected.status === "AUTHORIZED"
                    ? ["SUSPENDED", "REVOKED"]
                    : []
                ).map((status) => (
                  <button
                    key={status}
                    className={status === "AUTHORIZED" ? "adm-btn adm-btn-primary" : "adm-btn"}
                    disabled={action.busy || !reason.trim() || (status === "AUTHORIZED" && !expiry)}
                    onClick={() => void decide(status)}
                  >
                    {status.toLowerCase()}
                  </button>
                ))}
              </>
            }
          >
            <dl className="adm-detail-list">
              <dt>Scope</dt>
              <dd>{selected.scope}</dd>
              <dt>Scope mode</dt>
              <dd>{selected.scopeMode}</dd>
              <dt>Course versions</dt>
              <dd>{String(selected.courseVersionIds || "All courses")}</dd>
            </dl>
            <div className="adm-dialog-grid" style={{ marginTop: 20 }}>
              <section className="adm-panel">
                <h3>Issuer evidence</h3>
                <EvidenceManager organizationId={selected.organizationId} purpose="ISSUER" admin />
              </section>
              <section className="adm-panel">
                <h3>Decision</h3>
                <label className="adm-muted">
                  Decision reason
                  <textarea
                    className="adm-review-note"
                    maxLength={1000}
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                  />
                </label>
                <label className="adm-muted">
                  Authorization expiry
                  <input
                    className="adm-field"
                    style={{ display: "block", marginTop: 8 }}
                    type="datetime-local"
                    value={expiry}
                    onChange={(e) => setExpiry(e.target.value)}
                  />
                </label>
              </section>
            </div>
          </AdminDialog>
        )}
      </section>
    </AppShell>
  )
}
