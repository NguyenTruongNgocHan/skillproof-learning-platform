import { useEffect, useState } from "react"
import { Link, useNavigate, useParams } from "react-router-dom"
import PublicHeader from "@/components/layout/PublicHeader"
import PublicFooter from "@/components/layout/PublicFooter"
import Button from "@/components/ui/Button"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { learningApi, type Path } from "@/features/learning/learningApi"
export default function PathDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [path, setPath] = useState<Path | null>(null),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false)
  async function load() {
    if (!id) return
    setLoading(true)
    try {
      setPath(await learningApi.detail(id))
      setError("")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Path unavailable")
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [id])
  async function enroll() {
    if (!id) return
    if (!user) {
      navigate(`/login?returnTo=${encodeURIComponent(`/learning-paths/${id}`)}`)
      return
    }
    if (user.role !== "LEARNER") {
      setError("Sign in with a Learner account to enroll.")
      return
    }
    setBusy(true)
    setError("")
    try {
      const result = await learningApi.enroll(id)
      navigate(`/app/learning/${result.id}`)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to enroll")
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="public-page">
      <PublicHeader />
      <main className="v7-wrap">
        <Link to="/learning-paths" className="profile-back">
          ← All paths
        </Link>
        {loading ? (
          <p role="status">Loading the path…</p>
        ) : error && !path ? (
          <div className="org-error" role="alert">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </div>
        ) : (
          path && (
            <div className="v7-detail">
              <span className="org-eyebrow">
                {path.organization_name} · Version {path.version_no}
              </span>
              <h1>{path.title}</h1>
              <p>{path.summary}</p>
              <div className="v7-card">
                <h2>Learn with a clear record of progress</h2>
                <p>
                  Enrollment creates your personal record against this published
                  version. You can review the modules and assessments once
                  enrolled. Completing practice alone does not count as an
                  official assessment.
                </p>
                {error && (
                  <p role="alert" className="org-error">
                    {error}
                  </p>
                )}
                <Button disabled={busy} onClick={() => void enroll()}>
                  {busy
                    ? "Enrolling…"
                    : user
                      ? "Enroll in this path"
                      : "Sign in to enroll"}
                </Button>
              </div>
            </div>
          )
        )}
      </main>
      <PublicFooter />
    </div>
  )
}
