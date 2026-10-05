import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import Button from "@/components/ui/Button"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
import { OrganizerSurface } from "@/features/organizer/OrganizerSurface"
import { learningApi } from "@/features/learning/learningApi"
import { quizApi } from "@/features/quiz/quizApi"
import { certificationApi } from "@/features/certification/certificationApi"
export default function OrganizerPage() {
  const { organization, grants, owned } = useOrganizationContext()
  const [counts, setCounts] = useState<{
    paths?: number
    banks?: number
    programs?: number
    certificates?: number
  }>({})
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    let active = true
    setLoading(true)
    setError("")
    ;(async () => {
      if (!organization) return
      const values: {
        paths?: number
        banks?: number
        programs?: number
        certificates?: number
      } = {}
      if (grants.includes("MANAGE_CONTENT")) {
        const [paths, banks] = await Promise.all([
          learningApi.owned(organization.id),
          quizApi.banks(organization.id),
        ])
        values.paths = paths.length
        values.banks = banks.length
      }
      if (grants.includes("ISSUE_CERTIFICATES")) {
        const [programs, page] = await Promise.all([
          certificationApi.programs(organization.id),
          certificationApi.search(organization.id),
        ])
        values.programs = programs.length
        values.certificates = page.totalElements
      }
      if (active) setCounts(values)
    })()
      .catch((e) => {
        if (active) setError(e.message)
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [organization?.id, grants.join(",")])
  return (
    <OrganizerSurface
      title="Your organizer workspace"
      description="Create learning experiences and issue certificates backed by evidence."
    >
      {error && (
        <p className="sporg-alert" role="alert">
          {error}
        </p>
      )}
      {loading ? (
        <p role="status">Loading workspace…</p>
      ) : (
        <div className="sporg-grid">
          {Object.entries(counts).map(([label, count]) => (
            <section className="sporg-card" key={label}>
              <p className="sporg-eyebrow">{label}</p>
              <h2>{count}</h2>
            </section>
          ))}
          <section className="sporg-card sporg-full">
            <h2>Where would you like to begin?</h2>
            <div className="sporg-actions">
              <Button asChild variant="outline">
                <Link to="/organizer/organization">Organization and team</Link>
              </Button>
              {grants.includes("MANAGE_CONTENT") && (
                <Button asChild>
                  <Link to="/organizer/paths">Build a learning path</Link>
                </Button>
              )}
              {grants.includes("ISSUE_CERTIFICATES") && (
                <Button asChild variant="outline">
                  <Link to="/organizer/certifications">
                    Manage certifications
                  </Link>
                </Button>
              )}
            </div>
            {!grants.length && (
              <p>
                You have an active membership. Ask your manager to grant the
                permissions needed for your work.
              </p>
            )}
          </section>
          {!owned && (
            <section className="sporg-card">
              <h2>Create your own organization</h2>
              <p>
                You can keep your existing memberships while applying for your
                own organization.
              </p>
              <Button asChild variant="outline">
                <Link to="/onboarding/organizer">Start application</Link>
              </Button>
            </section>
          )}
          {owned && owned.status !== "APPROVED" && (
            <section className="sporg-card">
              <h2>Your own application</h2>
              <p>
                {owned.displayName} · {owned.status}
              </p>
              <Button asChild variant="outline">
                <Link to="/onboarding/organizer">View application</Link>
              </Button>
            </section>
          )}
        </div>
      )}
    </OrganizerSurface>
  )
}
