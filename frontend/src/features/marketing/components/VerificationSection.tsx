import {
  ArrowUpRight,
  Briefcase,
  Building2,
  GraduationCap,
  ShieldCheck,
} from "lucide-react";
import Container from "@/components/ui/Container";
import SectionHeading from "@/components/ui/SectionHeading";
const audiences = [
  {
    icon: Briefcase,
    title: "Recruiters",
    description: "Check a credential before an interview.",
  },
  {
    icon: Building2,
    title: "Employers",
    description: "See its issuer and current status.",
  },
  {
    icon: GraduationCap,
    title: "Institutions",
    description: "Review evidence from an independent link.",
  },
];
export default function VerificationSection() {
  return (
    <section className="marketing-section bg-skin">
      <Container>
        <SectionHeading
          eyebrow="CREDENTIAL TRUST"
          title="A credential should be easy to check."
          subtitle="Public verification is planned for issued SkillProof certificates. This identity release does not issue or validate certificates yet."
        />
        <div className="verification-layout">
          <div className="verification-preview">
            <span className="eyebrow">
              <ShieldCheck size={16} /> FUTURE EXPERIENCE · CONCEPT
            </span>
            <h3>One link. A clear answer.</h3>
            <p>
              When certification is live, a public certificate page will show
              the issuing organization, credential status and the evidence
              needed to trust it.
            </p>
            <div className="verification-preview-steps">
              <span>01 · Open certificate link</span>
              <span>02 · Check issuer and status</span>
              <span>03 · Share the result</span>
            </div>
            <span className="verification-preview-foot">
              Verification API coming in the Certification phase{" "}
              <ArrowUpRight size={17} />
            </span>
          </div>
          <div className="verification-audience-list">
            {audiences.map(({ icon: Icon, title, description }) => (
              <div key={title}>
                <span className="verification-audience-icon">
                  <Icon size={21} />
                </span>
                <div>
                  <h3>{title}</h3>
                  <p>{description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </Container>
    </section>
  );
}
