import { BarChart3, CreditCard } from "lucide-react";
export function OrganizerDashboardFrame() {
  return (
    <section className="sporg-dashboard" aria-label="Upcoming reporting">
      <header>
        <h2>Reports</h2>
        <p>Additional insights are coming soon.</p>
      </header>
      <div className="sporg-grid">
        {[
          {
            title: "Learning insights",
            description:
              "Enrollment, progress and completion across your organization.",
            icon: BarChart3,
          },
          {
            title: "Revenue reports",
            description: "Revenue and payment insights for your organization.",
            icon: CreditCard,
          },
        ].map(({ title, description, icon: Icon }) => (
          <section key={title} className="sporg-card sporg-report-placeholder">
            <Icon size={20} />
            <div>
              <h3>{title}</h3>
              <p>{description}</p>
            </div>
            <span className="sporg-tag">Coming soon</span>
          </section>
        ))}
      </div>
    </section>
  );
}
