import { ArrowRight } from 'lucide-react';
import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';
import type { JourneyStep } from '@/types';

const steps: JourneyStep[] = [
  { step: 1, title: 'Set your goal', description: 'Define your career objective and target skills' },
  { step: 2, title: 'Follow a Learning Path', description: 'Work through structured modules, resources and practice content' },
  { step: 3, title: 'Practice and compete', description: 'Reinforce understanding through quizzes and realtime 1v1 challenges' },
  { step: 4, title: 'Complete assessment', description: 'Demonstrate mastery through proctored assessments' },
  { step: 5, title: 'Earn your certificate', description: 'Certificates are issued by approved organizations after requirements are met' },
  { step: 6, title: 'Share and verify', description: 'Share your credential and let employers independently verify it' },
];

export default function JourneySection() {
  return (
    <section className="py-24 bg-white">
      <Container>
        <SectionHeading
          eyebrow="THE SKILLPROOF JOURNEY"
          title="From learning goal to verified achievement."
          centered
        />

        {/* Desktop: horizontal */}
        <div className="hidden lg:flex items-start gap-0">
          {steps.map((step, i) => (
            <div key={step.step} className="flex items-start flex-1">
              <div className="flex flex-col items-center text-center flex-1 px-3">
                <div
                  className="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm mb-3 flex-shrink-0"
                  style={{ backgroundColor: '#FF4F8B' }}
                >
                  {String(step.step).padStart(2, '0')}
                </div>
                <h3 className="font-semibold text-sm text-[#17181C] mb-1">{step.title}</h3>
                <p className="text-xs text-[#666A73] leading-relaxed">{step.description}</p>
              </div>
              {i < steps.length - 1 && (
                <div className="flex-shrink-0 mt-4">
                  <ArrowRight size={16} color="#E5E7EB" />
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Mobile: vertical */}
        <div className="lg:hidden space-y-6">
          {steps.map((step) => (
            <div key={step.step} className="flex gap-4">
              <div
                className="w-9 h-9 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0 mt-0.5"
                style={{ backgroundColor: '#FF4F8B' }}
              >
                {String(step.step).padStart(2, '0')}
              </div>
              <div>
                <h3 className="font-semibold text-[#17181C] mb-1">{step.title}</h3>
                <p className="text-sm text-[#666A73] leading-relaxed">{step.description}</p>
              </div>
            </div>
          ))}
        </div>
      </Container>
    </section>
  );
}
