import { useCallback, useState, useEffect } from "react";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { assignmentApi } from "../api/assignmentApi";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
export function AssignmentRubricEditor({
  assignmentId,
  editable,
}: {
  assignmentId: string;
  editable: boolean;
}) {
  const remote = useRemote(
    useCallback(() => assignmentApi.rubric(assignmentId), [assignmentId]),
  );
  const action = useAction(remote.reload);
  const [rows, setRows] = useState<{ title: string; weight: number }[] | null>(
    null,
  );
  const { setDirty } = useOrganizationContext();
  useEffect(() => {
    const key = `rubric-${assignmentId}`;
    setDirty(key, rows !== null);
    return () => setDirty(key, false);
  }, [assignmentId, rows, setDirty]);
  const values = rows ?? remote.data ?? [];
  const total = values.reduce((n, r) => n + r.weight, 0);
  return (
    <section className="sporg-card">
      <h3>Grading rubric</h3>
      <p>
        Weights must total 100%. Empty rubric uses an overall score. Publish
        freezes the criteria; grades retain individual scores and feedback.
      </p>
      <RemoteState {...remote} retry={remote.reload} />
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          void action.run(async () => {
            await assignmentApi.saveRubric(
              assignmentId,
              values.map((r) => ({ title: r.title, weight: r.weight })),
            );
            setRows(null);
          }, "Rubric saved.");
        }}
      >
        {values.map((r, i) => (
          <div className="sporg-fields" key={i}>
            <Input
              label={`Criterion ${i + 1}`}
              required
              maxLength={180}
              disabled={!editable}
              value={r.title}
              onChange={(e) =>
                setRows(
                  values.map((v, j) =>
                    j === i ? { ...v, title: e.target.value } : v,
                  ),
                )
              }
            />
            <Input
              label="Weight (%)"
              type="number"
              min={1}
              max={100}
              required
              disabled={!editable}
              value={r.weight}
              onChange={(e) =>
                setRows(
                  values.map((v, j) =>
                    j === i ? { ...v, weight: Number(e.target.value) } : v,
                  ),
                )
              }
            />
            {editable && (
              <Button
                type="button"
                variant="ghost"
                onClick={() => setRows(values.filter((_, j) => j !== i))}
              >
                Remove
              </Button>
            )}
          </div>
        ))}
        <p>{values.length ? `Total: ${total}%` : "Overall-score grading"}</p>
        {editable && (
          <div className="sporg-actions">
            <Button
              type="button"
              variant="outline"
              disabled={values.length >= 20 || action.busy}
              onClick={() =>
                setRows([
                  ...values,
                  { title: "", weight: Math.max(1, 100 - total) },
                ])
              }
            >
              Add criterion
            </Button>
            <Button
              type="submit"
              disabled={
                action.busy ||
                remote.loading ||
                Boolean(remote.error) ||
                (values.length > 0 && total !== 100)
              }
            >
              Save rubric
            </Button>
          </div>
        )}
        <ActionNotice {...action} />
      </form>
    </section>
  );
}
