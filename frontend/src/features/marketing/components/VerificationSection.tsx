import Container from "@/shared/ui/Container"
import SectionHeading from "@/shared/ui/SectionHeading"
import { Briefcase, Building2, GraduationCap, ShieldCheck } from "lucide-react"

const audiences = [
  {
    icon: Briefcase,
    title: "Recruiters",
    description: "Check a credential before an interview.",
  },
  {
    icon: Building2,
    title: "Employers",
    description: "See who issued it and whether it is current.",
  },
  {
    icon: GraduationCap,
    title: "Institutions",
    description: "Review the evidence behind an achievement.",
  },
]

export default function VerificationSection() {
  return (
    <section className="marketing-section bg-skin sp-verification">
      <Container>
        <SectionHeading
          eyebrow="CREDENTIAL TRUST"
          title="Let others check what you have earned."
          subtitle="A future public page will let recruiters and other external verifiers use a certificate ID, QR code or link without an account. Certificate lookup is not active yet."
        />

        <div className="sp-verification__layout">
          <div className="sp-verification__people">
            <p className="sp-verification__label">A PUBLIC ANSWER, WITHOUT AN ACCOUNT</p>
            {audiences.map(({ icon: Icon, title, description }, index) => (
              <div className="sp-verification__person" key={title}>
                <span className="sp-verification__icon">
                  <Icon size={20} aria-hidden="true" />
                </span>
                <div>
                  <h3>{title}</h3>
                  <p>{description}</p>
                </div>
                <span className="sp-verification__number" aria-hidden="true">
                  0{index + 1}
                </span>
              </div>
            ))}
          </div>

          <div
            className="sp-verification__visual"
            aria-label="Illustration of a future public credential page"
          >
            <div className="sp-verification__halo" aria-hidden="true" />
            <div className="sp-verification__sheet">
              <div className="sp-verification__sheet-top">
                <span>
                  <ShieldCheck size={21} aria-hidden="true" /> SkillProof
                </span>
                <small>CONCEPT PREVIEW</small>
              </div>
              <div className="sp-verification__seal">
                <ShieldCheck size={34} strokeWidth={1.6} aria-hidden="true" />
              </div>
              <h3>Proof, in one place.</h3>
              <p>Issuer, status and proof in one view.</p>
              <div className="sp-verification__field" aria-hidden="true">
                <span>ISSUER</span>
                <i />
              </div>
              <div className="sp-verification__field" aria-hidden="true">
                <span>STATUS</span>
                <i />
              </div>
              <div className="sp-verification__field" aria-hidden="true">
                <span>EVIDENCE</span>
                <i />
              </div>
            </div>
            <span className="sp-verification__float" aria-hidden="true">
              <ShieldCheck size={17} /> Independent review
            </span>
            <p className="sp-verification__note">
              Concept preview. Public lookup will show only the details needed for verification.
            </p>
          </div>
        </div>
      </Container>

      <style>{`
        .sp-verification * { box-sizing: border-box; }
        .sp-verification__layout { display: grid; grid-template-columns: minmax(0, .92fr) minmax(0, 1.08fr); align-items: center; gap: clamp(50px, 7vw, 110px); margin-top: clamp(38px, 5vw, 70px); }
        .sp-verification__people { min-width: 0; }
        .sp-verification__label { margin: 0 0 18px; color: var(--brand); font-size: .75rem; font-weight: 750; letter-spacing: .08em; }
        .sp-verification__person { display: flex; align-items: center; gap: 18px; min-height: 98px; border-top: 1px solid var(--border); }
        .sp-verification__person:nth-of-type(3) { border-bottom: 1px solid var(--border); }
        .sp-verification__icon { display: grid; flex: 0 0 44px; place-items: center; width: 44px; height: 44px; border-radius: 13px; color: var(--brand); background: color-mix(in srgb, var(--brand) 11%, transparent); }
        .sp-verification__person h3 { margin: 0 0 4px; color: var(--fg); font-size: 1rem; font-weight: 650; }
        .sp-verification__person p { margin: 0; color: var(--fg-muted); font-size: .9rem; line-height: 1.45; }
        .sp-verification__number { margin-left: auto; color: var(--brand); font-size: .75rem; font-weight: 700; opacity: .5; }
        .sp-verification__visual { position: relative; display: grid; place-items: center; min-height: 520px; padding: 24px 28px 50px; isolation: isolate; }
        .sp-verification__halo { position: absolute; z-index: -1; width: min(100%, 510px); aspect-ratio: 1; border-radius: 50%; background: radial-gradient(circle, color-mix(in srgb, var(--brand) 17%, transparent), transparent 70%); }
        .sp-verification__sheet { width: min(100%, 440px); padding: 27px clamp(25px, 3vw, 38px) 34px; border: 1px solid var(--border); border-radius: 27px; background: var(--surface); box-shadow: var(--shadow-lg); animation: sp-verification-float 6s ease-in-out infinite; }
        .sp-verification__sheet-top { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
        .sp-verification__sheet-top > span { display: flex; align-items: center; gap: 8px; color: var(--fg); font-size: .93rem; font-weight: 800; }
        .sp-verification__sheet-top svg { color: var(--brand); }
        .sp-verification__sheet-top small { color: var(--brand); font-size: .65rem; font-weight: 800; letter-spacing: .06em; text-align: right; }
        .sp-verification__seal { display: grid; place-items: center; width: 86px; height: 86px; margin: 34px auto 23px; border: 2px solid var(--brand); border-radius: 50%; color: var(--brand); background: color-mix(in srgb, var(--brand) 8%, transparent); box-shadow: 0 0 0 10px color-mix(in srgb, var(--brand) 5%, transparent); }
        .sp-verification__sheet h3 { margin: 0; color: var(--fg); font-size: clamp(1.55rem, 2.1vw, 1.95rem); font-weight: 650; line-height: 1.25; letter-spacing: -.02em; text-align: center; }
        .sp-verification__sheet > p { margin: 10px 0 27px; color: var(--fg-muted); font-size: .88rem; text-align: center; }
        .sp-verification__field { display: flex; align-items: center; justify-content: space-between; gap: 20px; min-height: 45px; border-top: 1px solid var(--border); }
        .sp-verification__field span { color: var(--fg-muted); font-size: .71rem; font-weight: 750; letter-spacing: .09em; }
        .sp-verification__field i { width: 42%; height: 6px; border-radius: 99px; background: var(--border); }
        .sp-verification__field:nth-last-child(2) i { width: 25%; }
        .sp-verification__float { position: absolute; top: 48%; right: 0; display: flex; align-items: center; gap: 9px; padding: 13px 16px; border: 1px solid var(--border); border-radius: 13px; background: var(--surface); box-shadow: var(--shadow-md); color: var(--fg); font-size: .8rem; font-weight: 750; animation: sp-verification-float 5s -2s ease-in-out infinite; }
        .sp-verification__float svg { color: var(--brand); }
        .sp-verification__note { position: absolute; bottom: 0; max-width: 420px; margin: 0; color: var(--fg-muted); font-size: .8rem; text-align: center; }
        @keyframes sp-verification-float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }
        @media (max-width: 900px) { .sp-verification__layout { grid-template-columns: 1fr; } .sp-verification__visual { min-height: 500px; } .sp-verification__float { right: 4%; } }
        @media (max-width: 480px) { .sp-verification__visual { padding-inline: 0; } .sp-verification__float { right: 0; font-size: .7rem; } .sp-verification__number { display: none; } }
        @media (prefers-reduced-motion: reduce) { .sp-verification__sheet, .sp-verification__float { animation: none; } }
      `}</style>
    </section>
  )
}
