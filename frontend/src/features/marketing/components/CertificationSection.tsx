import { Award, ClipboardCheck, ShieldCheck } from "lucide-react";
import Container from "@/components/ui/Container";
import SectionHeading from "@/components/ui/SectionHeading";

const stages = [
  {
    icon: ClipboardCheck,
    title: "Meet the requirements",
    description: "Progress and assessment evidence are evaluated against the published Completion Policy.",
  },
  {
    icon: ShieldCheck,
    title: "Authorized issuance",
    description: "An authorized organizer in an approved organization decides when to issue a certificate.",
  },
  {
    icon: Award,
    title: "Keep a traceable record",
    description: "The issued certificate links back to the learner, organization, program and path version.",
  },
];

export default function CertificationSection() {
  return (
    <section className="py-24 bg-subtle-skin sp-certification">
      <Container>
        <SectionHeading
          eyebrow="OFFICIAL CERTIFICATION"
          title="Achievement follows evidence."
          subtitle="Official certificates depend on published completion requirements and an authorized issuer. This certification workflow is planned for a future release."
        />
        <div className="sp-certification__stages">
          {stages.map(({ icon: Icon, title, description }, index) => (
            <div className="sp-certification__stage" key={title}>
              <span className="sp-certification__icon"><Icon size={23} aria-hidden="true" /></span>
              <span className="sp-certification__count">0{index + 1}</span>
              <h3>{title}</h3>
              <p>{description}</p>
            </div>
          ))}
        </div>
        <p className="sp-certification__boundary">
          Community practice, battle ratings, payments and recommendation scores do not replace completion evidence.
        </p>
      </Container>
      <style>{`
        .sp-certification__stages { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; margin-top: 42px; }
        .sp-certification__stage { position: relative; min-width: 0; padding: 30px; border: 1px solid var(--border); border-radius: 20px; background: var(--surface); transition: transform .25s ease, border-color .25s ease; }
        .sp-certification__stage:hover { transform: translateY(-4px); border-color: var(--brand); }
        .sp-certification__icon { display: grid; place-items: center; width: 48px; height: 48px; margin-bottom: 28px; border-radius: 14px; color: var(--brand); background: var(--brand-soft); }
        .sp-certification__count { position: absolute; top: 33px; right: 30px; color: var(--brand); font-size: .78rem; font-weight: 800; }
        .sp-certification__stage h3 { margin: 0 0 11px; color: var(--fg); font-size: 1.1rem; }
        .sp-certification__stage p { margin: 0; color: var(--fg-muted); font-size: .92rem; line-height: 1.6; }
        .sp-certification__boundary { margin: 22px 0 0; color: var(--fg-muted); font-size: .85rem; line-height: 1.6; }
        @media (max-width: 800px) { .sp-certification__stages { grid-template-columns: 1fr; } }
        @media (prefers-reduced-motion: reduce) { .sp-certification__stage { transition: none; } }
      `}</style>
    </section>
  );
}
