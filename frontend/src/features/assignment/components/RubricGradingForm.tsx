import { useCallback, useEffect, useState } from "react";
import { assignmentApi } from "../api/assignmentApi";
import type { Submission, CriterionGrade } from "../types/assignment.types";
import { useRemote, useAction } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import Input from "@/shared/ui/Input";
import Button from "@/shared/ui/Button";
export function RubricGradingForm({
  assignmentId,
  submission,
  saved,
  onBusyChange,
}: {
  assignmentId: string;
  submission: Submission;
  saved: (s: Submission) => void;
  onBusyChange?: (v: boolean) => void;
}) {
  const remote = useRemote(
    useCallback(
      async () => ({
        rubric: await assignmentApi.rubric(assignmentId),
        grades: await assignmentApi.criterionGrades(submission.id),
      }),
      [assignmentId, submission.id],
    ),
  );
  const action = useAction();
  const [values, setValues] = useState<Record<string, CriterionGrade>>({}),
    [feedback, setFeedback] = useState(submission.feedback ?? ""),
    [score, setScore] = useState(submission.scorePercent ?? 0);
  useEffect(() => {
    onBusyChange?.(action.busy);
    return () => onBusyChange?.(false);
  }, [action.busy, onBusyChange]);
  const criteria = remote.data?.rubric ?? [];
  const grade = (id: string) =>
    values[id] ??
    remote.data?.grades.find((g) => g.criterionId === id) ?? {
      criterionId: id,
      score: 0,
      feedback: "",
    };
  const total = criteria.length
    ? Math.round(
        criteria.reduce((n, c) => n + c.weight * grade(c.id).score, 0) / 100,
      )
    : score;
  return (
    <section>
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data && (
        <form
          className="sporg-form"
          onSubmit={(e) => {
            e.preventDefault();
            if (
              !window.confirm("Finalize this grade and its criterion feedback?")
            )
              return;
            void action.run(async () => {
              const result = criteria.length
                ? await assignmentApi.rubricGrade(
                    submission.id,
                    criteria.map((c) => grade(c.id)),
                    feedback,
                  )
                : await assignmentApi.grade(submission.id, {
                    scorePercent: score,
                    feedback,
                  });
              saved(result);
            }, "Final grade saved.");
          }}
        >
          {criteria.map((c) => (
            <fieldset key={c.id}>
              <legend>
                {c.title} · {c.weight}%
              </legend>
              <Input
                type="number"
                label="Criterion score (%)"
                min={0}
                max={100}
                required
                disabled={submission.status !== "SUBMITTED"}
                value={grade(c.id).score}
                onChange={(e) =>
                  setValues({
                    ...values,
                    [c.id]: { ...grade(c.id), score: Number(e.target.value) },
                  })
                }
              />
              <label>
                Criterion feedback
                <textarea
                  required
                  maxLength={1000}
                  disabled={submission.status !== "SUBMITTED"}
                  value={grade(c.id).feedback}
                  onChange={(e) =>
                    setValues({
                      ...values,
                      [c.id]: { ...grade(c.id), feedback: e.target.value },
                    })
                  }
                />
              </label>
            </fieldset>
          ))}
          {!criteria.length && (
            <Input
              type="number"
              label="Overall score (%)"
              min={0}
              max={100}
              required
              disabled={submission.status !== "SUBMITTED"}
              value={score}
              onChange={(e) => setScore(Number(e.target.value))}
            />
          )}
          <strong>Final score: {total}%</strong>
          <label>
            Overall feedback
            <textarea
              required
              maxLength={2000}
              disabled={submission.status !== "SUBMITTED"}
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
            />
          </label>
          <ActionNotice {...action} />
          {submission.status === "SUBMITTED" && (
            <Button type="submit" disabled={action.busy}>
              Finalize grade
            </Button>
          )}
        </form>
      )}
    </section>
  );
}
