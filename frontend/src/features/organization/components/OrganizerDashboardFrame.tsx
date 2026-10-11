import { Link } from "react-router-dom";
import { BarChart3, MessageSquare, ListChecks } from "lucide-react";
import { useOrganizationContext } from "../providers/OrganizationProvider";
export function OrganizerDashboardFrame() {
  const { grants } = useOrganizationContext();
  return (
    <section className="sporg-dashboard">
      <header>
        <h2>Operate & improve</h2>
        <p>
          Review real learner activity, verified payments and feedback from your
          workspace.
        </p>
      </header>
      <div className="sporg-grid">
        {[
          {
            title: "Reach & conversions",
            description:
              "Visibility, campaigns, enrollments, completions and gross paid amounts.",
            icon: BarChart3,
            path: "/organizer/analytics",
            authority: "VIEW_ANALYTICS",
          },
          {
            title: "Learner feedback",
            description:
              "Read verified reviews, reply and report policy violations.",
            icon: MessageSquare,
            path: "/organizer/feedback",
            authority: "MANAGE_FEEDBACK",
          },
          {
            title: "Assignment grading",
            description:
              "Review submitted work and reevaluate completion after grading.",
            icon: ListChecks,
            path: "/organizer/grading",
            authority: "GRADE_ASSIGNMENTS",
          },
        ]
          .filter((item) =>
            grants.includes(item.authority as (typeof grants)[number]),
          )
          .map(({ title, description, icon: Icon, path }) => (
            <Link className="sporg-card" to={path} key={path}>
              <Icon size={20} />
              <h3>{title}</h3>
              <p>{description}</p>
              <span>Open report →</span>
            </Link>
          ))}
      </div>
    </section>
  );
}
