import { useCallback, useState } from "react";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import { workspaceCourseApi } from "@/features/course/api/workspaceCourseApi";
import { useRemote } from "@/shared/hooks/useRemote";
import { RemoteState } from "@/shared/ui/RemoteState";
import { CourseCommercialPanel } from "../components/CourseCommercialPanel";
export default function OrganizationPricingPage() {
  const { organization, confirmDiscard } = useOrganizationContext();
  const remote = useRemote(
    useCallback(
      () => workspaceCourseApi.directory(organization?.id ?? ""),
      [organization?.id],
    ),
  );
  const [selected, setSelected] = useState("");
  const version =
    remote.data?.find((v) => v.id === selected) ?? remote.data?.[0];
  return (
    <OrganizerSurface
      title="Pricing & promotions"
      description="Manage commercial offers independently of course authoring authority."
      authority="MANAGE_PRICING"
    >
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.length === 0 && (
        <p>No published course available for pricing.</p>
      )}
      {version && (
        <>
          <label>
            Published course version
            <select
              value={version.id}
              onChange={(e) => {
                if (confirmDiscard("Discard unsaved commercial settings?"))
                  setSelected(e.target.value);
              }}
            >
              {remote.data?.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.title} · {v.id}
                </option>
              ))}
            </select>
          </label>
          <CourseCommercialPanel key={version.id} versionId={version.id} />
        </>
      )}
    </OrganizerSurface>
  );
}
