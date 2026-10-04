import { useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import {
  organizationApi,
  type Organization,
} from "@/features/organization/organizationApi"
import { selectedOrganizationId } from "@/features/organization/OrganizationSwitcher"
import { learningApi, type Path } from "@/features/learning/learningApi"
export default function OrganizerPathsPage() {
  const navigate = useNavigate()
  const [org, setOrg] = useState<Organization | null>(null),
    [items, setItems] = useState<Path[]>([]),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false),
    [title, setTitle] = useState(""),
    [summary, setSummary] = useState(""),
    [slug, setSlug] = useState("")
  async function load() {
    setLoading(true)
    try {
      const o = await organizationApi.mine()
      const selected = selectedOrganizationId()
      const organization = selected
        ? (await organizationApi.memberships()).find((item) => item.id === selected) ?? o
        : o
      setOrg(organization)
      setItems(await learningApi.owned(organization.id))
      setError("")
    } catch (e) {
      setError(
        e instanceof Error ? e.message : "Unable to load organization paths",
      )
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [])
  async function create(e: React.FormEvent) {
    e.preventDefault()
    if (!org) return
    setBusy(true)
    setError("")
    try {
      const path = await learningApi.create(org.id, { slug, title, summary })
      navigate(`/organizer/paths/${path.id}`)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to create path")
    } finally {
      setBusy(false)
    }
  }
  return (
    <AppShell>
      <div className="v7-wrap">
        <header className="v7-hero">
          <span className="org-eyebrow">CONTENT STUDIO</span>
          <h1>Build learning people can finish.</h1>
          <p>
            {org?.displayName ?? "Your organization"} · Draft first, check every
            resource and assessment, then publish an immutable version.
          </p>
        </header>
        {loading ? (
          <p role="status">Checking organization access…</p>
        ) : error ? (
          <div role="alert" className="org-error">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </div>
        ) : (
          <>
            <div className="v7-grid">
              <section className="v7-card">
                <h2>Start a learning path</h2>
                <form className="v7-form" onSubmit={(e) => void create(e)}>
                  <label>
                    Title
                    <input
                      required
                      maxLength={180}
                      value={title}
                      onChange={(e) => {
                        setTitle(e.target.value)
                        if (!slug)
                          setSlug(
                            e.target.value
                              .toLowerCase()
                              .normalize("NFKD")
                              .replace(/[^a-z0-9]+/g, "-")
                              .replace(/^-|-$/g, ""),
                          )
                      }}
                    />
                  </label>
                  <label>
                    Short URL name
                    <input
                      required
                      maxLength={100}
                      pattern="[a-z0-9]+(-[a-z0-9]+)*"
                      value={slug}
                      onChange={(e) => setSlug(e.target.value)}
                    />
                  </label>
                  <label>
                    What learners will gain
                    <textarea
                      required
                      maxLength={1000}
                      value={summary}
                      onChange={(e) => setSummary(e.target.value)}
                    />
                  </label>
                  <Button type="submit" disabled={busy}>
                    {busy ? "Creating…" : "Create draft"}
                  </Button>
                </form>
              </section>
              <section className="v7-card">
                <h2>Your learning paths</h2>
                {items.length === 0 ? (
                  <p>
                    No paths yet. Create a draft and shape the first journey.
                  </p>
                ) : (
                  items.map((p) => (
                    <Link
                      className="v7-row"
                      key={p.id}
                      to={`/organizer/paths/${p.id}`}
                    >
                      {p.title}{" "}
                      <small>
                        {p.current_version_id
                          ? "Published · version " + p.version_no
                          : "Draft only"}
                      </small>
                    </Link>
                  ))
                )}
                <Button asChild variant="outline">
                  <Link to="/organizer/questions">Question banks</Link>
                </Button>
              </section>
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
