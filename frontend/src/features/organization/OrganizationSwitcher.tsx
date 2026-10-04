import { useEffect, useState } from "react"
import { organizationApi, type Organization } from "./organizationApi"

const STORAGE_KEY = "skillproof.organizer.organization"

export function selectedOrganizationId() {
  return localStorage.getItem(STORAGE_KEY)
}

export default function OrganizationSwitcher() {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [selected, setSelected] = useState("")
  useEffect(() => {
    organizationApi.memberships().then((items) => {
      setOrganizations(items)
      const saved = selectedOrganizationId()
      setSelected(items.some((item) => item.id === saved) ? saved! : items[0]?.id ?? "")
      if (!saved && items[0]) localStorage.setItem(STORAGE_KEY, items[0].id)
    }).catch(() => setOrganizations([]))
  }, [])
  if (organizations.length < 2) return null
  return (
    <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
      <span className="sr-only">Working organization</span>
      <select
        aria-label="Working organization"
        value={selected}
        onChange={(event) => {
          localStorage.setItem(STORAGE_KEY, event.target.value)
          setSelected(event.target.value)
          window.location.reload()
        }}
      >
        {organizations.map((organization) => (
          <option key={organization.id} value={organization.id}>{organization.displayName}</option>
        ))}
      </select>
    </label>
  )
}
