import type { ReactNode } from "react";
import type { Authority } from "@/features/organization/types/organization.types";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
export function OrganizerAuthorityGuard({
  authority,
  children,
}: {
  authority: Authority;
  children: ReactNode;
}) {
  const { grants } = useOrganizationContext();
  if (!grants.includes(authority))
    return (
      <OrganizerSurface title="Permission required" authority={authority}>
        <span />
      </OrganizerSurface>
    );
  return children;
}
