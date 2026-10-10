import { Link } from "react-router-dom";
import { BookOpen, Users, ArrowUpRight, FileCheck2 } from "lucide-react";
import { OrganizerSurface } from "../components/OrganizerSurface";
import { OrganizationIdentity } from "../components/OrganizationIdentity";
import { OrganizerDashboardFrame } from "../components/OrganizerDashboardFrame";
import { useOrganizationContext } from "../providers/OrganizationProvider";
import Button from "@/shared/ui/Button";
export default function OrganizerPage() {
  const { organization, grants, owned, select } = useOrganizationContext();
  const links = [
    {
      label: "Organization & team",
      text: "Manage your profile and collaborators.",
      to: "/organizer/organization",
      icon: Users,
    },
    ...(grants.includes("MANAGE_CONTENT")
      ? [
          {
            label: "Courses",
            text: "Continue authoring or start a new course.",
            to: "/organizer/courses",
            icon: BookOpen,
          },
        ]
      : []),
    ...(grants.includes("ISSUE_CERTIFICATES")
      ? [
          {
            label: "Certifications",
            text: "Review eligibility and issue certificates.",
            to: "/organizer/certifications",
            icon: FileCheck2,
          },
        ]
      : []),
  ];
  return (
    <OrganizerSurface
      title="Overview"
      description="Your organization, your team, your next steps."
    >
      {organization && <OrganizationIdentity organization={organization} />}
      <section className="sporg-quick-actions" aria-label="Workspace shortcuts">
        {links.map(({ label, text, to, icon: Icon }) => (
          <Link key={to} to={to}>
            <Icon size={21} />
            <div>
              <strong>{label}</strong>
              <small>{text}</small>
            </div>
            <ArrowUpRight size={16} />
          </Link>
        ))}
      </section>
      <div className="sporg-grid">
        <section className="sporg-card">
          <h2>Your responsibilities</h2>
          <div className="sporg-grant-chips">
            {grants.length ? (
              grants.map((g) => (
                <span className="sporg-tag" key={g}>
                  {
                    {
                      MANAGE_PROFILE: "Profile management",
                      MANAGE_MEMBERS: "Team management",
                      MANAGE_CONTENT: "Content authoring",
                      ISSUE_CERTIFICATES: "Certificate issuance",
                    }[g]
                  }
                </span>
              ))
            ) : (
              <p>
                You have read access. Ask your organization manager to assign
                responsibilities.
              </p>
            )}
          </div>
        </section>
        <section className="sporg-card">
          <h2>Your application</h2>
          {owned ? (
            <>
              <p>
                {owned.displayName}{" "}
                <span
                  className={`sporg-badge sporg-badge-${owned.status.toLowerCase()}`}
                >
                  {owned.status.toLowerCase()}
                </span>
              </p>
              <Button asChild variant="outline">
                <Link
                  onClick={() => {
                    if (owned.status === "APPROVED") select(owned.id);
                  }}
                  to={
                    owned.status === "APPROVED"
                      ? "/organizer/organization"
                      : owned.status === "DRAFT"
                        ? "/onboarding/organizer"
                        : "/organizer/verification-pending"
                  }
                >
                  View application
                </Link>
              </Button>
            </>
          ) : (
            <>
              <p>
                Apply for your own organization while keeping your memberships.
              </p>
              <Button asChild variant="outline">
                <Link to="/onboarding/organizer">Start application</Link>
              </Button>
            </>
          )}
        </section>
      </div>
      <OrganizerDashboardFrame />
    </OrganizerSurface>
  );
}
