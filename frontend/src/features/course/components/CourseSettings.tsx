import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { useAction } from "@/shared/hooks/useRemote"
import { ActionNotice } from "@/shared/ui/RemoteState"
import Button from "@/shared/ui/Button"
import Input from "@/shared/ui/Input"
import type { CourseRecord, CourseOutline } from "../types/course.types"
import { courseApi } from "../api/courseApi"
export function CourseSettings({
  course,
  outline,
  refresh,
  onDirtyChange,
}: {
  course: CourseRecord
  outline: CourseOutline
  refresh: () => void
  onDirtyChange?: (dirty: boolean) => void
}) {
  const editable = outline.version.status === "DRAFT"
  const [title, setTitle] = useState(course.title),
    [summary, setSummary] = useState(course.summary)
  const [official, setOfficial] = useState(outline.policy.require_official_assessments)
  const { setDirty } = useOrganizationContext()
  const detailsDirty = title !== course.title || summary !== course.summary
  const policyDirty =
    official !== outline.policy.require_official_assessments ||
    !outline.policy.require_all_resources
  const dirty = detailsDirty || policyDirty
  const action = useAction(refresh)
  useEffect(() => {
    setDirty("course-settings", dirty)
    onDirtyChange?.(dirty)
    return () => {
      setDirty("course-settings", false)
      onDirtyChange?.(false)
    }
  }, [dirty, onDirtyChange, setDirty])
  function canSave(otherDirty: boolean) {
    return (
      !otherDirty ||
      window.confirm(
        "Saving reloads the saved course. Discard unsaved changes in the other settings form?",
      )
    )
  }
  return (
    <>
      <ActionNotice {...action} />
      <div className="sporg-grid">
        <section className="sporg-card">
          <h2>Course details</h2>
          <form
            className="sporg-form"
            onSubmit={(event) => {
              event.preventDefault()
              if (editable && canSave(policyDirty))
                void action.run(
                  () => courseApi.edit(course.id, { title: title.trim(), summary: summary.trim() }),
                  "Course details saved.",
                )
            }}
          >
            <Input
              label="Title"
              required
              maxLength={180}
              disabled={!editable || action.busy}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
            <label>
              Summary
              <textarea
                required
                maxLength={1000}
                disabled={!editable || action.busy}
                value={summary}
                onChange={(e) => setSummary(e.target.value)}
              />
            </label>
            {editable && (
              <Button type="submit" disabled={action.busy || !detailsDirty}>
                Save details
              </Button>
            )}
          </form>
        </section>
        <section className="sporg-card">
          <h2>Completion rules</h2>
          <form
            className="sporg-form"
            onSubmit={(event) => {
              event.preventDefault()
              if (editable && canSave(detailsDirty))
                void action.run(
                  () =>
                    courseApi.policy(outline.version.id, {
                      requireAllResources: true,
                      requireOfficialAssessments: official,
                    }),
                  "Completion policy saved.",
                )
            }}
          >
            <label className="sporg-checkbox">
              <input type="checkbox" checked disabled readOnly /> Required resources must be
              completed
            </label>
            <label className="sporg-checkbox">
              <input
                type="checkbox"
                disabled={!editable || action.busy}
                checked={official}
                onChange={(e) => setOfficial(e.target.checked)}
              />{" "}
              Require official assessments
            </label>
            <p>
              All lessons, required resources, required official assessments and required
              assignments must be completed. Optional resources and practice/mock assessments do not
              block completion. Course completion does not automatically issue a certificate.
            </p>
            {editable && (
              <Button type="submit" disabled={action.busy || !policyDirty}>
                Save completion policy
              </Button>
            )}
          </form>
        </section>
        <section className="sporg-card sporg-full">
          <h2>Access & pricing</h2>
          <p>
            Initial and subsequent commercial prices are managed in the pricing workspace with
            MANAGE_PRICING authority. Sales availability remains separate from content publication.
          </p>
          <Link to={`/organizer/pricing?version=${encodeURIComponent(outline.version.id)}`}>
            Open pricing for this course version
          </Link>
          <p>
            Version ID for your pricing manager: <code>{outline.version.id}</code>
          </p>
        </section>
      </div>
    </>
  )
}
