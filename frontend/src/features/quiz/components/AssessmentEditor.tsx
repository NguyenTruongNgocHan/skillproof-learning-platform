import { useCallback, useState, useEffect } from "react";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import type { ActivityOwner } from "@/features/course/types/course.types";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import { RemoteState, ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import type { Assessment } from "../types/quiz.types";
import { quizApi } from "../api/quizApi";
export function AssessmentEditor({
  onBusyChange,
  assessment,
  owners,
  editable,
  refresh,
}: {
  onBusyChange?: (busy: boolean) => void;
  assessment: Assessment;
  owners: ActivityOwner[];
  editable: boolean;
  refresh: () => void;
}) {
  const { organization } = useOrganizationContext();
  const org = organization?.id ?? "";
  const links = useRemote(
    useCallback(
      () => quizApi.assessmentQuestions(assessment.id),
      [assessment.id],
    ),
  );
  const banks = useRemote(useCallback(() => quizApi.banks(org), [org]));
  const [bank, setBank] = useState(""),
    [question, setQuestion] = useState(""),
    [points, setPoints] = useState(1);
  const questions = useRemote(
    useCallback(
      () => (bank ? quizApi.questions(bank) : Promise.resolve([])),
      [bank],
    ),
  );
  const action = useAction(links.reload);
  useEffect(() => {
    onBusyChange?.(action.busy);
    return () => onBusyChange?.(false);
  }, [action.busy, onBusyChange]);
  const [form, setForm] = useState({
    title: assessment.title,
    durationSeconds: assessment.duration_seconds,
    passPercent: assessment.pass_percent,
    maxAttempts: assessment.max_attempts,
  });
  const [owner, setOwner] = useState(assessment.ownerId ?? owners[0]?.id ?? ""),
    [required, setRequired] = useState(
      assessment.required ?? assessment.kind === "OFFICIAL",
    );
  return (
    <>
      <ActionNotice {...action} />
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          void action.run(
            () => quizApi.editAssessment(assessment.id, form),
            "Assessment settings saved.",
          );
        }}
      >
        <Input
          label="Title"
          required
          maxLength={180}
          disabled={!editable || action.busy}
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
        />
        <div className="sporg-fields">
          <Input
            label="Duration (seconds)"
            type="number"
            min={60}
            max={14400}
            required
            disabled={!editable || action.busy}
            value={form.durationSeconds}
            onChange={(e) =>
              setForm({ ...form, durationSeconds: Number(e.target.value) })
            }
          />
          <Input
            label="Pass score (%)"
            type="number"
            min={1}
            max={100}
            required
            disabled={!editable || action.busy}
            value={form.passPercent}
            onChange={(e) =>
              setForm({ ...form, passPercent: Number(e.target.value) })
            }
          />
          <Input
            label="Maximum attempts"
            type="number"
            min={1}
            max={20}
            required
            disabled={!editable || action.busy}
            value={form.maxAttempts}
            onChange={(e) =>
              setForm({ ...form, maxAttempts: Number(e.target.value) })
            }
          />
        </div>
        {editable && (
          <Button variant="outline" disabled={action.busy} type="submit">
            Save settings
          </Button>
        )}
      </form>
      <h3>Location and completion</h3>
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault();
          const o = owners.find((x) => x.id === owner);
          if (o)
            void action.run(
              () =>
                quizApi.owner(assessment.id, {
                  scope: o.scope,
                  ownerId: o.id,
                  required,
                }),
              "Location saved.",
            );
        }}
      >
        <label>
          Location
          <select
            value={owner}
            disabled={!editable || action.busy}
            onChange={(e) => setOwner(e.target.value)}
          >
            {owners.map((o) => (
              <option key={o.id} value={o.id}>
                {o.label}
              </option>
            ))}
          </select>
        </label>
        <label className="sporg-checkbox">
          <input
            type="checkbox"
            checked={required}
            disabled={
              !editable || action.busy || assessment.kind !== "OFFICIAL"
            }
            onChange={(e) => setRequired(e.target.checked)}
          />{" "}
          Required for completion
        </label>
        {editable && (
          <Button variant="outline" type="submit" disabled={action.busy}>
            Save location
          </Button>
        )}
      </form>
      <h3>Assessment questions</h3>
      <RemoteState {...links} retry={links.reload} />
      {links.data?.map((q) => (
        <div className="sporg-row" key={q.question_version_id}>
          <div>
            <strong>
              {q.position}. {q.stem}
            </strong>
            <small>{q.points} points</small>
          </div>
          {editable && (
            <Button
              variant="ghost"
              disabled={action.busy}
              onClick={() =>
                void action.run(
                  () => quizApi.detach(assessment.id, q.question_version_id),
                  "Question detached.",
                )
              }
            >
              Remove
            </Button>
          )}
        </div>
      ))}
      {links.data?.length === 0 && <p>No questions attached yet.</p>}
      {editable && (
        <>
          <RemoteState {...banks} retry={banks.reload} />
          <form
            className="sporg-form"
            onSubmit={(e) => {
              e.preventDefault();
              if (links.loading || links.error || !links.data) return;
              void action.run(async () => {
                const position =
                  Math.max(0, ...links.data.map((q) => q.position)) + 1;
                await quizApi.attach(assessment.id, question, position, points);
                setQuestion("");
              }, "Question attached.");
            }}
          >
            <label>
              Question bank
              <select
                required
                value={bank}
                onChange={(e) => {
                  setBank(e.target.value);
                  setQuestion("");
                }}
              >
                <option value="">Choose bank</option>
                {banks.data?.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.title}
                  </option>
                ))}
              </select>
            </label>
            <RemoteState
              loading={Boolean(bank) && questions.loading}
              error={questions.error}
              retry={questions.reload}
            />
            <label>
              Question version
              <select
                required
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
              >
                <option value="">Choose question</option>
                {questions.data
                  ?.filter(
                    (q) =>
                      !links.data?.some(
                        (l) => l.question_version_id === q.version_id,
                      ),
                  )
                  .map((q) => (
                    <option key={q.version_id} value={q.version_id}>
                      {q.stem} · v{q.version_no}
                    </option>
                  ))}
              </select>
            </label>
            <Input
              label="Points"
              type="number"
              required
              min={1}
              max={100}
              value={points}
              onChange={(e) => setPoints(Number(e.target.value))}
            />
            <Button
              type="submit"
              disabled={
                action.busy ||
                !question ||
                links.loading ||
                Boolean(links.error)
              }
            >
              Attach question
            </Button>
          </form>
          <div className="sporg-actions">
            <Button
              disabled={
                action.busy || !links.data?.length || Boolean(links.error)
              }
              onClick={() => {
                if (
                  window.confirm(
                    "Publish this assessment? Questions and settings become immutable.",
                  )
                )
                  void action.run(async () => {
                    await quizApi.publish(assessment.id);
                    refresh();
                  }, "Assessment published.");
              }}
            >
              Publish assessment
            </Button>
          </div>
        </>
      )}
    </>
  );
}
