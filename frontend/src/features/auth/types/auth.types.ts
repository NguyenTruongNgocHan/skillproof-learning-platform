export type UserRole = 'LEARNER' | 'ORGANIZER' | 'ADMIN';
export type EmailVerificationStatus = 'UNVERIFIED' | 'VERIFIED';
export type OnboardingStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
export type OrganizerVerificationStatus = 'NOT_SUBMITTED' | 'PENDING' | 'VERIFIED' | 'REJECTED';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  emailVerificationStatus: EmailVerificationStatus;
  onboardingStatus: OnboardingStatus;
}

export interface OrganizerProfile {
  verificationStatus: OrganizerVerificationStatus;
  organizationName?: string;
  jobTitle?: string;
  organizationId?: string;
}

export interface LearnerProfile {
  careerGoal?: string;
  targetRole?: string;
  skillLevel?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  weeklyGoal?: number;
  learningPreferences?: string[];
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterLearnerData {
  fullName: string;
  email: string;
  password: string;
}

export interface RegisterOrganizerData {
  fullName: string;
  email: string;
  password: string;
  jobTitle: string;
  organizationName: string;
  organizationWebsite: string;
  country: string;
}

export interface AuthResult {
  user: User;
}
