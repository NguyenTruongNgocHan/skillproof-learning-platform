import { useState } from "react";
import MediaPanel from "@/features/media/components/MediaPanel";
import { ApplicationHistory } from "../components/ApplicationHistory";
import { OrganizationProfilePanel } from "../components/OrganizationProfilePanel";
import { OrganizationTeamPanel } from "../components/OrganizationTeamPanel";
import { OrganizationIdentity } from "../components/OrganizationIdentity";
import { OrganizerSurface } from "../components/OrganizerSurface";
import { useOrganizationContext } from "../providers/OrganizationProvider";
export default function OrganizationWorkspacePage() {
  const { organization, grants } = useOrganizationContext();
  const [tab, setTab] = useState("profile");
  const [panelDirty, setPanelDirty] = useState(false);
  const changeTab = (next: string) => {
    if (next === tab) return;
    if (panelDirty && !window.confirm("Discard unsaved changes?")) return;
    setPanelDirty(false);
    setTab(next);
  };
  const tabs = [
    "profile",
    ...(grants.includes("MANAGE_MEMBERS") ? ["team"] : []),
    ...(grants.includes("MANAGE_PROFILE") ? ["documents"] : []),
    "history",
  ];
  return (
    <OrganizerSurface
      title="Organization"
      description="Your verified identity, team and application records."
    >
      {organization && <OrganizationIdentity organization={organization} />}
      <div
        className="sporg-local-tabs"
        role="tablist"
        aria-label="Organization settings"
      >
        {tabs.map((t) => (
          <button
            role="tab"
            id={`org-tab-${t}`}
            aria-selected={tab === t}
            aria-controls="org-panel"
            key={t}
            onClick={() => changeTab(t)}
          >
            {t === "history"
              ? "Application history"
              : t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>
      <div role="tabpanel" id="org-panel" aria-labelledby={`org-tab-${tab}`}>
        {tab === "profile" && (
          <OrganizationProfilePanel
            key={organization?.id}
            onDirtyChange={setPanelDirty}
          />
        )}{" "}
        {tab === "team" && grants.includes("MANAGE_MEMBERS") && (
          <OrganizationTeamPanel
            key={organization?.id}
            onDirtyChange={setPanelDirty}
          />
        )}{" "}
        {tab === "documents" &&
          organization &&
          grants.includes("MANAGE_PROFILE") && (
            <section className="sporg-card">
              <h2>Submitted documents</h2>
              <p>
                Supporting evidence is kept with application revisions. Approved
                application documents are read-only.
              </p>
              <MediaPanel scope="ORGANIZATION" target={organization.id} />
            </section>
          )}
        {tab === "history" && organization && (
          <ApplicationHistory
            organizationId={organization.id}
            refreshKey={organization.updatedAt}
            documentsVisible={grants.includes("MANAGE_PROFILE")}
          />
        )}
      </div>
    </OrganizerSurface>
  );
}
