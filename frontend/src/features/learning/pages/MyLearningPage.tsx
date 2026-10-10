import { ArrowRight, BookOpen, CheckCircle2, Clock3, Compass } from "lucide-react"
import { useEffect, useMemo, useState } from "react"
import { Link } from "react-router-dom"

import { learningApi } from "@/features/learning/api/learningApi"
import type { Enrollment } from "@/features/learning/types/learning.types"
import LearnerShell from "@/shared/components/layout/LearnerShell"
import Button from "@/shared/ui/Button"

export default function MyLearningPage() {
  const [items, setItems] = useState<Enrollment[]>([])
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)

  async function load() {
    setLoading(true)
    try {
      setItems(await learningApi.mine())
      setError("")
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "We couldn't load your learning right now.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const active = useMemo(() => items.filter((item) => item.status === "ACTIVE"), [items])
  const completed = useMemo(() => items.filter((item) => item.status === "COMPLETED"), [items])

  return (
    <LearnerShell>
      <main className="learner-page learner-page--learning">
        <header className="learner-page__hero">
          <div>
            <p className="learner-page__intro">Your learning</p>
            <h1>Keep moving forward.</h1>
            <p>
              Return to what you are learning now, or revisit something you have already completed.
            </p>
          </div>
          <Link to="/learning-paths" className="learner-page__hero-action">
            <Compass size={18} /> Discover something new
          </Link>
        </header>

        {loading ? (
          <div className="learner-state" role="status">
            <span className="learner-state__pulse" />
            Loading your learning…
          </div>
        ) : error ? (
          <div className="learner-state learner-state--error" role="alert">
            <strong>We couldn't open your learning.</strong>
            <span>{error}</span>
            <Button variant="outline" onClick={() => void load()}>
              Try again
            </Button>
          </div>
        ) : items.length === 0 ? (
          <section className="learner-empty learner-empty--wide">
            <span className="learner-empty__icon">
              <BookOpen size={24} />
            </span>
            <h2>Your first path is waiting.</h2>
            <p>Explore published learning paths and choose one that feels worth your time.</p>
            <Button asChild>
              <Link to="/learning-paths">
                Explore learning paths <ArrowRight size={16} />
              </Link>
            </Button>
          </section>
        ) : (
          <>
            <section className="learner-content-section">
              <div className="learner-content-section__heading">
                <div>
                  <h2>In progress</h2>
                  <p>
                    {active.length ? "Pick up where you left off." : "Nothing active right now."}
                  </p>
                </div>
                <span>{active.length}</span>
              </div>
              {active.length ? (
                <div className="learning-list">
                  {active.map((item, index) => (
                    <Link className="learning-row" to={`/app/learning/${item.id}`} key={item.id}>
                      <span className="learning-row__index">
                        {String(index + 1).padStart(2, "0")}
                      </span>
                      <span className="learning-row__body">
                        <strong>{item.title}</strong>
                        <small>{item.summary || "Continue your learning journey."}</small>
                      </span>
                      <span className="learning-row__meta">
                        <Clock3 size={14} /> Version {item.version_no}
                      </span>
                      <ArrowRight className="learning-row__arrow" size={18} />
                    </Link>
                  ))}
                </div>
              ) : null}
            </section>

            {completed.length ? (
              <section className="learner-content-section learner-content-section--quiet">
                <div className="learner-content-section__heading">
                  <div>
                    <h2>Completed</h2>
                    <p>Learning you can return to whenever you need it.</p>
                  </div>
                  <span>{completed.length}</span>
                </div>
                <div className="learning-completed-grid">
                  {completed.map((item) => (
                    <Link
                      className="learning-completed"
                      to={`/app/learning/${item.id}`}
                      key={item.id}
                    >
                      <CheckCircle2 size={20} />
                      <span>
                        <strong>{item.title}</strong>
                        <small>Review learning · Version {item.version_no}</small>
                      </span>
                      <ArrowRight size={16} />
                    </Link>
                  ))}
                </div>
              </section>
            ) : null}
          </>
        )}
      </main>
    </LearnerShell>
  )
}
