import { apiClient } from "@/shared/api/apiClient";
export interface Readiness {
  ready: boolean;
  status: string;
  issues: { code: string; tab: string; targetId?: string; message: string }[];
}
export interface EnrollmentRow {
  id: string;
  learnerId: string;
  email: string;
  status: string;
  enrolledAt: string;
  completedAt: string | null;
  retainedFreeAccess: boolean;
  completedResources: number;
  totalResources: number;
}
export const workspaceCourseApi = {
  directory: (org: string) =>
    apiClient.get<
      { id: string; courseId: string; title: string; status: string }[]
    >(`/course-workspace/organizations/${encodeURIComponent(org)}/versions`),
  enrollment: (id: string, enrollment: string) =>
    apiClient.get<EnrollmentDetail>(
      `/course-workspace/versions/${encodeURIComponent(id)}/enrollments/${encodeURIComponent(enrollment)}`,
    ),
  readiness: (id: string) =>
    apiClient.get<Readiness>(
      `/course-workspace/versions/${encodeURIComponent(id)}/readiness`,
    ),
  enrollments: (
    id: string,
    query = "",
    page = 0,
    status = "",
    start = "1970-01-01T00:00:00Z",
    end = "9999-12-31T00:00:00Z",
  ) =>
    apiClient.get<{
      content: EnrollmentRow[];
      totalPages: number;
      totalElements: number;
    }>(
      `/course-workspace/versions/${encodeURIComponent(id)}/enrollments?${new URLSearchParams({ query, page: String(page), status, start, end })}`,
    ),
};

export interface EnrollmentDetail {
  enrollmentId: string;
  learnerId: string;
  email: string;
  status: string;
  enrolledAt: string;
  completedAt: string | null;
  retainedFreeAccess: boolean;
  accessHistory: {
    id: string;
    type: string;
    source: string;
    cohort: string;
    createdAt: string;
    expiresAt: string | null;
    revokedAt: string | null;
  }[];
  certificates: {
    id: string;
    serial: string;
    status: string;
    program: string;
    issuedAt: string;
    revokedAt: string | null;
    reason: string | null;
  }[];
  completionEvidenceJson: string | null;
  outline: {
    title: string;
    lessons: { id: string; title: string; completed: boolean }[];
    resources: {
      id: string;
      title: string;
      kind: string;
      required: boolean;
      completedAt: string | null;
    }[];
  }[];
  assessments: {
    requiredOfficialAssessments: number;
    passedOfficialAssessments: number;
  };
  assignments: { required: number; passed: number };
  assessmentHistory: {
    id: string;
    assessmentId: string;
    title: string;
    kind: string;
    required: boolean;
    status: string;
    scorePercent: number | null;
    passed: boolean | null;
    startedAt: string;
    deadlineAt: string;
    submittedAt: string | null;
  }[];
  assignmentHistory: {
    id: string;
    title: string;
    required: boolean;
    passPercent: number;
    maxSubmissions: number;
    submissions: {
      id: string;
      status: string;
      scorePercent: number | null;
      passed: boolean | null;
      submittedAt: string | null;
      gradedAt: string | null;
      feedback: string | null;
      criterionGrades: {
        title: string;
        criterionId: string;
        score: number;
        feedback: string;
      }[];
    }[];
  }[];
}
