import {
  Shield,
  Users,
  CheckCircle,
  Star,
  BookOpen,
  Award,
} from "lucide-react";
import Container from "@/components/ui/Container";
import SectionHeading from "@/components/ui/SectionHeading";

export default function CommunitySection() {
  return (
    <section className="py-24 bg-subtle-skin">
      <Container>
        <SectionHeading
          title="Practice beyond the official curriculum."
          subtitle="The planned community will expand practice beyond official Learning Paths. Community activity does not grant official certificate eligibility."
          centered
        />

        <div className="grid md:grid-cols-2 gap-8 mb-8">
          {/* Official */}
          <div className="rounded-2xl border-2 border-strong-skin p-8">
            <div className="flex items-center gap-2 mb-4">
              <div className="flex items-center gap-2 bg-ink-skin px-3 py-1.5 rounded-lg">
                <Shield size={14} />
                <span className="text-xs font-semibold uppercase tracking-wide">
                  Official
                </span>
              </div>
            </div>
            <h3 className="font-semibold text-skin mb-1">
              Organization-verified learning paths
            </h3>
            <p className="text-sm text-muted-skin mb-5">
              Curricula authored and maintained by approved training
              organizations.
            </p>

            <div className="space-y-3">
              {[
                {
                  icon: BookOpen,
                  text: "Structured curriculum from verified organizations",
                },
                {
                  icon: Award,
                  text: "Organization-issued certificates upon completion",
                },
                {
                  icon: CheckCircle,
                  text: "Completion follows the organization’s published policy",
                },
              ].map((item) => (
                <div key={item.text} className="flex items-start gap-2.5">
                  <item.icon
                    size={15}
                    color="var(--brand)"
                    className="mt-0.5 flex-shrink-0"
                  />
                  <span className="text-sm text-skin">{item.text}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Community */}
          <div className="rounded-2xl border border-skin p-8 bg-subtle-skin">
            <div className="flex items-center gap-2 mb-4">
              <div className="flex items-center gap-2 bg-subtle-skin text-muted-skin px-3 py-1.5 rounded-lg">
                <Users size={14} />
                <span className="text-xs font-semibold uppercase tracking-wide">
                  Community
                </span>
              </div>
            </div>
            <h3 className="font-semibold text-skin mb-1">
              Learner-created practice content
            </h3>
            <p className="text-sm text-muted-skin mb-5">
              Quizzes, mock tests, and flashcards shared by the SkillProof
              community.
            </p>

            <div className="space-y-3">
              {[
                { icon: BookOpen, text: "Community quizzes and mock tests" },
                { icon: Star, text: "Community ratings are planned as a discovery signal" },
                { icon: CheckCircle, text: "Access will follow the content’s published policy" },
              ].map((item) => (
                <div key={item.text} className="flex items-start gap-2.5">
                  <item.icon
                    size={15}
                    color="var(--fg-muted)"
                    className="mt-0.5 flex-shrink-0"
                  />
                  <span className="text-sm text-skin">{item.text}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="rounded-lg border border-skin bg-subtle-skin px-5 py-3 text-sm text-muted-skin text-center">
          Concept preview. Community practice does not automatically count toward an official program or certificate.
        </div>
      </Container>
    </section>
  );
}
