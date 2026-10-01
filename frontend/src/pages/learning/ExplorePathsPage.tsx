import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import PublicHeader from "@/components/layout/PublicHeader"
import PublicFooter from "@/components/layout/PublicFooter"
import Button from "@/components/ui/Button"
import { learningApi, type Path } from "@/features/learning/learningApi"
export default function ExplorePathsPage() {
  const [paths, setPaths] = useState<Path[]>([]),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(""),
    [query, setQuery] = useState("")
  async function load() {
    setLoading(true)
    try {
      setPaths(await learningApi.discover())
      setError("")
    } catch (e) {
      setError(
        e instanceof Error ? e.message : "Unable to load published paths",
      )
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [])
  const shown = paths.filter((p) =>
    (p.title + " " + p.summary + " " + (p.organization_name ?? ""))
      .toLowerCase()
      .includes(query.toLowerCase()),
  )
  return (
    <div className="public-page">
      <PublicHeader />
      <main className="v7-wrap">
        <header className="v7-hero">
          <span className="org-eyebrow">EXPLORE LEARNING</span>
          <h1>Find a path worth finishing.</h1>
          <p>
            Explore real published programs. You choose when to enroll, and your
            progress stays tied to the version you joined.
          </p>
        </header>
        <label className="v7-search">
          Search paths{" "}
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search a skill, path or organization"
          />
        </label>
        {loading ? (
          <p role="status">Loading published paths…</p>
        ) : error ? (
          <div role="alert" className="org-error">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </div>
        ) : shown.length === 0 ? (
          <div className="v7-card">
            <h2>
              {query
                ? "No paths match your search"
                : "No published learning paths yet"}
            </h2>
            <p>Check back when an approved organization publishes a path.</p>
          </div>
        ) : (
          <div className="v7-grid">
            {shown.map((path) => (
              <article className="v7-card" key={path.id}>
                <span className="org-eyebrow">{path.organization_name}</span>
                <h2>{path.title}</h2>
                <p>{path.summary}</p>
                <small>Published version {path.version_no}</small>
                <Button asChild variant="outline">
                  <Link to={`/learning-paths/${path.id}`}>See the path</Link>
                </Button>
              </article>
            ))}
          </div>
        )}
      </main>
      <PublicFooter />
    </div>
  )
}
