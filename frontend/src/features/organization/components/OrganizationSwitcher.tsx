import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import "@/features/organization/styles/organizer.css"
export default function OrganizationSwitcher() {
  const { organizations, organization, select, loading, error, refresh } = useOrganizationContext()
  if (!organizations.length) {
    if (!loading && !error) return null
    return (
      <div className="sp-org-switcher">
        <select aria-label="Working organization" disabled value="">
          <option value="">{loading ? "Loading organization…" : "Organization unavailable"}</option>
        </select>
      </div>
    )
  }
  return (
    <div className="sp-org-switcher">
      <label>
        <span className="sr-only">Working organization</span>
        <select
          aria-label="Working organization"
          disabled={loading}
          value={organization?.id ?? ""}
          onChange={(e) => select(e.target.value)}
        >
          {organizations.map((o) => (
            <option key={o.id} value={o.id}>
              {o.displayName}
            </option>
          ))}
        </select>
      </label>
      <button
        type="button"
        disabled={loading}
        aria-label="Refresh organization membership and permissions"
        onClick={() => void refresh()}
      >
        ↻
      </button>
    </div>
  )
}
