import { RubricGradingForm } from "./RubricGradingForm";
import { useCallback, useState, useEffect } from "react";
import MediaPanel from "@/features/media/components/MediaPanel";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";

import type { Assignment, Submission } from "../types/assignment.types";
import { assignmentApi } from "../api/assignmentApi";
export function AssignmentSubmissions({
  onBusyChange,
  assignment,
  initialSubmission,
}: {
  onBusyChange?: (busy: boolean) => void;
  assignment: Assignment;
  initialSubmission?: Submission;
}) {
  const remote = useRemote(
    useCallback(
      () => assignmentApi.submissions(assignment.id),
      [assignment.id],
    ),
  );
  const action = useAction(remote.reload);
  useEffect(() => {
    onBusyChange?.(action.busy);
    return () => onBusyChange?.(false);
  }, [action.busy, onBusyChange]);
  const [selected, setSelected] = useState<Submission | null>(
    initialSubmission ?? null,
  );
  return (
    <>
      <RemoteState {...remote} retry={remote.reload} />
      <ActionNotice {...action} />
      {remote.data?.length === 0 && <p>No submissions yet.</p>}
      {remote.data?.map((s) => (
        <div className="sporg-row" key={s.id}>
          <div>
            <strong>Learner {s.learnerId}</strong>
            <small>
              {s.status.toLowerCase()} ·{" "}
              {s.submittedAt
                ? new Date(s.submittedAt).toLocaleString()
                : "Not submitted"}
            </small>
          </div>
          <Button
            disabled={action.busy}
            variant="outline"
            onClick={() => {
              setSelected(s);
            }}
          >
            Review
          </Button>
        </div>
      ))}
      {selected && (
        <section key={selected.id} className="sporg-card">
          <h3>Submission</h3>
          <p className="sporg-lesson-body">
            {selected.body || "No text provided."}
          </p>
          {selected.link && (
            <a
              href={
                /^https?:\/\//i.test(selected.link) ? selected.link : undefined
              }
              target="_blank"
              rel="noreferrer"
            >
              Open submitted link ↗
            </a>
          )}
          <MediaPanel scope="SUBMISSION" target={selected.id} />
          <RubricGradingForm
            key={selected.id}
            assignmentId={assignment.id}
            submission={selected}
            onBusyChange={onBusyChange}
            saved={(s) => {
              setSelected(s);
              remote.reload();
            }}
          />
        </section>
      )}
    </>
  );
}
