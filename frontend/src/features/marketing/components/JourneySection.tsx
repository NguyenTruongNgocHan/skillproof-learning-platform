import type { JourneyStep } from "@/features/marketing/types/marketing.types"
import Container from "@/shared/ui/Container"
import SectionHeading from "@/shared/ui/SectionHeading"

const steps: JourneyStep[] = [
  {
    step: 1,
    title: "Set your goal",
    description: "Define your career objective and target skills",
  },
  {
    step: 2,
    title: "Follow a Learning Path",
    description: "Work through structured modules, resources and practice content",
  },
  {
    step: 3,
    title: "Practice and compete",
    description: "Reinforce understanding through quizzes and realtime 1v1 challenges",
  },
  {
    step: 4,
    title: "Complete assessment",
    description: "Demonstrate mastery through proctored assessments",
  },
  {
    step: 5,
    title: "Earn your certificate",
    description: "An authorized organizer may issue a certificate after requirements are met",
  },
  {
    step: 6,
    title: "Share and verify",
    description: "Share your issued credential for independent verification",
  },
]

export default function JourneySection() {
  return (
    <section className="py-24 bg-skin sp-journey" aria-labelledby="sp-journey-heading">
      <Container>
        <div id="sp-journey-heading">
          <SectionHeading
            eyebrow="THE SKILLPROOF JOURNEY"
            title="From learning goal to verified achievement."
            centered
          />
        </div>

        <div className="sp-journey__track">
          <ol className="sp-journey__steps">
            {steps.map((step) => (
              <li className="sp-journey__step" key={step.step}>
                <span className="sp-journey__number" aria-label={`Step ${step.step}`}>
                  <span>{String(step.step).padStart(2, "0")}</span>
                </span>
                <div className="sp-journey__text">
                  <h3>{step.title}</h3>
                  <p>{step.description}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </Container>

      <style>{`
        .sp-journey__track {
          position: relative;
          margin: 48px 0 0;
        }
        .sp-journey__steps {
          display: grid;
          grid-template-columns: repeat(6, minmax(0, 1fr));
          gap: 0;
          margin: 0;
          padding: 0;
          list-style: none;
        }
        .sp-journey__track::before {
          content: "";
          position: absolute;
          top: 19px;
          left: 8.333%;
          right: 8.333%;
          height: 2px;
          background: var(--border);
          opacity: .65;
        }
        .sp-journey__step {
          position: relative;
          display: flex;
          flex-direction: column;
          align-items: center;
          min-width: 0;
          padding: 0 10px;
          text-align: center;
        }
        .sp-journey__number {
          position: relative;
          z-index: 2;
          display: grid;
          flex: none;
          place-items: center;
          width: 40px;
          height: 40px;
          margin-bottom: 15px;
          border-radius: 72% 0 72% 72%;
          background: var(--brand);
          color: #fff;
          font-size: .8rem;
          font-weight: 700;
          box-shadow: 0 0 0 5px var(--bg, transparent),
                      0 7px 14px color-mix(in srgb, var(--brand) 15%, transparent);
          animation: sp-journey-bob 4.2s ease-in-out infinite;
        }
        .sp-journey__step:nth-child(2n) .sp-journey__number { animation-delay: -.8s; }
        .sp-journey__step:nth-child(3n) .sp-journey__number { animation-delay: -1.7s; }
        .sp-journey__number > span { transform: rotate(45deg); }
        .sp-journey__text h3 {
          margin: 0 0 6px;
          color: var(--fg);
          font-size: .875rem;
          font-weight: 650;
          line-height: 1.35;
        }
        .sp-journey__text p {
          max-width: 170px;
          margin: 0 auto;
          color: var(--fg-muted);
          font-size: .76rem;
          line-height: 1.55;
        }
        @keyframes sp-journey-bob {
          0%, 100% { transform: translateY(0) rotate(-45deg); }
          45% { transform: translateY(-4px) rotate(-42deg); }
          70% { transform: translateY(-2px) rotate(-48deg); }
        }
        @media (max-width: 1023px) {
          .sp-journey__track {
            margin-top: 38px;
          }
          .sp-journey__steps {
            grid-template-columns: 1fr;
            gap: 0;
          }
          .sp-journey__track::before {
            top: 19px;
            bottom: 19px;
            left: 19px;
            right: auto;
            width: 2px;
            height: auto;
          }
          .sp-journey__step {
            flex-direction: row;
            align-items: flex-start;
            gap: 18px;
            min-height: 104px;
            padding: 0;
            text-align: left;
          }
          .sp-journey__number { margin: 0; }
          .sp-journey__text { padding-top: 2px; }
          .sp-journey__text p { max-width: 440px; margin: 0; font-size: .875rem; }
        }
        @media (prefers-reduced-motion: reduce) {
          .sp-journey__number { animation: none; transform: rotate(-45deg); }
        }
      `}</style>
    </section>
  )
}
