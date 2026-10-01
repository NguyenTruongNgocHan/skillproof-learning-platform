import MediaPanel from "@/features/media/MediaPanel"
import { useEffect, useState } from "react"
import { Link, useNavigate, useParams } from "react-router-dom"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import {
  learningApi,
  type Course,
  type Resource,
} from "@/features/learning/learningApi"
import {
  quizApi,
  type Assessment,
  type AttemptHistory,
} from "@/features/quiz/quizApi"
export default function CoursePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [course, setCourse] = useState<Course | null>(null),
    [assessments, setAssessments] = useState<Assessment[]>([]),
    [history, setHistory] = useState<AttemptHistory[]>([]),
    [selected, setSelected] = useState<Resource | null>(null),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false)
  async function load() {
    if (!id) return
    setLoading(true)
    try {
      const [c, a, h] = await Promise.all([
        learningApi.course(id),
        quizApi.available(id),
        quizApi.history(id),
      ])
      setCourse(c)
      setAssessments(a)
      setHistory(h)
      setSelected(
        (previous) =>
          c.modules
            .flatMap((m) => m.resources)
            .find((r) => r.id === previous?.id) ??
          c.modules[0]?.resources[0] ??
          null,
      )
      setError("")
    } catch (e) {
      setError(
        e instanceof Error ? e.message : "Unable to load your enrollment",
      )
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [id])
  async function complete() {
    if (!id || !selected) return
    setBusy(true)
    try {
      await learningApi.complete(id, selected.id)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not save progress")
    } finally {
      setBusy(false)
    }
  }
  async function begin(a: Assessment) {
    if (!id) return
    setBusy(true)
    setError("")
    try {
      const view = await quizApi.start(a.id, id)
      navigate(`/app/attempts/${view.attempt.id}`)
    } catch (e) {
      setError(
        e instanceof Error ? e.message : "Unable to start this assessment",
      )
    } finally {
      setBusy(false)
    }
  }
  return (
    <AppShell>
      <div className="v7-wrap">
        <Link to="/app/learning" className="profile-back">
          ← Your paths
        </Link>
        {loading && !course ? (
          <p role="status">Loading lessons…</p>
        ) : error && !course ? (
          <div role="alert" className="org-error">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </div>
        ) : (
          course && (
            <>
              <header className="v7-hero">
                <span className="org-eyebrow">
                  VERSION {course.enrollment.version_no} ·{" "}
                  {course.enrollment.status}
                </span>
                <h1>{course.enrollment.title}</h1>
                <p>{course.enrollment.summary}</p>
                <div
                  className="v7-progress"
                  role="progressbar"
                  aria-valuenow={course.progress.completedResources}
                  aria-valuemin={0}
                  aria-valuemax={course.progress.totalResources}
                >
                  <span
                    style={{
                      width: `${
                        course.progress.totalResources
                          ? (100 * course.progress.completedResources) /
                            course.progress.totalResources
                          : 0
                      }%`,
                    }}
                  />
                </div>
                <p>
                  {course.progress.completedResources} /{" "}
                  {course.progress.totalResources} resources ·{" "}
                  {course.progress.passedAssessments} /{" "}
                  {course.progress.officialAssessments} official assessments
                  passed
                </p>
              </header>
              {error && (
                <div role="alert" className="org-error">
                  {error}{" "}
                  <Button variant="outline" onClick={() => void load()}>
                    Retry
                  </Button>
                </div>
              )}
              <div className="v7-course">
                <nav className="v7-card" aria-label="Course modules">
                  <h2>Learning journey</h2>
                  {course.modules.map((m) => (
                    <div key={m.id}>
                      <h3>
                        {m.position}. {m.title}
                      </h3>
                      {m.resources.map((r) => (
                        <button
                          className={`v7-row ${
                            selected?.id === r.id ? "v7-row-active" : ""
                          }`}
                          key={r.id}
                          onClick={() => setSelected(r)}
                        >
                          {r.completed ? "✓ " : "○ "}
                          {r.title}
                        </button>
                      ))}
                    </div>
                  ))}
                </nav>
                <section className="v7-card" aria-live="polite">
                  <span className="org-eyebrow">LEARNING RESOURCE</span>
                  {selected ? (
                    <>
                      <h2>{selected.title}</h2>
                      {selected.kind === "ARTICLE" ? (
                        <div className="v7-article">{selected.body}</div>
                      ) : selected.kind === "FILE" ||
                        selected.kind === "AUDIO" ? (
                        <p>Download the lesson attachment below.</p>
                      ) : (
                        <p>
                          Open the {selected.kind.toLowerCase()} resource in a
                          new tab:{" "}
                          <a
                            href={selected.url ?? "#"}
                            target="_blank"
                            rel="noreferrer"
                          >
                            {selected.title} ↗
                          </a>
                        </p>
                      )}
                      <MediaPanel
                        key={selected.id}
                        scope="RESOURCE"
                        target={selected.id}
                      />
                      <Button
                        disabled={busy || selected.completed}
                        onClick={() => void complete()}
                      >
                        {selected.completed
                          ? "Completed"
                          : "Mark this resource complete"}
                      </Button>
                    </>
                  ) : (
                    <p>No resources in this version.</p>
                  )}
                </section>
              </div>
              <section className="v7-section">
                <span className="org-eyebrow">CHECK YOUR UNDERSTANDING</span>
                <h2>Practice and assessments</h2>
                {assessments.length === 0 ? (
                  <p>No assessments published for this version.</p>
                ) : (
                  <div className="v7-grid">
                    {assessments.map((a) => (
                      <article className="v7-card" key={a.id}>
                        <span className="org-eyebrow">{a.kind}</span>
                        <h3>{a.title}</h3>
                        <p>
                          {Math.ceil(a.duration_seconds / 60)} minutes ·{" "}
                          {a.max_attempts} attempts · Pass at {a.pass_percent}%
                        </p>
                        <p>
                          {a.kind === "OFFICIAL"
                            ? "Passing counts toward completion."
                            : "Your score is practice feedback and does not count as an official assessment."}
                        </p>
                        <Button disabled={busy} onClick={() => void begin(a)}>
                          Start {a.kind.toLowerCase()}
                        </Button>
                      </article>
                    ))}
                  </div>
                )}
              </section>
              <section className="v7-section">
                <h2>Your attempt history</h2>
                {history.length === 0 ? (
                  <p>
                    No attempts yet. Your answers and results will appear here
                    after you begin.
                  </p>
                ) : (
                  <div className="v7-grid">
                    {history.map((h) => (
                      <article className="v7-card" key={h.id}>
                        <span className="org-eyebrow">
                          {h.kind} · {h.status}
                        </span>
                        <h3>{h.title}</h3>
                        <p>
                          {h.score_percent === null
                            ? "In progress"
                            : `Score ${h.score_percent}% · ${
                                h.passed ? "Passed" : "Not passed"
                              }`}
                        </p>
                        <Button asChild variant="outline">
                          <Link to={`/app/attempts/${h.id}`}>
                            {h.status === "SCORED"
                              ? "Review result"
                              : "Resume attempt"}
                          </Link>
                        </Button>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            </>
          )
        )}
      </div>
    </AppShell>
  )
}
