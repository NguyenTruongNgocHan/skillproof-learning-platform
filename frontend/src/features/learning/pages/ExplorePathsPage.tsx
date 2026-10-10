import { ArrowRight, BookOpen, Search, Sparkles } from "lucide-react"
import { useEffect, useMemo, useState } from "react"
import { Link, useSearchParams } from "react-router-dom"

import { learningApi } from "@/features/learning/api/learningApi"
import type { Path } from "@/features/learning/types/learning.types"
import LearnerSurface from "@/shared/components/layout/LearnerSurface"
import Button from "@/shared/ui/Button"

export default function ExplorePathsPage() {
  const [params, setParams] = useSearchParams()
  const [paths, setPaths] = useState<Path[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const query = params.get("search") ?? ""

  async function load() {
    setLoading(true)
    try {
      setPaths(await learningApi.discover())
      setError("")
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "We couldn't load learning paths right now.",
      )
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [])

  const shown = useMemo(
    () =>
      paths.filter((path) =>
        `${path.title} ${path.summary} ${path.organization_name ?? ""}`
          .toLowerCase()
          .includes(query.toLowerCase()),
      ),
    [paths, query],
  )

  return (
    <LearnerSurface>
      <main className="learner-page learner-page--explore">
        <header className="explore-page__hero">
          <div className="explore-page__glow" />
          <div className="explore-page__copy">
            <span>
              <Sparkles size={15} /> Follow your curiosity
            </span>
            <h1>
              Find something
              <br />
              worth <em>learning.</em>
            </h1>
            <p>
              Search published paths by skill, topic, or organization. You can look around before
              deciding what deserves your time.
            </p>
          </div>
          <label className="explore-search">
            <Search size={20} />
            <input
              type="search"
              value={query}
              onChange={(event) => {
                const value = event.target.value
                setParams(value ? { search: value } : {})
              }}
              placeholder="Try â€˜Japaneseâ€™, â€˜backendâ€™, or an organization…"
              aria-label="Search learning paths"
            />
            {query ? (
              <button type="button" onClick={() => setParams({})}>
                Clear
              </button>
            ) : null}
          </label>
        </header>

        <section className="explore-results">
          <div className="explore-results__heading">
            <h2>{query ? `Results for â€œ${query}â€` : "Explore published paths"}</h2>
            {!loading ? <span>{shown.length} available</span> : null}
          </div>
          {loading ? (
            <div className="learner-state" role="status">
              Finding learning paths…
            </div>
          ) : error ? (
            <div className="learner-state learner-state--error" role="alert">
              <strong>Discovery is unavailable right now.</strong>
              <span>{error}</span>
              <Button variant="outline" onClick={() => void load()}>
                Try again
              </Button>
            </div>
          ) : shown.length === 0 ? (
            <div className="learner-empty">
              <span className="learner-empty__icon">
                <Search size={23} />
              </span>
              <h3>{query ? "Nothing matched that search." : "No published paths yet."}</h3>
              <p>
                {query
                  ? "Try a broader skill, topic, or organization name."
                  : "Published learning paths will appear here when they become available."}
              </p>
            </div>
          ) : (
            <div className="path-gallery">
              {shown.map((path, index) => (
                <Link
                  className="path-gallery__item"
                  to={`/learning-paths/${path.id}`}
                  key={path.id}
                >
                  <span className="path-gallery__number">{String(index + 1).padStart(2, "0")}</span>
                  <span className="path-gallery__icon">
                    <BookOpen size={21} />
                  </span>
                  <div>
                    <small>{path.organization_name || "SkillProof learning"}</small>
                    <h3>{path.title}</h3>
                    <p>{path.summary || "Explore this published learning path."}</p>
                    <span>
                      Explore path <ArrowRight size={14} />
                    </span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </section>
      </main>
    </LearnerSurface>
  )
}
