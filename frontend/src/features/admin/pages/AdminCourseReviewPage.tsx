import type { CourseOutline } from "@/features/course/types/course.types"
import CourseReviewOutline from "../components/CourseReviewOutline"
import AdminPageHeader from "../components/AdminPageHeader"
import AdminStatusBadge from "../components/AdminStatusBadge"
import { useCallback, useEffect, useState } from "react"
import AppShell from "@/shared/components/layout/AppShell"
import { apiClient } from "@/shared/api/apiClient"
import AdminTable, { type Column } from "../components/AdminTable"
import AdminDialog from "../components/AdminDialog"
import "../styles/admin-console.css"
type Version = {
  id: string
  courseId: string
  versionNo: number
  reviewStatus: string
  publishedTitle?: string
  createdAt?: string
}
const columns: Column<Version>[] = [
  {
    key: "course",
    label: "Course",
    render: (r) => (
      <>
        <span className="adm-primary-cell">{r.publishedTitle || `Course ${r.courseId}`}</span>
        <span className="adm-secondary-cell">{r.courseId}</span>
      </>
    ),
    sortValue: (r) => r.publishedTitle || r.courseId,
  },
  {
    key: "version",
    label: "Version",
    render: (r) => `v${r.versionNo}`,
    sortValue: (r) => r.versionNo,
  },
  {
    key: "status",
    label: "Review status",
    render: (r) => <AdminStatusBadge value={r.reviewStatus} />,
    sortValue: (r) => r.reviewStatus,
  },
  {
    key: "created",
    label: "Created",
    render: (r) => (r.createdAt ? new Date(r.createdAt).toLocaleDateString() : "—"),
    sortValue: (r) => r.createdAt || "",
  },
]
export default function AdminCourseReviewPage() {
  const [rows, setRows] = useState<Version[]>([])
  const [selected, setSelected] = useState<Version | null>(null)
  const [outline, setOutline] = useState<CourseOutline | null>(null)
  const [page, setPage] = useState(0)
  const [reason, setReason] = useState("")
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const [refresh, setRefresh] = useState(0)
  const load = useCallback(() => {
    apiClient
      .get<Version[]>(`/admin/course-versions?page=${page}&size=20`)
      .then(setRows)
      .catch((e) => setError(e instanceof Error ? e.message : "Cannot load courses"))
  }, [page])
  useEffect(() => {
    load()
  }, [load, refresh])
  useEffect(() => {
    let active = true
    setOutline(null)
    setReason("")
    if (selected)
      apiClient
        .get<CourseOutline>(`/admin/course-versions/${selected.id}/outline`)
        .then((r) => {
          if (active) setOutline(r)
        })
        .catch((e) => {
          if (active) setError(e instanceof Error ? e.message : "Outline unavailable")
        })
    return () => {
      active = false
    }
  }, [selected])
  async function decide(approved: boolean) {
    if (!selected || !reason.trim() || busy || outline === null) return
    if (!window.confirm(`${approved ? "Approve" : "Reject"} version ${selected.versionNo}?`)) return
    setBusy(true)
    try {
      await apiClient.post(`/admin/course-versions/${selected.id}/review`, {
        approved,
        reason: reason.trim(),
      })
      setSelected(null)
      setRefresh((v) => v + 1)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Review failed")
    } finally {
      setBusy(false)
    }
  }
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Learning governance"
          title="Course review"
          description="Inspect course versions, learning structure and submission metadata."
        />
        {error && (
          <p className="adm-error" role="alert">
            {error}
          </p>
        )}
        <AdminTable
          rows={rows}
          columns={columns}
          searchText={(r) => `${r.publishedTitle} ${r.courseId} ${r.reviewStatus}`}
          filters={[{ key: "status", label: "Status", value: (r) => r.reviewStatus }]}
          onOpen={setSelected}
          serverPage={page}
          serverNext={rows.length === 20}
          onServerPage={setPage}
          empty="No course versions awaiting review."
        />
        {selected && (
          <AdminDialog
            title={selected.publishedTitle || "Course review"}
            subtitle={`Version ${selected.versionNo} · ${selected.reviewStatus}`}
            onClose={() => {
              if (!busy) setSelected(null)
            }}
            footer={
              <>
                <button className="adm-btn" disabled={busy} onClick={() => setSelected(null)}>
                  Close
                </button>
                <button
                  className="adm-btn"
                  disabled={busy || !reason.trim() || outline === null}
                  onClick={() => void decide(false)}
                >
                  Reject
                </button>
                <button
                  className="adm-btn adm-btn-primary"
                  disabled={busy || !reason.trim() || outline === null}
                  onClick={() => void decide(true)}
                >
                  Approve version
                </button>
              </>
            }
          >
            <div className="adm-dialog-grid">
              <section className="adm-panel">
                <h3>Submitted outline</h3>
                <p className="adm-muted">
                  Nested modules, lessons and assessments are displayed as structured content.
                </p>
                {outline === null ? (
                  <p>Loading outline…</p>
                ) : (
                  <CourseReviewOutline outline={outline} />
                )}
              </section>
              <section className="adm-panel">
                <h3>Reviewer decision</h3>
                <p className="adm-muted">
                  Assess structure, lesson quality, completeness and learning outcomes before
                  deciding.
                </p>
                <textarea
                  className="adm-review-note"
                  aria-label="Course review reason"
                  maxLength={1000}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="Required decision reason…"
                />
              </section>
            </div>
          </AdminDialog>
        )}
      </section>
    </AppShell>
  )
}
