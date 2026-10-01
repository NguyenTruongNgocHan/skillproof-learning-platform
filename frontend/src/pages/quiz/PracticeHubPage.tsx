import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import PublicHeader from "@/components/layout/PublicHeader"
import PublicFooter from "@/components/layout/PublicFooter"
import Button from "@/components/ui/Button"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { learningApi, type Enrollment } from "@/features/learning/learningApi"
export default function PracticeHubPage() {
  const { user } = useAuth()
  const [items, setItems] = useState<Enrollment[]>([]),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(false)
  useEffect(() => {
    if (user?.role !== "LEARNER") return
    setLoading(true)
    learningApi
      .mine()
      .then((data) => {
        setItems(data)
        setError("")
      })
      .catch((e) =>
        setError(
          e instanceof Error ? e.message : "Unable to load your learning",
        ),
      )
      .finally(() => setLoading(false))
  }, [user?.id, user?.role])
  return (
    <div className="public-page">
      <PublicHeader />
      <main className="v7-wrap">
        <header className="v7-hero">
          <span className="org-eyebrow">LEARN BY DOING</span>
          <h1>Practice with a purpose.</h1>
          <p>
            Practice quizzes and mock tests give feedback. Official assessments
            belong to a specific published learning path version and have
            different completion rules.
          </p>
        </header>
        {user?.role === "LEARNER" ? (
          loading ? (
            <p role="status">Loading your available paths…</p>
          ) : error ? (
            <p role="alert" className="org-error">
              {error}
            </p>
          ) : items.length ? (
            <div className="v7-grid">
              {items.map((e) => (
                <article className="v7-card" key={e.id}>
                  <h2>{e.title}</h2>
                  <p>
                    Version {e.version_no} · Open your path to see available
                    assessments and your attempt limits.
                  </p>
                  <Button asChild>
                    <Link to={`/app/learning/${e.id}`}>View assessments</Link>
                  </Button>
                </article>
              ))}
            </div>
          ) : (
            <div className="v7-card">
              <h2>Choose a path to begin</h2>
              <p>
                Practice becomes available inside a learning path after
                enrollment.
              </p>
              <Button asChild>
                <Link to="/learning-paths">Explore paths</Link>
              </Button>
            </div>
          )
        ) : (
          <div className="v7-card">
            <h2>Your practice begins in a path</h2>
            <p>
              Explore published paths to see what you can study, then sign in as
              a Learner to enroll.
            </p>
            <Button asChild>
              <Link to="/learning-paths">Explore paths</Link>
            </Button>
          </div>
        )}
      </main>
      <PublicFooter />
    </div>
  )
}
