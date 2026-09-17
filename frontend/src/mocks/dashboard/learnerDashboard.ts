export interface MockLearningPath {
  id: string;
  title: string;
  organization: string;
  progress: number;
  nextLesson: string;
}

export const CURRENT_LEARNING_PATH: MockLearningPath = {
  id: "1",
  title: "Backend Engineering Path",
  organization: "SkillProof Academy",
  progress: 68,
  nextLesson: "API Design Fundamentals",
};

export interface MockRecommendedPath {
  title: string;
  org: string;
  category: string;
  meta: string;
}

export const RECOMMENDED_PATHS: MockRecommendedPath[] = [
  {
    title: "Node.js API Development",
    org: "SkillProof Academy",
    category: "Backend",
    meta: "12 modules · 8h · Certificate",
  },
  {
    title: "PostgreSQL for Developers",
    org: "DB Institute",
    category: "Databases",
    meta: "10 modules · 6h · Certificate",
  },
  {
    title: "Docker Fundamentals",
    org: "Cloud Academy",
    category: "DevOps",
    meta: "8 modules · 5h · Certificate",
  },
];

export const WEEKLY_GOAL = { target: 5, completed: 3.5, unit: "hours" };

export const CAREER_GOAL = { targetRole: "Backend Developer" };
