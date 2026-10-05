import "@/styles/organizer.css"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
export default function OrganizationSwitcher() {
  const { organizations, organization, select, loading, refresh } =
    useOrganizationContext()
  if (!organizations.length) return null
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
