import { Link } from 'react-router-dom';
import { Clock, BookOpen, Award, Users } from 'lucide-react';
import Container from '@/components/ui/Container';
import SectionHeading from '@/components/ui/SectionHeading';
import Badge from '@/components/ui/Badge';
import { landingLearningPaths } from '@/data/landingLearningPaths';
import type { LearningPath } from '@/types';

function PathCard({ path }: { path: LearningPath }) {
  return (
    <div
      className="bg-white rounded-xl border border-[#E5E7EB] p-6 flex flex-col gap-4 hover:border-[#FF4F8B] transition-colors cursor-pointer"
    >
      <div>
        <h3 className="font-semibold text-lg text-[#17181C] mb-1">{path.title}</h3>
        <p className="text-xs text-[#666A73]">{path.organization}</p>
      </div>

      <div className="flex flex-wrap gap-2">
        <Badge variant="default">{path.level}</Badge>
        <span className="inline-flex items-center gap-1 text-xs text-[#666A73]">
          <Clock size={12} /> {path.duration}
        </span>
        <span className="inline-flex items-center gap-1 text-xs text-[#666A73]">
          <BookOpen size={12} /> {path.modules} modules
        </span>
      </div>

      {path.hasCertification && (
        <Badge variant="brand" className="self-start">
          <Award size={11} className="mr-1" /> Certificate Available
        </Badge>
      )}

      <p className="text-xs text-[#666A73] flex items-center gap-1 mt-auto">
        <Users size={12} /> {path.enrolledCount.toLocaleString()} enrolled
      </p>
    </div>
  );
}

export default function LearningPathsSection() {
  return (
    <section className="py-24 bg-white">
      <Container>
        <SectionHeading
          title="Structured learning that leads somewhere."
          subtitle="Each learning path connects curated resources, practice assessments, and certification milestones into a coherent progression — not a random collection of videos."
        />

        <div className="grid md:grid-cols-3 gap-6 mb-10">
          {landingLearningPaths.map((path) => (
            <PathCard key={path.id} path={path} />
          ))}
        </div>

        <div className="text-center">
          <Link
            to="/learning-paths"
            className="text-sm font-medium hover:underline"
            style={{ color: '#FF4F8B' }}
          >
            Browse all learning paths →
          </Link>
        </div>
      </Container>
    </section>
  );
}
