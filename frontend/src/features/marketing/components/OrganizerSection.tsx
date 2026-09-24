import {
  BookOpen,
  Database,
  ClipboardList,
  Settings,
  Award,
  CheckCircle,
} from "lucide-react";
import { Link } from "react-router-dom";
import Container from "@/components/ui/Container";
import SectionHeading from "@/components/ui/SectionHeading";
import Button from "@/components/ui/Button";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getNextRouteForUserState } from "@/utils/authFlow";

const capabilities = [
  {
    icon: BookOpen,
    title: "Build structured Learning Paths",
    description: "Define modules, lessons, and prerequisite sequences",
  },
  {
    icon: Database,
    title: "Manage Question Banks",
    description: "Author, tag, and organize questions by topic and difficulty",
  },
  {
    icon: ClipboardList,
    title: "Create Quizzes & Assessments",
    description: "Build timed, proctored assessments from your question bank",
  },
  {
    icon: Settings,
    title: "Define Completion Policies",
    description:
      "Set pass marks, attempt limits, and certification requirements",
  },
  {
    icon: Award,
    title: "Manage Certification Programs",
    description: "Evaluate published requirements before authorized issuance",
  },
];

const fakePaths = [
  {
    name: "Backend Engineering Foundations",
    status: "Concept",
  },
  {
    name: "Software Testing Professional",
    status: "Concept",
  },
  {
    name: "Cloud Infrastructure Fundamentals",
    status: "Concept",
  },
];

function DashboardPreview() {
  return (
    <div
      className="rounded-2xl overflow-hidden"
      style={{
        backgroundColor: "var(--surface)",
        border: "1px solid var(--border)",
      }}
    >
      {/* Sidebar + main */}
      <div className="flex h-64">
        {/* Sidebar */}
        <div
          className="w-40 border-r flex flex-col gap-1 p-3"
          style={{ borderColor: "var(--border)", backgroundColor: "var(--bg)" }}
        >
          {["Learning Paths", "Question Bank", "Certificates", "Analytics"].map(
            (item, i) => (
              <div
                key={item}
                className="px-3 py-2 rounded-lg text-xs font-medium"
                style={
                  i === 0
                    ? {
                        backgroundColor: "var(--brand-soft)",
                        color: "var(--brand)",
                      }
                    : { color: "var(--fg-muted)" }
                }
              >
                {item}
              </div>
            ),
          )}
        </div>

        {/* Main content */}
        <div className="flex-1 p-4 overflow-hidden">
          <p className="text-xs font-semibold text-skin mb-3">
            Planned organizer workspace
          </p>
          <div className="space-y-2">
            {fakePaths.map((path) => (
              <div
                key={path.name}
                className="flex items-center gap-2 rounded-lg px-3 py-2"
                style={{ backgroundColor: "var(--bg-subtle)" }}
              >
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-medium text-skin truncate">
                    {path.name}
                  </p>
                  <p className="text-xs text-muted-skin">Illustrative program</p>
                </div>
                <span
                  className="text-xs font-medium px-2 py-0.5 rounded flex-shrink-0"
                  style={{
                    backgroundColor: "var(--brand-soft)",
                    color: "var(--brand)",
                  }}
                >
                  {path.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function OrganizerSection() {
  const { user } = useAuth();
  return (
    <section className="py-24 bg-skin">
      <Container>
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20">
          <div>
            <SectionHeading
              eyebrow="FOR ORGANIZATIONS"
              title="Infrastructure for organizations that teach and certify."
              subtitle="Approved organizations can manage their identity and team today. Learning Paths, assessment and certification tooling are planned."
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {capabilities.map((cap) => (
                <div key={cap.title} className="flex gap-3">
                  <div
                    className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5"
                    style={{ backgroundColor: "var(--brand-soft)" }}
                  >
                    <cap.icon size={15} color="var(--brand)" />
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold text-skin mb-0.5">
                      {cap.title}
                    </h3>
                    <p className="text-xs text-muted-skin">{cap.description}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-8">
              <Button asChild variant="primary" size="md">
                <Link to={user ? getNextRouteForUserState(user) : "/register"}>
                  {user ? "Open my workspace" : "Create an account"}
                </Link>
              </Button>
            </div>
          </div>

          <div className="flex flex-col justify-center">
            <p className="text-xs font-semibold uppercase tracking-wide text-muted-skin mb-4">
              Planned organizer tools
            </p>
            <DashboardPreview />

            <div className="mt-5 space-y-2">
              {[
                "Role-based access for instructors and admins",
                "Official programs remain under the organization’s authority",
                "Only authorized organizers may issue or revoke certificates",
              ].map((item) => (
                <div key={item} className="flex items-center gap-2">
                  <CheckCircle size={13} color="var(--brand)" />
                  <span className="text-xs text-muted-skin">{item}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </Container>
    </section>
  );
}
