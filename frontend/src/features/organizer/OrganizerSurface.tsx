import type { ReactNode } from "react"
import { NavLink } from "react-router-dom"
import AppShell from "@/components/layout/AppShell"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
import type { Authority } from "@/features/organization/organizationApi"
import "@/styles/organizer.css"
export function OrganizerSurface({
  title,
  description,
  authority,
  children,
}: {
  title: string
  description?: string
  authority?: Authority
  children: ReactNode
}) {
  const { organization, grants } = useOrganizationContext()
  return (
    <AppShell>
      <main className="sporg">
        <header className="sporg-heading">
          <p className="sporg-eyebrow">
            {organization?.displayName ?? "Organizer"}
          </p>
          <h1>{title}</h1>
          {description && <p>{description}</p>}
        </header>
        <nav className="sporg-tabs" aria-label="Organizer workspace">
          <NavLink to="/organizer" end>
            Overview
          </NavLink>
          <NavLink to="/organizer/organization">Organization</NavLink>
          {grants.includes("MANAGE_CONTENT") && (
            <>
              <NavLink to="/organizer/paths">Learning paths</NavLink>
              <NavLink to="/organizer/questions">Question banks</NavLink>
            </>
          )}
          {grants.includes("ISSUE_CERTIFICATES") && (
            <NavLink to="/organizer/certifications">Certifications</NavLink>
          )}
        </nav>
        {authority && !grants.includes(authority) ? (
          <section className="sporg-card">
            <h2>Permission required</h2>
            <p>
              Ask your organization manager to grant{" "}
              {authority.replaceAll("_", " ").toLowerCase()}.
            </p>
          </section>
        ) : (
          children
        )}
      </main>
    </AppShell>
  )
}
