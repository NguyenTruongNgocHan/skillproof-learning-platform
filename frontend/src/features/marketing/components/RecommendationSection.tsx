import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';

const chips = [
  { label: 'Python', active: true },
  { label: 'APIs', active: true },
  { label: 'Cloud', active: false },
  { label: 'Databases', active: true },
];

const recommendations = [
  { title: 'Backend Engineering Foundations', match: 96, meta: '8 weeks • 6 modules • Certificate' },
  { title: 'API Design Professional', match: 88, meta: '4 weeks • 4 modules' },
  { title: 'Database Systems Fundamentals', match: 81, meta: '5 weeks • 5 modules' },
];

export default function RecommendationSection() {
  return (
    <section className="py-24" style={{ backgroundColor: '#F7F8FA' }}>
      <Container>
        <SectionHeading
          title="A learning path that starts with your goal."
          subtitle="SkillProof maps your career goal and current skill level to the most relevant structured learning paths and certification programs."
        />

        <div className="grid md:grid-cols-2 gap-10 lg:gap-16">
          {/* Left: form card */}
          <div className="bg-white rounded-2xl border border-[#E5E7EB] p-6 flex flex-col gap-5">
            <div>
              <label className="block text-xs font-medium text-[#666A73] mb-1.5">What is your goal?</label>
              <div className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2.5 text-sm text-[#17181C] bg-[#F7F8FA] flex items-center justify-between">
                <span>Become a Backend Engineer</span>
                <span className="text-[#666A73]">▾</span>
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-[#666A73] mb-1.5">Current level</label>
              <div className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2.5 text-sm text-[#17181C] bg-[#F7F8FA] flex items-center justify-between">
                <span>Some programming experience</span>
                <span className="text-[#666A73]">▾</span>
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-[#666A73] mb-1.5">Time available</label>
              <div className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2.5 text-sm text-[#17181C] bg-[#F7F8FA] flex items-center justify-between">
                <span>5–10 hours/week</span>
                <span className="text-[#666A73]">▾</span>
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-[#666A73] mb-2">Your interests</label>
              <div className="flex flex-wrap gap-2">
                {chips.map((chip) => (
                  <span
                    key={chip.label}
                    className="px-3 py-1 rounded-full text-xs font-medium border transition-colors cursor-pointer"
                    style={
                      chip.active
                        ? { backgroundColor: '#FFF0F5', color: '#FF4F8B', borderColor: '#FF4F8B' }
                        : { backgroundColor: 'white', color: '#666A73', borderColor: '#E5E7EB' }
                    }
                  >
                    {chip.label}
                  </span>
                ))}
              </div>
            </div>

            <Button variant="primary" size="md" className="w-full justify-center mt-2">
              Find My Learning Path
            </Button>
          </div>

          {/* Right: recommendations */}
          <div className="flex flex-col gap-4">
            <p className="text-sm font-medium text-[#17181C] mb-1">Recommended paths</p>
            {recommendations.map((rec) => (
              <div
                key={rec.title}
                className="bg-white rounded-xl border border-[#E5E7EB] p-5 flex items-start justify-between hover:border-[#FF4F8B] transition-colors"
              >
                <div>
                  <h4 className="font-semibold text-sm text-[#17181C] mb-1">{rec.title}</h4>
                  <p className="text-xs text-[#666A73]">{rec.meta}</p>
                </div>
                <Badge variant="brand" className="flex-shrink-0 ml-4">{rec.match}% match</Badge>
              </div>
            ))}
          </div>
        </div>
      </Container>
    </section>
  );
}
