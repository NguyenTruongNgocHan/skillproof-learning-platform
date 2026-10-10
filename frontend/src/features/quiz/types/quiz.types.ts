export type Kind = "PRACTICE" | "MOCK" | "OFFICIAL";

export interface Bank {
  id: string;
  organization_id: string;
  title: string;
}

export interface Question {
  id: string;
  version_id: string;
  version_no: number;
  stem: string;
}

export interface Choice {
  id: string;
  body: string;
  position: number;
  correct?: boolean;
}

export interface Assessment {
  ownerScope?: "COURSE" | "MODULE" | "LESSON";
  ownerId?: string;
  required?: boolean;
  id: string;
  version_id: string;
  kind: Kind;
  title: string;
  status: "DRAFT" | "PUBLISHED";
  duration_seconds: number;
  pass_percent: number;
  max_attempts: number;
}

export interface AssessmentQuestion {
  question_version_id: string;
  stem: string;
  position: number;
  points: number;
}

export interface Attempt {
  id: string;
  assessment_id: string;
  enrollment_id: string;
  kind: Kind;
  title: string;
  status: string;
  deadline_at: string;
  started_at: string;
}

export interface AttemptQuestion {
  question_version_id: string;
  stem: string;
  position: number;
  points: number;
  selected_option_id: string | null;
  options: Choice[];
}

export interface AttemptView {
  attempt: Attempt;
  questions: AttemptQuestion[];
}

export interface Result {
  attemptId: string;
  assessmentId: string;
  kind: Kind;
  scorePercent: number;
  passed: boolean;
  timedOut: boolean;
}

export interface AttemptHistory {
  id: string;
  assessment_id: string;
  title: string;
  kind: Kind;
  status: string;
  score_percent: number | null;
  passed: boolean | null;
  started_at: string;
  deadline_at: string;
  submitted_at: string | null;
}
