import Input from "@/shared/ui/Input"
import { useCallback, useState } from "react"
import AppShell from "@/shared/components/layout/AppShell"
import AdminPageHeader from "../components/AdminPageHeader"
import AdminTable from "../components/AdminTable"
import AdminDialog from "../components/AdminDialog"
import { adminCourseApi, type ModeratedCourse } from "../api/adminCourseApi"
import { useRemote, useAction } from "@/shared/hooks/useRemote"
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState"
import Button from "@/shared/ui/Button"
import "../styles/admin-console.css"
export default function AdminCourseModerationPage() {
  const [query, setQuery] = useState("")
  const [suspended, setSuspended] = useState("")
  const [page, setPage] = useState(0)
  const remote = useRemote(
    useCallback(() => adminCourseApi.search(query, suspended, page), [query, suspended, page]),
  )
  const [selected, setSelected] = useState<ModeratedCourse | null>(null)
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Content governance"
          title="Course moderation"
          description="Suspend protected course access or restore it. Sales availability and immutable content versions remain separate."
        />
        <div className="platform-filterbar">
          <Input
            label="Search all courses"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value)
              setPage(0)
            }}
          />
          <label>
            Moderation status
            <select
              value={suspended}
              onChange={(e) => {
                setSuspended(e.target.value)
                setPage(0)
              }}
            >
              <option value="">All</option>
              <option value="true">Suspended</option>
              <option value="false">Not suspended</option>
            </select>
          </label>
        </div>
        <RemoteState {...remote} retry={remote.reload} />
        {remote.data && (
          <AdminTable
            rows={remote.data.content}
            columns={[
              {
                key: "title",
                label: "Course",
                render: (c) => (
                  <>
                    <strong>{c.title}</strong>
                    <small className="adm-secondary-cell">{c.slug}</small>
                  </>
                ),
              },
              { key: "owner", label: "Owner", render: (c) => c.organizationId ?? "Community" },
              {
                key: "suspension",
                label: "Moderation",
                render: (c) => (c.suspended ? "Suspended" : "Not suspended"),
              },
            ]}
            searchText={(c) => c.title}
            onOpen={setSelected}
            serverPage={page}
            serverTotal={remote.data.totalElements}
            serverNext={page + 1 < remote.data.totalPages}
            onServerPage={setPage}
          />
        )}
        {selected && (
          <ModerationDetail
            key={selected.id}
            id={selected.id}
            close={() => setSelected(null)}
            saved={() => {
              remote.reload()
            }}
          />
        )}
      </section>
    </AppShell>
  )
}
function ModerationDetail({
  id,
  close,
  saved,
}: {
  id: string
  close: () => void
  saved: () => void
}) {
  const remote = useRemote(useCallback(() => adminCourseApi.detail(id), [id]))
  const action = useAction(() => {
    remote.reload()
    saved()
  })
  const [reason, setReason] = useState("")
  return (
    <AdminDialog
      title="Course moderation"
      onClose={() => {
        if (!action.busy) close()
      }}
    >
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data && (
        <>
          <h3>{remote.data.title}</h3>
          <p>{remote.data.suspended ? "Suspended" : "Not suspended"}</p>
          <p>{remote.data.moderationReason ?? "No moderation decision recorded."}</p>
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault()
              if (
                window.confirm(
                  remote.data!.suspended
                    ? "Restore this course?"
                    : "Suspend protected access to this course?",
                )
              )
                void action.run(async () => {
                  await adminCourseApi.moderate(id, !remote.data!.suspended, reason.trim())
                  setReason("")
                }, "Moderation decision recorded.")
            }}
          >
            <label>
              Decision reason
              <textarea
                required
                maxLength={1000}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                disabled={action.busy}
              />
            </label>
            <Button type="submit" disabled={action.busy || !reason.trim()}>
              {remote.data.suspended ? "Restore course" : "Suspend course"}
            </Button>
          </form>
        </>
      )}
    </AdminDialog>
  )
}
