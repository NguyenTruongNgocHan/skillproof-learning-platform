export interface AssignmentInput {
  ownerScope: "COURSE" | "MODULE" | "LESSON";
  ownerId: string;
  title: string;
  instructions: string;
  required: boolean;
  passPercent: number;
  maxSubmissions: number;
  dueAt: string | null;
}
export interface Assignment extends AssignmentInput {
  id: string;
  versionId: string;
}
export interface Submission {
  id: string;
  assignmentId: string;
  learnerId: string;
  enrollmentId: string;
  status: "DRAFT" | "SUBMITTED" | "GRADED";
  body: string | null;
  link: string | null;
  scorePercent: number | null;
  feedback: string | null;
  passed: boolean | null;
  submittedAt: string | null;
}

export interface RubricCriterion {
  id: string;
  assignmentId: string;
  title: string;
  weight: number;
  position: number;
}
export interface CriterionGrade {
  criterionId: string;
  score: number;
  feedback: string;
}
