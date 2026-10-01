import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import { learningApi, type Enrollment } from "@/features/learning/learningApi"
export default function MyLearningPage() {
  const [items, setItems] = useState<Enrollment[]>([]),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true)
  async function load() {
    setLoading(true)
    try {
      setItems(await learningApi.mine())
      setError("")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not load enrollments")
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [])
  return (
    <AppShell>
      <div className="v7-wrap">
        <header className="v7-hero">
          <span className="org-eyebrow">YOUR LEARNING</span>
          <h1>Your paths, at your pace.</h1>
          <p>
            Pick up exactly where you left off. Each enrollment keeps its
            original published version.
          </p>
        </header>
        {loading ? (
          <p role="status">Loading your learning…</p>
        ) : error ? (
          <p role="alert" className="org-error">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </p>
        ) : items.length === 0 ? (
          <div className="v7-card">
            <h2>Your next step starts here</h2>
            <p>You have not enrolled in a path yet.</p>
            <Button asChild>
              <Link to="/learning-paths">Explore paths</Link>
            </Button>
          </div>
        ) : (
          <div className="v7-grid">
            {items.map((item) => (
              <article className="v7-card" key={item.id}>
                <span className="org-eyebrow">
                  {item.status} · Version {item.version_no}
                </span>
                <h2>{item.title}</h2>
                <p>{item.summary}</p>
                <Button asChild>
                  <Link to={`/app/learning/${item.id}`}>
                    {item.status === "COMPLETED"
                      ? "Review your learning"
                      : "Continue learning"}
                  </Link>
                </Button>
              </article>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
