import {
  ArrowRight,
  CheckCircle2,
  Play,
  ShieldCheck,
  Sparkles,
} from "lucide-react";
import { Link } from "react-router-dom";
import Button from "@/components/ui/Button";
import Container from "@/components/ui/Container";

export default function HeroSection() {
  return (
    <section className="marketing-hero">
      <div className="hero-glow hero-glow--one" />
      <div className="hero-glow hero-glow--two" />
      <Container>
        <div className="hero-layout">
          <div className="hero-copy">
            <div className="hero-kicker">
              <Sparkles size={14} /> Build skills with a clear path
            </div>
            <h1>
              Build real skills.
              <br />
              <span>Make them undeniable.</span>
            </h1>
            <p>
              SkillProof connects structured learning, meaningful assessment,
              and independently verifiable credentials in one trusted journey.
            </p>
            <div className="hero-actions">
              <Button asChild variant="primary" size="lg">
                <Link to="/register">
                  Create an account <ArrowRight size={17} />
                </Link>
              </Button>
              <a href="#how-it-works" className="hero-text-action">
                <span>
                  <Play size={14} fill="currentColor" />
                </span>
                See how it works
              </a>
            </div>
            <div className="hero-proof">
              {[
                "Role-aware experience",
                "Secure identity flow",
                "Credential roadmap",
              ].map((item) => (
                <span key={item}>
                  <CheckCircle2 size={15} />
                  {item}
                </span>
              ))}
            </div>
          </div>
          <div
            className="hero-product"
            aria-label="SkillProof learning progress preview"
          >
            <div className="hero-product__top">
              <div>
                <span>CONCEPT PREVIEW · LEARNING PATH</span>
                <h2>Backend Engineering</h2>
              </div>
              <div className="verified-pill">
                <ShieldCheck size={15} /> Planned
              </div>
            </div>
            <div className="learning-ring">
              <div>
                <strong>Path</strong>
                <span>preview</span>
              </div>
            </div>
            <div className="hero-product__content">
              <div className="module-row module-row--done">
                <CheckCircle2 />
                <span>
                  <b>Programming foundations</b>
                  <small>Learning milestone</small>
                </span>
              </div>
              <div className="module-row module-row--done">
                <CheckCircle2 />
                <span>
                  <b>API design & REST</b>
                  <small>Practice milestone</small>
                </span>
              </div>
              <div className="module-row module-row--active">
                <span className="module-pulse" />
                <span>
                  <b>Cloud fundamentals</b>
                  <small>A structured next step</small>
                </span>
                <ArrowRight size={16} />
              </div>
            </div>
            <div className="floating-badge floating-badge--top">
              <Sparkles size={16} />
              <span>
                <b>AI guidance</b>
                <small>Personalized next step</small>
              </span>
            </div>
            <div className="floating-badge floating-badge--bottom">
              <ShieldCheck size={16} />
              <span>
                <b>Future credential</b>
                <small>Portable credential</small>
              </span>
            </div>
          </div>
        </div>
      </Container>
    </section>
  );
}
