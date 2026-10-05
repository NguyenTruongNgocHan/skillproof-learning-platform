import { OrganizerSurface } from "@/features/organizer/OrganizerSurface"
import { ApplicationHistory } from "@/features/organizer/ApplicationHistory"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
import { OrganizationProfilePanel } from "@/features/organization/components/OrganizationProfilePanel"
import { OrganizationTeamPanel } from "@/features/organization/components/OrganizationTeamPanel"
export default function OrganizationWorkspacePage() {
  const { organization, grants } = useOrganizationContext()
  return (
    <OrganizerSurface
      title="Organization and team"
      description="Manage the verified profile, team membership and responsibilities."
    >
      <div className="sporg-grid">
        <OrganizationProfilePanel />
        {grants.includes("MANAGE_MEMBERS") && <OrganizationTeamPanel />}
        {organization && (
          <div className="sporg-full">
            <ApplicationHistory
              organizationId={organization.id}
              documentsVisible={grants.includes("MANAGE_PROFILE")}
            />
          </div>
        )}
      </div>
    </OrganizerSurface>
  )
}
