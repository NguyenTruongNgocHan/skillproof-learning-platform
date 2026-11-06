import AdminPageHeader from "../components/AdminPageHeader"
import { useCallback, useState } from "react"
import AppShell from "@/shared/components/layout/AppShell"
import MediaPanel from "@/features/media/components/MediaPanel"
import { libraryApi } from "@/features/library/api/libraryApi"
import { useRemote, useAction } from "@/shared/hooks/useRemote"
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState"
import AdminTable, { type Column } from "../components/AdminTable"
import AdminDialog from "../components/AdminDialog"
import "../styles/admin-console.css"
type Resource = Awaited<ReturnType<typeof libraryApi.reviewQueue>>[number]
const columns: Column<Resource>[] = [
  {
    key: "title",
    label: "Resource",
    render: (r) => <span className="adm-primary-cell">{r.title}</span>,
    sortValue: (r) => r.title,
  },
  {
    key: "id",
    label: "Resource ID",
    render: (r) => <span className="adm-secondary-cell">{r.id.slice(0, 16)}…</span>,
  },
]
export default function AdminLibraryReviewPage() {
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState("")
  const [reason, setReason] = useState("")
  const queue = useRemote(useCallback(() => libraryApi.reviewQueue(page), [page]))
  const detail = useRemote(
    useCallback(
      () => (selected ? libraryApi.reviewDetail(selected) : Promise.resolve(null)),
      [selected],
    ),
  )
  const action = useAction(() => {
    queue.reload()
    detail.reload()
  })
  const open = (r: Resource) => {
    setSelected(r.id)
    setReason("")
  }
  async function decide(approved: boolean) {
    if (!reason.trim() || !window.confirm(`${approved ? "Approve" : "Reject"} this resource?`))
      return
    await action.run(
      () => libraryApi.review(selected, approved, reason),
      approved ? "Resource published." : "Resource returned with reason.",
    )
    setSelected("")
  }
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Learning governance"
          title="Resource review"
          description="Inspect learning content, media and metadata before publishing."
        />
        <RemoteState {...queue} retry={queue.reload} />
        <ActionNotice {...action} />
        <AdminTable
          rows={queue.data || []}
          columns={columns}
          searchText={(r) => `${r.title} ${r.id}`}
          onOpen={open}
          serverPage={page}
          serverNext={(queue.data?.length || 0) === 20}
          onServerPage={setPage}
          empty="No pending resources."
        />
        {selected && (
          <AdminDialog
            title={detail.data?.title || "Resource review"}
            subtitle={selected}
            onClose={() => setSelected("")}
            footer={
              <>
                <button className="adm-btn" onClick={() => setSelected("")}>
                  Close
                </button>
                <button
                  className="adm-btn"
                  disabled={action.busy || !reason.trim()}
                  onClick={() => void decide(false)}
                >
                  Reject
                </button>
                <button
                  className="adm-btn adm-btn-primary"
                  disabled={action.busy || !reason.trim()}
                  onClick={() => void decide(true)}
                >
                  Approve
                </button>
              </>
            }
          >
            <RemoteState {...detail} retry={detail.reload} />
            {detail.data && (
              <div className="adm-dialog-grid">
                <section className="adm-panel">
                  <h3>Resource content</h3>
                  <p className="adm-muted">{detail.data.summary}</p>
                  <div
                    style={{
                      whiteSpace: "pre-wrap",
                      fontSize: 13,
                      maxHeight: 320,
                      overflow: "auto",
                    }}
                  >
                    {detail.data.body}
                  </div>
                  <p className="adm-muted">
                    {detail.data.priceVnd.toLocaleString("vi-VN")} VND · {detail.data.accessMode}
                  </p>
                  <MediaPanel scope="LIBRARY" target={detail.data.id} />
                </section>
                <section className="adm-panel">
                  <h3>Review decision</h3>
                  <p className="adm-muted">
                    Assess clarity, completeness, content quality and policy compliance.
                  </p>
                  <textarea
                    className="adm-review-note"
                    aria-label="Resource review reason"
                    value={reason}
                    maxLength={1000}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="Required reason…"
                  />
                </section>
              </div>
            )}
          </AdminDialog>
        )}
      </section>
    </AppShell>
  )
}
