import { useCallback, useState } from "react";
import type { ActivityOwner } from "@/features/course/types/course.types";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import WorkspaceDialog from "@/features/organization/components/WorkspaceDialog";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { assignmentApi } from "../api/assignmentApi";
import type { Assignment } from "../types/assignment.types";
import { AssignmentSubmissions } from "./AssignmentSubmissions";
export function CourseAssignments({
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
    useCallback(() => assignmentApi.list(versionId), [versionId]),
  );
  const action = useAction(remote.reload);
  const [open, setOpen] = useState(false),
    [selected, setSelected] = useState<Assignment | null>(null);
  const [form, setForm] = useState({
    title: "",
    instructions: "",
    ownerId: versionId,
    required: true,
    passPercent: 60,
    maxSubmissions: 3,
    dueAt: "",
  });
  return (
    <>
      <div className="sporg-toolbar">
        <div>
          <h2>Assignments</h2>
          <p>Written or file-based work, reviewed by your organization.</p>
        </div>
        {editable && (
          <Button
            onClick={() => {
              setForm({
                title: "",
                instructions: "",
                ownerId: versionId,
                required: true,
                passPercent: 60,
                maxSubmissions: 3,
                dueAt: "",
              });
              setOpen(true);
            }}
          >
            Add assignment
          </Button>
        )}
      </div>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data?.length === 0 && (
        <div className="sporg-empty">
          <h3>No assignments yet</h3>
          <p>Add an assignment when learner work needs manual evaluation.</p>
        </div>
      )}
      {remote.data?.map((a) => (
        <section className="sporg-card sporg-assignment-row" key={a.id}>
          <div>
            <h3>{a.title}</h3>
            <span className="sporg-tag">
              {a.required ? "Required" : "Optional"}
            </span>
            <p>
              {owners.find((o) => o.id === a.ownerId)?.label ?? a.ownerScope} ·
              Pass {a.passPercent}% · {a.maxSubmissions} submissions
            </p>
            <p className="sporg-lesson-body">{a.instructions}</p>
            {a.dueAt && <small>Due {new Date(a.dueAt).toLocaleString()}</small>}
          </div>
          <div className="sporg-actions">
            <Button variant="outline" onClick={() => setSelected(a)}>
              Review submissions
            </Button>
            {editable && (
              <Button
                variant="ghost"
                disabled={action.busy}
                onClick={() => {
                  if (window.confirm("Delete this assignment?"))
                    void action.run(
                      () => assignmentApi.remove(a.id),
                      "Assignment deleted.",
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
          title={selected.title + " · submissions"}
          close={() => setSelected(null)}
        >
          <AssignmentSubmissions
            onBusyChange={setEditorBusy}
            assignment={selected}
          />
        </WorkspaceDialog>
      )}
      {open && (
        <WorkspaceDialog
          title="Add assignment"
          busy={action.busy}
          close={() => {
            if (window.confirm("Discard this assignment?")) setOpen(false);
          }}
        >
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              const owner = owners.find((o) => o.id === form.ownerId);
              if (!owner) return;
              void action.run(async () => {
                await assignmentApi.create(versionId, {
                  ...form,
                  ownerScope: owner.scope,
                  dueAt: form.dueAt ? new Date(form.dueAt).toISOString() : null,
                });
                setOpen(false);
              }, "Assignment created.");
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
              Instructions
              <textarea
                required
                maxLength={20000}
                value={form.instructions}
                onChange={(e) =>
                  setForm({ ...form, instructions: e.target.value })
                }
              />
            </label>
            <label>
              Location
              <select
                value={form.ownerId}
                onChange={(e) => setForm({ ...form, ownerId: e.target.value })}
              >
                {owners.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.label}
                  </option>
                ))}
              </select>
            </label>
            <div className="sporg-fields">
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
                label="Maximum submissions"
                type="number"
                required
                min={1}
                max={20}
                value={form.maxSubmissions}
                onChange={(e) =>
                  setForm({ ...form, maxSubmissions: Number(e.target.value) })
                }
              />
              <Input
                label="Due date (optional, your local time)"
                type="datetime-local"
                value={form.dueAt}
                onChange={(e) => setForm({ ...form, dueAt: e.target.value })}
              />
            </div>
            <label className="sporg-checkbox">
              <input
                type="checkbox"
                checked={form.required}
                onChange={(e) =>
                  setForm({ ...form, required: e.target.checked })
                }
              />{" "}
              Required for completion
            </label>
            <ActionNotice {...action} />
            <Button type="submit" disabled={action.busy}>
              Create assignment
            </Button>
          </form>
        </WorkspaceDialog>
      )}
    </>
  );
}
