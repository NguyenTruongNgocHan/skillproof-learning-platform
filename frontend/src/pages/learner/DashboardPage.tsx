import AppShell from "@/components/layout/AppShell";
import ContinueLearningCard from "@/features/learner-dashboard/components/ContinueLearningCard";
import LearnerStatsRow from "@/features/learner-dashboard/components/LearnerStatsRow";
import RecommendedPathsSection from "@/features/learner-dashboard/components/RecommendedPathsSection";
import PracticeSection from "@/features/learner-dashboard/components/PracticeSection";
import CertificatesSection from "@/features/learner-dashboard/components/CertificatesSection";
import {
  CURRENT_LEARNING_PATH,
  RECOMMENDED_PATHS,
  WEEKLY_GOAL,
  CAREER_GOAL,
} from "@/mocks/dashboard/learnerDashboard";

export default function DashboardPage() {
  return (
    <AppShell>
      <div
        style={{
          maxWidth: "900px",
          margin: "0 auto",
          display: "flex",
          flexDirection: "column",
          gap: "24px",
        }}
      >
        {/* Greeting header */}
        <div>
          <h1
            style={{
              fontSize: "1.5rem",
              fontWeight: "700",
              color: "var(--fg)",
              margin: 0,
            }}
          >
            Good morning, Alex.
          </h1>
          <p
            style={{
              color: "var(--fg-muted)",
              margin: "4px 0 0",
              fontSize: "0.9rem",
            }}
          >
            Saturday, September 5, 2026
          </p>
        </div>

        <ContinueLearningCard path={CURRENT_LEARNING_PATH} />
        <LearnerStatsRow careerGoal={CAREER_GOAL} weeklyGoal={WEEKLY_GOAL} />
        <RecommendedPathsSection paths={RECOMMENDED_PATHS} />
        <PracticeSection />
        <CertificatesSection />
      </div>
    </AppShell>
  );
}
