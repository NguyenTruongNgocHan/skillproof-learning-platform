import AdminPageHeader from "../components/AdminPageHeader"
import AdminStatusBadge from "../components/AdminStatusBadge"
import { useCallback, useState } from "react"
import AppShell from "@/shared/components/layout/AppShell"
import { feedbackApi } from "@/features/feedback/api/feedbackApi"
import { ReviewModerationHistory } from "@/features/feedback/components/ReviewModerationHistory"
import { useRemote, useAction } from "@/shared/hooks/useRemote"
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState"
import AdminTable, { type Column } from "../components/AdminTable"
import AdminDialog from "../components/AdminDialog"
import "../styles/admin-console.css"
type Report = Awaited<ReturnType<typeof feedbackApi.reports>>["content"][number]
const columns: Column<Report>[] = [
  {
    key: "review",
    label: "Reported review",
    render: (r) => <span className="adm-primary-cell">{r.reviewId}</span>,
    sortValue: (r) => r.reviewId,
  },
  {
    key: "reason",
    label: "Report reason",
    render: (r) => <span className="adm-secondary-cell">{r.reason}</span>,
    sortValue: (r) => r.reason,
  },
  { key: "status", label: "Status", render: () => <AdminStatusBadge value="Pending" /> },
]
export default function AdminFeedbackModerationPage() {
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState<Report | null>(null)
  const [reason, setReason] = useState("")
  const remote = useRemote(useCallback(() => feedbackApi.reports(page), [page]))
  const action = useAction(remote.reload)
  async function resolve(hide: boolean) {
    if (!selected || !reason.trim() || !window.confirm(`${hide ? "Hide" : "Retain"} this review?`))
      return
    await action.run(() => feedbackApi.resolve(selected.id, hide, reason), "Report resolved.")
    setSelected(null)
  }
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Trust & safety"
          title="Feedback moderation"
          description="Review reported feedback, record a policy reason and preserve the audit trail."
        />
        <RemoteState {...remote} retry={remote.reload} />
        <ActionNotice {...action} />
        <AdminTable
          rows={remote.data?.content || []}
          columns={columns}
          searchText={(r) => `${r.reviewId} ${r.reason}`}
          onOpen={(r) => {
            setSelected(r)
            setReason("")
          }}
          serverPage={page}
          serverNext={Boolean(remote.data && page + 1 < remote.data.totalPages)}
          onServerPage={setPage}
          serverTotal={remote.data?.totalElements}
          empty="No pending reports."
        />
        {selected && (
          <AdminDialog
            title="Moderate reported review"
            subtitle={selected.reviewId}
            onClose={() => setSelected(null)}
            footer={
              <>
                <button className="adm-btn" onClick={() => setSelected(null)}>
                  Close
                </button>
                <button
                  className="adm-btn"
                  disabled={action.busy || !reason.trim()}
                  onClick={() => void resolve(false)}
                >
                  Keep review
                </button>
                <button
                  className="adm-btn adm-btn-primary"
                  disabled={action.busy || !reason.trim()}
                  onClick={() => void resolve(true)}
                >
                  Hide review
                </button>
              </>
            }
          >
            <p className="adm-muted">Reported reason: {selected.reason}</p>
            <ReviewDetail id={selected.reviewId} />
            <label className="adm-muted">
              Resolution reason
              <textarea
                className="adm-review-note"
                value={reason}
                maxLength={1000}
                onChange={(e) => setReason(e.target.value)}
                placeholder="Policy reason required…"
              />
            </label>
          </AdminDialog>
        )}
      </section>
    </AppShell>
  )
}
function ReviewDetail({ id }: { id: string }) {
  const remote = useRemote(useCallback(() => feedbackApi.adminReview(id), [id]))
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <section className="adm-panel">
          <strong>
            {remote.data.rating}/5 · {remote.data.type}
          </strong>
          <p>{remote.data.body}</p>
          <p className="adm-muted">{remote.data.reply}</p>
          <ReviewModerationHistory id={id} />
        </section>
      )}
    </>
  )
}
