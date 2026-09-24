import { ArrowRight, Compass, Layers3, Sparkles } from "lucide-react";
import Container from "@/components/ui/Container";
import SectionHeading from "@/components/ui/SectionHeading";
export default function RecommendationSection() {
  return (
    <section className="marketing-section bg-subtle-skin">
      <Container>
        <SectionHeading
          eyebrow="A SMARTER NEXT STEP"
          title="A learning path that fits your goal."
          subtitle="Our recommendation module is planned to combine learner goals, skill metadata and interactions. It is not active in the current identity release."
        />
        <div className="recommendation-steps">
          <div>
            <Compass size={25} />
            <span>01 · Define a goal</span>
            <p>Tell SkillProof what you want to learn.</p>
          </div>
          <ArrowRight className="recommendation-arrow" size={22} />
          <div>
            <Layers3 size={25} />
            <span>02 · Find relevant content</span>
            <p>Match goals and skills to eligible paths.</p>
          </div>
          <ArrowRight className="recommendation-arrow" size={22} />
          <div>
            <Sparkles size={25} />
            <span>03 · Keep improving</span>
            <p>Use feedback to shape future suggestions.</p>
          </div>
        </div>
        <p className="recommendation-disclosure">
          Recommendation roadmap · concept preview · no ranking or match score
          is being claimed.
        </p>
      </Container>
    </section>
  );
}
