import { useCallback, useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import LearnerShell from "@/components/layout/LearnerShell"
import Button from "@/components/ui/Button"
import { quizApi, type AttemptView, type Result } from "@/features/quiz/quizApi"
function isResult(value: AttemptView | Result): value is Result {
  return "scorePercent" in value
}
export default function AttemptPage() {
  const { id } = useParams()
  const [attempt, setAttempt] = useState<AttemptView | null>(null),
    [result, setResult] = useState<Result | null>(null),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false),
    [now, setNow] = useState(Date.now())
  const load = useCallback(async () => {
    if (!id) return
    setLoading(true)
    try {
      const data = await quizApi.resume(id)
      if (isResult(data)) {
        setResult(data)
        setAttempt(null)
      } else setAttempt(data)
      setError("")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to load this attempt")
    } finally {
      setLoading(false)
    }
  }, [id])
  useEffect(() => {
    void load()
  }, [load])
  useEffect(() => {
    if (!attempt) return
    const timer = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(timer)
  }, [attempt])
  const remaining = attempt
    ? Math.max(
        0,
        Math.ceil(
          (new Date(attempt.attempt.deadline_at).getTime() - now) / 1000,
        ),
      )
    : 0
  async function answer(questionVersionId: string, optionId: string) {
    if (!id) return
    setBusy(true)
    setError("")
    try {
      const data = await quizApi.answer(id, questionVersionId, optionId)
      if (isResult(data)) {
        setResult(data)
        setAttempt(null)
      } else setAttempt(data)
    } catch (e) {
      setError(
        e instanceof Error
          ? e.message
          : "Answer not saved. Check your connection and retry.",
      )
    } finally {
      setBusy(false)
    }
  }
  async function submit() {
    if (!id) return
    setBusy(true)
    setError("")
    try {
      setResult(await quizApi.submit(id))
      setAttempt(null)
    } catch (e) {
      setError(
        e instanceof Error
          ? e.message
          : "Could not submit; your saved answers remain available.",
      )
    } finally {
      setBusy(false)
    }
  }
  return (
    <LearnerShell>
      <div className="learner-page learner-page--attempt">
        <Link to="/app/learning" className="profile-back">
          ← Your paths
        </Link>
        {loading ? (
          <p role="status">Restoring your attempt…</p>
        ) : error && !attempt && !result ? (
          <p role="alert" className="org-error">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </p>
        ) : result ? (
          <div className="v7-card v7-result">
            
            <h1>
              {result.timedOut
                ? "Time ran out"
                : result.passed
                  ? "You passed"
                  : "Attempt completed"}
            </h1>
            <strong>{result.scorePercent}%</strong>
            <p>
              {result.timedOut
                ? "The server closed this attempt at its deadline. Saved answers were scored, but this attempt cannot count as passed."
                : result.kind === "OFFICIAL"
                  ? "This result is linked to your enrolled learning path version."
                  : "Practice and mock scores do not substitute for an official assessment."}
            </p>
            <Button asChild>
              <Link to="/app/learning">Return to learning</Link>
            </Button>
          </div>
        ) : (
          attempt && (
            <>
              <header className="v7-hero">
                <span className="course-hero__context">{attempt.attempt.kind === "OFFICIAL" ? "Official assessment" : attempt.attempt.kind === "MOCK" ? "Mock test" : "Practice"} · In progress</span>
                <h1>{attempt.attempt.title}</h1>
                <p role="timer">
                  Time remaining: {Math.floor(remaining / 60)}:
                  {String(remaining % 60).padStart(2, "0")} · Saved answers
                  remain available after refresh.
                </p>
                <p>
                  The server determines the deadline. Submit once you are ready.
                </p>
              </header>
              {error && (
                <p className="org-error" role="alert">
                  {error}{" "}
                  <Button variant="outline" onClick={() => void load()}>
                    Reload saved answers
                  </Button>
                </p>
              )}
              <div className="v7-stack">
                {attempt.questions.map((q, i) => (
                  <fieldset className="v7-card" key={q.question_version_id}>
                    <legend>
                      <b>
                        {i + 1}. {q.stem}
                      </b>{" "}
                      <small>({q.points} points)</small>
                    </legend>
                    {q.options.map((o) => (
                      <label className="v7-option" key={o.id}>
                        <input
                          type="radio"
                          name={q.question_version_id}
                          checked={q.selected_option_id === o.id}
                          disabled={busy || remaining === 0}
                          onChange={() =>
                            void answer(q.question_version_id, o.id)
                          }
                        />
                        {o.body}
                      </label>
                    ))}
                  </fieldset>
                ))}
              </div>
              <div className="v7-submit">
                <span>
                  {attempt.questions.filter((q) => q.selected_option_id).length}{" "}
                  / {attempt.questions.length} answered
                </span>
                <Button disabled={busy} onClick={() => void submit()}>
                  {busy
                    ? "Saving…"
                    : remaining === 0
                      ? "Finalize expired attempt"
                      : "Submit answers"}
                </Button>
              </div>
            </>
          )
        )}
      </div>
    </LearnerShell>
  )
}
