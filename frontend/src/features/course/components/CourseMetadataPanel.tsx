import { useCallback, useState } from "react"
import { apiClient } from "@/shared/api/apiClient"
import { useRemote, useAction } from "@/shared/hooks/useRemote"
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState"
import Input from "@/shared/ui/Input"
import Button from "@/shared/ui/Button"
export interface CourseMetadata {
  domainTag: string
  difficulty: "UNSPECIFIED" | "BEGINNER" | "INTERMEDIATE" | "ADVANCED"
  skills: string[]
  prerequisiteText: string
}
export function CourseMetadataPanel({
  versionId,
  editable,
  onChanged,
}: {
  versionId: string
  editable: boolean
  onChanged: () => void
}) {
  const remote = useRemote(
    useCallback(
      () =>
        apiClient.get<CourseMetadata>(
          `/courses/versions/${encodeURIComponent(versionId)}/metadata`,
        ),
      [versionId],
    ),
  )
  return (
    <section className="sporg-card">
      <h2>Audience & skills</h2>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <MetadataForm
          key={versionId + JSON.stringify(remote.data)}
          initial={remote.data}
          editable={editable}
          versionId={versionId}
          saved={() => {
            remote.reload()
            onChanged()
          }}
        />
      )}
    </section>
  )
}
function MetadataForm({
  initial,
  editable,
  versionId,
  saved,
}: {
  initial: CourseMetadata
  editable: boolean
  versionId: string
  saved: () => void
}) {
  const [form, setForm] = useState(initial)
  const [skills, setSkills] = useState(initial.skills.join(", "))
  const action = useAction(saved)
  return (
    <form
      className="sporg-form"
      onSubmit={(event) => {
        event.preventDefault()
        void action.run(() =>
          apiClient.put(`/courses/versions/${encodeURIComponent(versionId)}/metadata`, {
            ...form,
            skills: [
              ...new Set(
                skills
                  .split(",")
                  .map((s) => s.trim())
                  .filter(Boolean),
              ),
            ],
          }),
        )
      }}
    >
      <fieldset disabled={!editable || action.busy}>
        <Input
          label="Domain tag"
          required
          pattern="[a-z0-9]+(?:-[a-z0-9]+)*"
          maxLength={80}
          value={form.domainTag ?? ""}
          onChange={(e) => setForm({ ...form, domainTag: e.target.value })}
        />
        <label>
          Difficulty
          <select
            value={form.difficulty}
            onChange={(e) =>
              setForm({ ...form, difficulty: e.target.value as CourseMetadata["difficulty"] })
            }
          >
            {["UNSPECIFIED", "BEGINNER", "INTERMEDIATE", "ADVANCED"].map((d) => (
              <option key={d}>{d}</option>
            ))}
          </select>
        </label>
        <Input
          label="Skills (comma separated, up to 20)"
          value={skills}
          onChange={(e) => setSkills(e.target.value)}
        />
        <label>
          Prerequisite knowledge
          <textarea
            maxLength={1000}
            value={form.prerequisiteText ?? ""}
            onChange={(e) => setForm({ ...form, prerequisiteText: e.target.value })}
          />
        </label>
        <p>Prerequisites describe expected knowledge. They do not enforce a learning sequence.</p>
        {editable && (
          <Button
            type="submit"
            disabled={
              action.busy ||
              new Set(
                skills
                  .split(",")
                  .map((s) => s.trim())
                  .filter(Boolean),
              ).size > 20
            }
          >
            Save metadata
          </Button>
        )}
      </fieldset>
      <ActionNotice {...action} />
    </form>
  )
}
