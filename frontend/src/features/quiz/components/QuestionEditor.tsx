import { useCallback, useState, useEffect } from "react";
import { useAction, useRemote } from "@/shared/hooks/useRemote";
import { ActionNotice, RemoteState } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
import Input from "@/shared/ui/Input";
import { quizApi } from "../api/quizApi";
export function QuestionEditor({
  bankId,
  questionId,
  saved,
  onBusyChange,
}: {
  onBusyChange?: (busy: boolean) => void;
  bankId: string;
  questionId?: string;
  saved: () => void;
}) {
  const remote = useRemote(
    useCallback(
      () => (questionId ? quizApi.question(questionId) : Promise.resolve(null)),
      [questionId],
    ),
  );
  if (questionId && (remote.loading || remote.error))
    return <RemoteState {...remote} retry={remote.reload} />;
  if (questionId && !remote.data) return null;
  return (
    <QuestionForm
      key={questionId ?? "new"}
      bankId={bankId}
      questionId={questionId}
      saved={saved}
      onBusyChange={onBusyChange}
      stem={remote.data?.version.stem ?? ""}
      options={
        remote.data?.options.map((o) => ({
          body: o.body,
          correct: Boolean(o.correct),
        })) ?? [
          { body: "", correct: true },
          { body: "", correct: false },
          { body: "", correct: false },
          { body: "", correct: false },
        ]
      }
    />
  );
}
function QuestionForm({
  bankId,
  questionId,
  saved,
  onBusyChange,
  stem: initialStem,
  options: initialOptions,
}: {
  onBusyChange?: (busy: boolean) => void;
  bankId: string;
  questionId?: string;
  saved: () => void;
  stem: string;
  options: { body: string; correct: boolean }[];
}) {
  const [stem, setStem] = useState(initialStem),
    [options, setOptions] = useState(initialOptions);
  const action = useAction();
  useEffect(() => {
    onBusyChange?.(action.busy);
    return () => onBusyChange?.(false);
  }, [action.busy, onBusyChange]);
  const valid =
    options.filter((o) => o.body.trim()).length >= 2 &&
    options.filter((o) => o.correct && o.body.trim()).length === 1;
  return (
    <form
      className="sporg-form"
      onSubmit={(e) => {
        e.preventDefault();
        if (!valid) return;
        void action.run(async () => {
          const input = {
            stem: stem.trim(),
            options: options
              .filter((o) => o.body.trim())
              .map((o) => ({ ...o, body: o.body.trim() })),
          };
          if (questionId) await quizApi.reviseQuestion(questionId, input);
          else await quizApi.addQuestion(bankId, input);
          saved();
        }, "Question saved.");
      }}
    >
      <label>
        Question prompt
        <textarea
          required
          maxLength={2000}
          value={stem}
          onChange={(e) => setStem(e.target.value)}
        />
      </label>
      <fieldset disabled={action.busy}>
        <legend>Options · choose exactly one correct answer</legend>
        {options.map((o, i) => (
          <div className="sporg-option" key={i}>
            <input
              type="radio"
              name="correct-option"
              checked={o.correct}
              aria-label={`Option ${i + 1} is correct`}
              onChange={() =>
                setOptions(options.map((v, j) => ({ ...v, correct: j === i })))
              }
            />
            <Input
              label={`Option ${i + 1}`}
              required={i < 2}
              maxLength={1000}
              value={o.body}
              onChange={(e) =>
                setOptions(
                  options.map((v, j) =>
                    j === i ? { ...v, body: e.target.value } : v,
                  ),
                )
              }
            />
          </div>
        ))}
        <div className="sporg-actions">
          <Button
            variant="outline"
            disabled={options.length >= 8}
            onClick={() =>
              setOptions([...options, { body: "", correct: false }])
            }
          >
            Add option
          </Button>
          <Button
            variant="ghost"
            disabled={options.length <= 2}
            onClick={() => setOptions(options.slice(0, -1))}
          >
            Remove last option
          </Button>
        </div>
      </fieldset>
      <ActionNotice {...action} />
      <Button type="submit" disabled={action.busy || !valid}>
        {questionId ? "Save new revision" : "Save question"}
      </Button>
      {questionId && (
        <p>
          Existing assessments keep their attached question version. This
          creates a new revision.
        </p>
      )}
    </form>
  );
}
