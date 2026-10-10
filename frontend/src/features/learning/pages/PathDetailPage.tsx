import { ArrowLeft, ArrowRight, BookOpen, CheckCircle2, ShieldCheck } from "lucide-react"
import { useCallback, useEffect, useState } from "react"
import { Link, useNavigate, useParams } from "react-router-dom"

import { useAuth } from "@/features/auth/hooks/useAuth"
import { learningApi } from "@/features/learning/api/learningApi"
import type { Path } from "@/features/learning/types/learning.types"
import LearnerSurface from "@/shared/components/layout/LearnerSurface"
import Button from "@/shared/ui/Button"

export default function PathDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [path, setPath] = useState<Path | null>(null)
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    if (!id) return
    setLoading(true)
    try {
      setPath(await learningApi.detail(id))
      setError("")
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "This learning path is unavailable.")
    } finally {
      setLoading(false)
    }
  }, [id])
  useEffect(() => {
    void load()
  }, [load])

  async function enroll() {
    if (!id) return
    if (!user) {
      navigate(`/login?returnTo=${encodeURIComponent(`/learning-paths/${id}`)}`)
      return
    }
    if (user.role !== "LEARNER") {
      setError("Use a Learner account to join this learning path.")
      return
    }
    setBusy(true)
    setError("")
    try {
      const result = await learningApi.enroll(id)
      navigate(`/app/learning/${result.id}`)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "We couldn't enroll you right now.")
    } finally {
      setBusy(false)
    }
  }

  return (
    <LearnerSurface>
      <main className="learner-page learner-page--path-detail">
        <Link to="/learning-paths" className="learner-back">
          <ArrowLeft size={16} /> Explore learning
        </Link>
        {loading ? (
          <div className="learner-state" role="status">
            Opening this learning path…
          </div>
        ) : error && !path ? (
          <div className="learner-state learner-state--error" role="alert">
            <strong>This path couldn't be opened.</strong>
            <span>{error}</span>
            <Button variant="outline" onClick={() => void load()}>
              Try again
            </Button>
          </div>
        ) : path ? (
          <>
            <section className="path-detail-hero">
              <div className="path-detail-hero__copy">
                <span className="path-detail-hero__org">
                  {path.organization_name || "SkillProof learning"}
                </span>
                <h1>{path.title}</h1>
                <p>
                  {path.summary ||
                    "A structured learning path designed to turn progress into evidence."}
                </p>
                <div className="path-detail-hero__facts">
                  <span>
                    <BookOpen size={16} /> Structured learning
                  </span>
                  <span>
                    <CheckCircle2 size={16} /> Progress tracked
                  </span>
                  <span>
                    <ShieldCheck size={16} /> Version {path.version_no ?? "published"}
                  </span>
                </div>
                {error ? (
                  <p className="learner-inline-error" role="alert">
                    {error}
                  </p>
                ) : null}
                <Button size="lg" disabled={busy} onClick={() => void enroll()}>
                  {busy ? "Joining…" : user ? "Start this path" : "Sign in to start"}
                  <ArrowRight size={17} />
                </Button>
              </div>
              <div className="path-detail-hero__visual" aria-hidden="true">
                <span>YOUR JOURNEY</span>
                <div>
                  <i>01</i>
                  <strong>Learn</strong>
                </div>
                <div>
                  <i>02</i>
                  <strong>Practice</strong>
                </div>
                <div>
                  <i>03</i>
                  <strong>Prove</strong>
                </div>
              </div>
            </section>
            <section className="path-detail-story">
              <div>
                <h2>A clear record of what you actually did.</h2>
                <p>
                  When you enroll, SkillProof ties your progress to the published version you
                  joined. Learning resources and official assessments remain traceable to that
                  version.
                </p>
              </div>
              <div className="path-detail-story__note">
                <ShieldCheck size={22} />
                <strong>Practice helps you improve.</strong>
                <p>Official completion still follows the path's assessment and completion rules.</p>
              </div>
            </section>
          </>
        ) : null}
      </main>
    </LearnerSurface>
  )
}
