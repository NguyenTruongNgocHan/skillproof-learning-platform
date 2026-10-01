import { Link } from "react-router-dom"
import MediaPanel from "@/features/media/MediaPanel"
import Button from "@/components/ui/Button"
import { learningApi, type Resource } from "@/features/learning/learningApi"
import { quizApi, type Kind } from "@/features/quiz/quizApi"
import { useStudioState } from "@/features/learning/PathStudioContext"

export default function AssessmentSection() {
  const {
    id,
    version,
    assessments,
    assessmentItems,
    banks,
    bank,
    questions,
    busy,
    assessmentTitle,
    editingAssessment,
    points,
    kind,
    duration,
    pass,
    max,
    attachAssessment,
    attachQuestion,
    act,
    addAssessment,
    attach,
    setBank,
    setAssessmentTitle,
    setEditingAssessment,
    setPoints,
    setKind,
    setDuration,
    setPass,
    setMax,
    setAttachAssessment,
    setAttachQuestion,
  } = useStudioState()
  if (!version) return null
  const draft = version.status === "DRAFT"
  return (
    <section className="v7-section">
      <h2>2. Assess what learners understood</h2>
      <p>
        Practice and mock scores provide feedback; only official assessment
        results can satisfy the official completion requirement.
      </p>
      {draft && (
        <form
          className="v7-form v7-card"
          onSubmit={(e) => void addAssessment(e)}
        >
          <h3>
            {editingAssessment
              ? "Edit assessment draft"
              : "Create assessment draft"}
          </h3>
          <label>
            Assessment name
            <input
              required
              maxLength={180}
              value={assessmentTitle}
              onChange={(e) => setAssessmentTitle(e.target.value)}
            />
          </label>
          <label>
            Purpose
            <select
              disabled={!!editingAssessment}
              value={kind}
              onChange={(e) => setKind(e.target.value as Kind)}
            >
              <option value="PRACTICE">Practice quiz</option>
              <option value="MOCK">Mock test</option>
              <option value="OFFICIAL">Official assessment</option>
            </select>
          </label>
          <div className="v7-inline">
            <label>
              Time (minutes)
              <input
                type="number"
                min={1}
                max={240}
                value={duration / 60}
                onChange={(e) => setDuration(Number(e.target.value) * 60)}
              />
            </label>
            <label>
              Passing score (%)
              <input
                type="number"
                min={1}
                max={100}
                value={pass}
                onChange={(e) => setPass(Number(e.target.value))}
              />
            </label>
            <label>
              Max attempts
              <input
                type="number"
                min={1}
                max={20}
                value={max}
                onChange={(e) => setMax(Number(e.target.value))}
              />
            </label>
          </div>
          <Button type="submit" disabled={busy}>
            {editingAssessment
              ? "Save draft policy"
              : "Create assessment draft"}
          </Button>
          {editingAssessment && (
            <Button
              variant="ghost"
              onClick={() => {
                setEditingAssessment(null)
                setAssessmentTitle("")
              }}
            >
              Cancel edit
            </Button>
          )}
        </form>
      )}
      {assessments.length === 0 ? (
        <p>No assessments yet.</p>
      ) : (
        assessments.map((a) => (
          <article className="v7-card" key={a.id}>
            <span className="org-eyebrow">
              {a.kind} · {a.status}
            </span>
            <h3>{a.title}</h3>
            <p>
              {Math.ceil(a.duration_seconds / 60)} min · {a.pass_percent}% to
              pass · {a.max_attempts} attempts
            </p>
            {assessmentItems[a.id]?.length > 0 && (
              <ol>
                {assessmentItems[a.id].map((q) => (
                  <li key={q.question_version_id}>
                    {q.stem} · {q.points} point
                    {q.points === 1 ? "" : "s"}{" "}
                    {draft && a.status === "DRAFT" && (
                      <Button
                        variant="ghost"
                        disabled={busy}
                        onClick={() =>
                          void act(
                            () => quizApi.detach(a.id, q.question_version_id),
                            "Question removed.",
                          )
                        }
                      >
                        Remove
                      </Button>
                    )}
                  </li>
                ))}
              </ol>
            )}
            {draft && a.status === "DRAFT" && (
              <div className="v7-inline">
                <Button
                  variant="outline"
                  disabled={busy}
                  onClick={() => {
                    setEditingAssessment(a)
                    setAssessmentTitle(a.title)
                    setKind(a.kind)
                    setDuration(a.duration_seconds)
                    setPass(a.pass_percent)
                    setMax(a.max_attempts)
                  }}
                >
                  Edit policy
                </Button>
                <Button
                  disabled={busy}
                  variant="outline"
                  onClick={() =>
                    void act(
                      () => quizApi.publish(a.id),
                      "Assessment published within this draft version.",
                    )
                  }
                >
                  Publish assessment
                </Button>
                <Button
                  disabled={busy}
                  variant="ghost"
                  onClick={() => {
                    if (window.confirm(`Delete assessment draft ${a.title}?`))
                      void act(
                        () => quizApi.deleteAssessment(a.id),
                        "Assessment draft deleted.",
                      )
                  }}
                >
                  Delete draft
                </Button>
              </div>
            )}
          </article>
        ))
      )}
      {draft && assessments.some((a) => a.status === "DRAFT") && (
        <form className="v7-form v7-card" onSubmit={(e) => void attach(e)}>
          <h3>Add a question to an assessment draft</h3>
          <p>
            Questions are selected from this organization’s bank. The selected
            question version is preserved.
          </p>
          <label>
            Assessment
            <select
              required
              value={attachAssessment}
              onChange={(e) => setAttachAssessment(e.target.value)}
            >
              <option value="">Choose draft</option>
              {assessments
                .filter((a) => a.status === "DRAFT")
                .map((a) => (
                  <option value={a.id} key={a.id}>
                    {a.title}
                  </option>
                ))}
            </select>
          </label>
          <label>
            Question bank
            <select value={bank} onChange={(e) => setBank(e.target.value)}>
              <option value="">Choose bank</option>
              {banks.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.title}
                </option>
              ))}
            </select>
          </label>
          <label>
            Question version
            <select
              required
              value={attachQuestion}
              onChange={(e) => setAttachQuestion(e.target.value)}
            >
              <option value="">Choose question</option>
              {questions.map((q) => (
                <option key={q.version_id} value={q.version_id}>
                  {q.stem} · v{q.version_no}
                </option>
              ))}
            </select>
          </label>
          <label>
            Points for this question
            <input
              type="number"
              min={1}
              max={100}
              value={points}
              onChange={(e) => setPoints(Number(e.target.value))}
            />
          </label>
          <Button disabled={busy} type="submit">
            Attach question
          </Button>
          <Button asChild variant="outline">
            <Link to="/organizer/questions">Manage question banks</Link>
          </Button>
        </form>
      )}
    </section>
  )
}
