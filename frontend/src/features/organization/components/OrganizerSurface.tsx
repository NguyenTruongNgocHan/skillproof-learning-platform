import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import "@/features/organization/styles/organizer.css";
import "@/features/organization/styles/workspace.css";
import type { Authority } from "@/features/organization/types/organization.types";
import AppShell from "@/shared/components/layout/AppShell";
import type { ReactNode } from "react";
export function OrganizerSurface({
  title,
  description,
  authority,
  children,
}: {
  title: string;
  description?: string;
  authority?: Authority;
  children: ReactNode;
}) {
  const { organization, grants } = useOrganizationContext();
  return (
    <AppShell>
      <section className="sporg skillproof-app-theme">
        <header className="sporg-heading">
          <p className="sporg-eyebrow">
            {organization?.displayName ?? "Organizer"}
          </p>
          <h1>{title}</h1>
          {description && <p>{description}</p>}
        </header>
        {authority && !grants.includes(authority) ? (
          <section className="sporg-card">
            <h2>Permission required</h2>
            <p>
              Ask your organization manager to grant{" "}
              {authority.replace(/_/g, " ").toLowerCase()}.
            </p>
          </section>
        ) : (
          children
        )}
      </section>
    </AppShell>
  );
}
