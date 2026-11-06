import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import "@/features/organization/styles/organizer.css"
import "@/features/organization/styles/workspace.css"
import type { Authority } from "@/features/organization/types/organization.types"
import AppShell from "@/shared/components/layout/AppShell"
import { useWorkspaceFormSafety } from "../hooks/useWorkspaceFormSafety"
import PageHeader from "@/shared/components/layout/PageHeader"
import type { ReactNode } from "react"
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
  const formRoot = useWorkspaceFormSafety()
  const { organization, grants } = useOrganizationContext()
  return (
    <AppShell>
      <section className="sporg skillproof-app-theme">
        <PageHeader
          title={title}
          description={description}
          eyebrow={organization?.displayName ?? "Organizer"}
          breadcrumb={{ label: "Workspace", to: "/organizer" }}
        />
        {authority && !grants.includes(authority) ? (
          <section className="sporg-card">
            <h2>Permission required</h2>
            <p>
              Ask your organization manager to grant {authority.replace(/_/g, " ").toLowerCase()}.
            </p>
          </section>
        ) : (
          <div ref={formRoot} className="sporg-page-flow">
            {children}
          </div>
        )}
      </section>
    </AppShell>
  )
}
