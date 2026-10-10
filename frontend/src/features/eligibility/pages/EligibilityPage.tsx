import {
  ArrowRight,
  BadgeCheck,
  BookOpen,
  CheckCircle2,
  CircleDashed,
  ShieldCheck,
} from "lucide-react"
import { useEffect, useState } from "react"
import { Link } from "react-router-dom"

import { eligibilityApi } from "@/features/eligibility/api/eligibilityApi"
import type { CompletionEvidence } from "@/features/eligibility/types/eligibility.types"
import { learningApi } from "@/features/learning/api/learningApi"
import type { Enrollment } from "@/features/learning/types/learning.types"
import LearnerShell from "@/shared/components/layout/LearnerShell"
import Button from "@/shared/ui/Button"

export default function EligibilityPage() {
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [evidence, setEvidence] = useState<Record<string, CompletionEvidence>>({})
  const [busyId, setBusyId] = useState<string | null>(null)
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    learningApi
      .mine()
      .then(setEnrollments)
      .catch((cause) =>
        setError(
          cause instanceof Error ? cause.message : "We couldn't load your learning evidence.",
        ),
      )
      .finally(() => setLoading(false))
  }, [])

  async function evaluate(enrollmentId: string) {
    setBusyId(enrollmentId)
    setError("")
    try {
      const result = await eligibilityApi.evaluateCompletion(enrollmentId)
      setEvidence((current) => ({ ...current, [enrollmentId]: result }))
    } catch (cause) {
      setError(
        cause instanceof Error
          ? cause.message
          : "We couldn't refresh completion evidence right now.",
      )
    } finally {
      setBusyId(null)
    }
  }

  return (
    <LearnerShell>
      <main className="learner-page learner-page--achievements">
        <header className="achievement-hero">
          <div>
            <span>
              <ShieldCheck size={16} /> Evidence, not decoration
            </span>
            <h1>
              See what your progress
              <br />
              can <em>prove.</em>
            </h1>
            <p>
              SkillProof keeps learning progress, official assessment results, and certificate
              eligibility separate so each achievement has a clear basis.
            </p>
          </div>
          <div className="achievement-hero__seal" aria-hidden="true">
            <BadgeCheck size={42} />
            <strong>SkillProof</strong>
            <span>Trusted learning evidence</span>
          </div>
        </header>

        {error ? (
          <p className="learner-inline-error" role="alert">
            {error}
          </p>
        ) : null}
        <section className="learner-content-section">
          <div className="learner-content-section__heading">
            <div>
              <h2>Your completion evidence</h2>
              <p>
                Refresh a path to see whether its learning and official assessment requirements are
                complete.
              </p>
            </div>
          </div>
          {loading ? (
            <div className="learner-state" role="status">
              Loading your learning evidence…
            </div>
          ) : enrollments.length === 0 ? (
            <div className="learner-empty">
              <BookOpen size={24} />
              <h3>There is nothing to evaluate yet.</h3>
              <p>
                Join a learning path first. Your completion evidence will grow from real learning
                activity.
              </p>
              <Button asChild>
                <Link to="/learning-paths">
                  Explore learning <ArrowRight size={16} />
                </Link>
              </Button>
            </div>
          ) : (
            <div className="achievement-list">
              {enrollments.map((item) => {
                const result = evidence[item.id]
                const resourcePercent =
                  result && result.totalResources
                    ? Math.round((result.completedResources / result.totalResources) * 100)
                    : null
                return (
                  <article className="achievement-row" key={item.id}>
                    <div className="achievement-row__top">
                      <span
                        className={`achievement-row__status${
                          result?.completed ? " is-complete" : ""
                        }`}
                      >
                        {result?.completed ? (
                          <CheckCircle2 size={18} />
                        ) : (
                          <CircleDashed size={18} />
                        )}
                      </span>
                      <div>
                        <strong>{item.title}</strong>
                        <small>Published version {item.version_no}</small>
                      </div>
                      <Button
                        variant="outline"
                        disabled={busyId === item.id}
                        onClick={() => void evaluate(item.id)}
                      >
                        {busyId === item.id ? "Checking…" : result ? "Refresh" : "Check progress"}
                      </Button>
                    </div>
                    {result ? (
                      <div className="achievement-row__evidence">
                        <div>
                          <span>Learning resources</span>
                          <strong>
                            {result.completedResources} / {result.totalResources}
                          </strong>
                          <i>
                            <b style={{ width: `${resourcePercent ?? 0}%` }} />
                          </i>
                        </div>
                        <div>
                          <span>Official assessments</span>
                          <strong>
                            {result.passedAssessments} / {result.requiredAssessments}
                          </strong>
                        </div>
                        <div className={result.completed ? "is-complete" : ""}>
                          <span>Completion</span>
                          <strong>
                            {result.completed ? "Requirements met" : "Still in progress"}
                          </strong>
                        </div>
                      </div>
                    ) : (
                      <p className="achievement-row__hint">
                        Check progress when you want a current, server-evaluated view of this path.
                      </p>
                    )}
                  </article>
                )
              })}
            </div>
          )}
        </section>
        <section className="achievement-explainer">
          <ShieldCheck size={22} />
          <div>
            <h2>Completion is not the same as a certificate.</h2>
            <p>
              Meeting a path's completion rules can make you eligible for a certification program
              when one applies. Certificate issuance remains an organization-controlled step.
            </p>
          </div>
        </section>
      </main>
    </LearnerShell>
  )
}
