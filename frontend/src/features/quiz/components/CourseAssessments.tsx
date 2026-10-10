import { useCallback, useState } from "react";
import type { ActivityOwner } from "@/features/course/types/course.types";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import type { Assessment, Kind } from "../types/quiz.types";
import { quizApi } from "../api/quizApi";
import { AssessmentEditor } from "./AssessmentEditor";
export function CourseAssessments({
  versionId,
  owners,
  editable,
}: {
  versionId: string;
  owners: ActivityOwner[];
  editable: boolean;
}) {
  const [editorBusy, setEditorBusy] = useState(false);
  const remote = useRemote(
    useCallback(() => quizApi.assessments(versionId), [versionId]),
  );
  const action = useAction(remote.reload);
  const [open, setOpen] = useState(false),
    [selected, setSelected] = useState<Assessment | null>(null);
  const [form, setForm] = useState({
    title: "",
    kind: "PRACTICE" as Kind,
    durationSeconds: 600,
    passPercent: 60,
    maxAttempts: 3,
  });
  return (
    <>
      <div className="sporg-toolbar">
        <div>
          <h2>Assessments</h2>
          <p>
            Practice and mock activities are optional. Official assessments may
            be required.
          </p>
        </div>
        {editable && (
          <Button onClick={() => setOpen(true)}>Create assessment</Button>
        )}
      </div>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data?.length === 0 && (
        <section className="sporg-empty">
          <h3>No assessments yet</h3>
          <p>
            Create an assessment and attach questions from your organization
            banks.
          </p>
        </section>
      )}
      {remote.data?.map((a) => (
        <section className="sporg-card sporg-assignment-row" key={a.id}>
          <div>
            <h3>{a.title}</h3>
            <span className="sporg-tag">{a.kind.toLowerCase()}</span>{" "}
            <span className="sporg-tag">{a.status.toLowerCase()}</span>
            <p>
              {Math.round(a.duration_seconds / 60)} min · Pass {a.pass_percent}%
              · {a.max_attempts} attempts
            </p>
          </div>
          <div className="sporg-actions">
            <Button variant="outline" onClick={() => setSelected(a)}>
              Open
            </Button>
            {editable && a.status === "DRAFT" && (
              <Button
                variant="ghost"
                disabled={action.busy}
                onClick={() => {
                  if (window.confirm("Delete this draft assessment?"))
                    void action.run(
                      () => quizApi.deleteAssessment(a.id),
                      "Assessment deleted.",
                    );
                }}
              >
                Delete
              </Button>
            )}
          </div>
        </section>
      ))}
      {selected && (
        <WorkspaceDialog
          busy={editorBusy}
          title={selected.title}
          close={() => setSelected(null)}
        >
          <AssessmentEditor
            onBusyChange={setEditorBusy}
            key={selected.id}
            assessment={selected}
            owners={owners}
            editable={editable && selected.status === "DRAFT"}
            refresh={() => {
              remote.reload();
              setSelected(null);
            }}
          />
        </WorkspaceDialog>
      )}
      {open && (
        <WorkspaceDialog
          title="Create assessment"
          busy={action.busy}
          close={() => {
            if (window.confirm("Discard this assessment?")) setOpen(false);
          }}
        >
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              void action.run(async () => {
                const a = await quizApi.createAssessment(versionId, form);
                setOpen(false);
                setSelected(a);
                setForm({
                  title: "",
                  kind: "PRACTICE",
                  durationSeconds: 600,
                  passPercent: 60,
                  maxAttempts: 3,
                });
              }, "Draft assessment created.");
            }}
          >
            <Input
              label="Title"
              required
              maxLength={180}
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
            <label>
              Kind
              <select
                value={form.kind}
                onChange={(e) =>
                  setForm({ ...form, kind: e.target.value as Kind })
                }
              >
                {["PRACTICE", "MOCK", "OFFICIAL"].map((k) => (
                  <option key={k}>{k}</option>
                ))}
              </select>
            </label>
            <div className="sporg-fields">
              <Input
                label="Duration (seconds)"
                type="number"
                required
                min={60}
                max={14400}
                value={form.durationSeconds}
                onChange={(e) =>
                  setForm({ ...form, durationSeconds: Number(e.target.value) })
                }
              />
              <Input
                label="Pass score (%)"
                type="number"
                required
                min={1}
                max={100}
                value={form.passPercent}
                onChange={(e) =>
                  setForm({ ...form, passPercent: Number(e.target.value) })
                }
              />
              <Input
                label="Maximum attempts"
                type="number"
                required
                min={1}
                max={20}
                value={form.maxAttempts}
                onChange={(e) =>
                  setForm({ ...form, maxAttempts: Number(e.target.value) })
                }
              />
            </div>
            <p>
              The draft is initially owned by the course. Configure its location
              and questions before publishing.
            </p>
            <ActionNotice {...action} />
            <Button type="submit" disabled={action.busy}>
              Create draft
            </Button>
          </form>
        </WorkspaceDialog>
      )}
    </>
  );
}
