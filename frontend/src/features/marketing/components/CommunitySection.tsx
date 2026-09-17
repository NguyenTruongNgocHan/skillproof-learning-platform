import { Shield, Users, CheckCircle, Star, BookOpen, Award } from 'lucide-react';
import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';

export default function CommunitySection() {
  return (
    <section className="py-24 bg-white">
      <Container>
        <SectionHeading
          title="Practice beyond the official curriculum."
          subtitle="Community members create and share practice content. Official certification paths are always clearly distinguished from community content."
          centered
        />

        <div className="grid md:grid-cols-2 gap-8 mb-8">
          {/* Official */}
          <div className="rounded-2xl border-2 border-[#17181C] p-8">
            <div className="flex items-center gap-2 mb-4">
              <div className="flex items-center gap-2 bg-[#17181C] text-white px-3 py-1.5 rounded-lg">
                <Shield size={14} />
                <span className="text-xs font-semibold uppercase tracking-wide">Official</span>
              </div>
            </div>
            <h3 className="font-semibold text-[#17181C] mb-1">Organization-verified learning paths</h3>
            <p className="text-sm text-[#666A73] mb-5">Curricula authored and maintained by approved training organizations.</p>

            <div className="space-y-3">
              {[
                { icon: BookOpen, text: 'Structured curriculum from verified organizations' },
                { icon: Award, text: 'Organization-issued certificates upon completion' },
                { icon: CheckCircle, text: 'Quality guaranteed by the issuing organization' },
              ].map((item) => (
                <div key={item.text} className="flex items-start gap-2.5">
                  <item.icon size={15} color="#FF4F8B" className="mt-0.5 flex-shrink-0" />
                  <span className="text-sm text-[#17181C]">{item.text}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Community */}
          <div className="rounded-2xl border border-[#E5E7EB] p-8 bg-[#F7F8FA]">
            <div className="flex items-center gap-2 mb-4">
              <div className="flex items-center gap-2 bg-[#E5E7EB] text-[#666A73] px-3 py-1.5 rounded-lg">
                <Users size={14} />
                <span className="text-xs font-semibold uppercase tracking-wide">Community</span>
              </div>
            </div>
            <h3 className="font-semibold text-[#17181C] mb-1">Learner-created practice content</h3>
            <p className="text-sm text-[#666A73] mb-5">Quizzes, mock tests, and flashcards shared by the SkillProof community.</p>

            <div className="space-y-3">
              {[
                { icon: BookOpen, text: 'Community quizzes and mock tests' },
                { icon: Star, text: 'Peer-rated content with quality signals' },
                { icon: CheckCircle, text: 'Free and paid options available' },
              ].map((item) => (
                <div key={item.text} className="flex items-start gap-2.5">
                  <item.icon size={15} color="#666A73" className="mt-0.5 flex-shrink-0" />
                  <span className="text-sm text-[#17181C]">{item.text}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="rounded-lg border border-[#E5E7EB] bg-[#F7F8FA] px-5 py-3 text-sm text-[#666A73] text-center">
          Community content does not directly grant official SkillProof certificates. Official certification requires
          completion of an approved learning path from a verified organization.
        </div>
      </Container>
    </section>
  );
}
