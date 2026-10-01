import { ArrowRight, Compass, Layers3, Sparkles } from "lucide-react"
import Container from "@/components/ui/Container"
import SectionHeading from "@/components/ui/SectionHeading"
export default function RecommendationSection() {
  return (
    <section className="marketing-section bg-subtle-skin">
      <Container>
        <SectionHeading
          eyebrow="A SMARTER NEXT STEP"
          title="Find a more relevant next step."
          subtitle="Planned recommendations will use your goal, content metadata and available learning signals to suggest Learning Paths and practice content. They are not active in this identity release."
        />
        <div className="recommendation-steps">
          <div>
            <Compass size={25} />
            <span>01 Define a goal</span>
            <p>Tell SkillProof what you want to learn.</p>
          </div>
          <ArrowRight className="recommendation-arrow" size={22} />
          <div>
            <Layers3 size={25} />
            <span>02 Discover content</span>
            <p>Rank eligible paths and practice content for your skills.</p>
          </div>
          <ArrowRight className="recommendation-arrow" size={22} />
          <div>
            <Sparkles size={25} />
            <span>03 Refine suggestions</span>
            <p>Use relevant activity and feedback when available.</p>
          </div>
        </div>
        <p className="recommendation-disclosure">
          This is a concept preview. Recommendations help discovery and do not
          determine completion or certificate eligibility.
        </p>
      </Container>
    </section>
  )
}
