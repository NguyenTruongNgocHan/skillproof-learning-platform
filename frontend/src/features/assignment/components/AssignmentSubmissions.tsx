import { useCallback, useState, useEffect } from "react";
import MediaPanel from "@/features/media/components/MediaPanel";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import type { Assignment, Submission } from "../types/assignment.types";
import { assignmentApi } from "../api/assignmentApi";
export function AssignmentSubmissions({
  onBusyChange,
  assignment,
}: {
  onBusyChange?: (busy: boolean) => void;
  assignment: Assignment;
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
  const [selected, setSelected] = useState<Submission | null>(null),
    [score, setScore] = useState(""),
    [feedback, setFeedback] = useState("");
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
              setScore(s.scorePercent === null ? "" : String(s.scorePercent));
              setFeedback(s.feedback ?? "");
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
          {selected.status === "SUBMITTED" ? (
            <form
              className="sporg-form"
              onSubmit={(e) => {
                e.preventDefault();
                if (
                  !window.confirm(
                    "Finalize this grade? The result cannot be edited afterwards.",
                  )
                )
                  return;
                void action.run(async () => {
                  const s = await assignmentApi.grade(selected.id, {
                    scorePercent: Number(score),
                    feedback,
                  });
                  setSelected(s);
                }, "Grade recorded.");
              }}
            >
              <Input
                label="Score (%)"
                type="number"
                required
                min={0}
                max={100}
                value={score}
                onChange={(e) => setScore(e.target.value)}
              />
              <label>
                Feedback
                <textarea
                  required
                  maxLength={2000}
                  value={feedback}
                  onChange={(e) => setFeedback(e.target.value)}
                />
              </label>
              <Button type="submit" disabled={action.busy}>
                Finalize grade
              </Button>
            </form>
          ) : (
            <p>
              {selected.status === "GRADED"
                ? `Final grade: ${selected.scorePercent}% · ${selected.passed ? "Passed" : "Not passed"}. ${selected.feedback ?? ""}`
                : "This draft has not been submitted for grading."}
            </p>
          )}
        </section>
      )}
    </>
  );
}
