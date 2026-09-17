import type { LearningPath } from '@/types';

export const landingLearningPaths: LearningPath[] = [
  {
    id: 'backend-engineering-foundations',
    title: 'Backend Engineering Foundations',
    organization: 'SkillProof Academy',
    level: 'Intermediate',
    duration: '8 weeks',
    modules: 6,
    hasCertification: true,
    enrolledCount: 2841,
    category: 'Engineering',
  },
  {
    id: 'software-testing-professional',
    title: 'Software Testing Professional',
    organization: 'QA Institute',
    level: 'Beginner → Intermediate',
    duration: '6 weeks',
    modules: 5,
    hasCertification: true,
    enrolledCount: 1203,
    category: 'Quality Assurance',
  },
  {
    id: 'cloud-infrastructure-fundamentals',
    title: 'Cloud Infrastructure Fundamentals',
    organization: 'Cloud Training Co',
    level: 'Intermediate',
    duration: '10 weeks',
    modules: 8,
    hasCertification: true,
    enrolledCount: 3412,
    category: 'Cloud',
  },
];
