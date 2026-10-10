import type {
  Assessment,
  AssessmentQuestion,
  AttemptHistory,
  AttemptView,
  Bank,
  Choice,
  Kind,
  Question,
  Result,
} from "@/features/quiz/types/quiz.types";
import { apiClient } from "@/shared/api/apiClient";
const enc = (s: string) => encodeURIComponent(s);
export const quizApi = {
  owner: (
    id: string,
    input: {
      scope: "COURSE" | "MODULE" | "LESSON";
      ownerId: string;
      required: boolean;
    },
  ) => apiClient.put(`/quiz/assessments/${enc(id)}/owner`, input),
  banks: (org: string) =>
    apiClient.get<Bank[]>(`/quiz/organizations/${enc(org)}/banks`),
  createBank: (org: string, title: string) =>
    apiClient.post<Bank>(`/quiz/organizations/${enc(org)}/banks`, { title }),
  questions: (bank: string) =>
    apiClient.get<Question[]>(`/quiz/banks/${enc(bank)}/questions`),
  question: (id: string) =>
    apiClient.get<{
      question: Question;
      version: Question;
      options: Choice[];
    }>(`/quiz/questions/${enc(id)}`),
  addQuestion: (
    bank: string,
    data: {
      stem: string;
      options: {
        body: string;
        correct: boolean;
      }[];
    },
  ) =>
    apiClient.post<{
      question: Question;
      version: Question;
      options: Choice[];
    }>(`/quiz/banks/${enc(bank)}/questions`, data),
  reviseQuestion: (
    id: string,
    data: {
      stem: string;
      options: {
        body: string;
        correct: boolean;
      }[];
    },
  ) => apiClient.post(`/quiz/questions/${enc(id)}/versions`, data),
  assessments: (version: string) =>
    apiClient.get<Assessment[]>(`/quiz/versions/${enc(version)}/assessments`),
  createAssessment: (
    version: string,
    data: {
      kind: Kind;
      title: string;
      durationSeconds: number;
      passPercent: number;
      maxAttempts: number;
    },
  ) =>
    apiClient.post<Assessment>(
      `/quiz/versions/${enc(version)}/assessments`,
      data,
    ),
  editAssessment: (
    id: string,
    data: {
      title: string;
      durationSeconds: number;
      passPercent: number;
      maxAttempts: number;
    },
  ) => apiClient.patch<Assessment>(`/quiz/assessments/${enc(id)}`, data),
  assessmentQuestions: (id: string) =>
    apiClient.get<AssessmentQuestion[]>(
      `/quiz/assessments/${enc(id)}/questions`,
    ),
  attach: (
    assessment: string,
    questionVersionId: string,
    position: number,
    points: number,
  ) =>
    apiClient.post<void>(`/quiz/assessments/${enc(assessment)}/questions`, {
      questionVersionId,
      position,
      points,
    }),
  detach: (assessment: string, question: string) =>
    apiClient.delete<void>(
      `/quiz/assessments/${enc(assessment)}/questions/${enc(question)}`,
    ),
  deleteAssessment: (id: string) =>
    apiClient.delete<void>(`/quiz/assessments/${enc(id)}`),
  publish: (id: string) =>
    apiClient.post<Assessment>(`/quiz/assessments/${enc(id)}/publish`),
  available: (enrollment: string) =>
    apiClient.get<Assessment[]>(
      `/quiz/me/enrollments/${enc(enrollment)}/assessments`,
    ),
  history: (enrollment: string) =>
    apiClient.get<AttemptHistory[]>(
      `/quiz/me/enrollments/${enc(enrollment)}/attempts`,
    ),
  start: (assessment: string, enrollmentId: string) =>
    apiClient.post<AttemptView>(
      `/quiz/assessments/${enc(assessment)}/attempts`,
      { enrollmentId },
    ),
  resume: (attempt: string) =>
    apiClient.get<AttemptView | Result>(`/quiz/attempts/${enc(attempt)}`),
  answer: (attempt: string, questionVersionId: string, optionId: string) =>
    apiClient.put<AttemptView | Result>(
      `/quiz/attempts/${enc(attempt)}/answers`,
      {
        questionVersionId,
        optionId,
      },
    ),
  submit: (attempt: string) =>
    apiClient.post<Result>(`/quiz/attempts/${enc(attempt)}/submit`),
  result: (attempt: string) =>
    apiClient.get<Result>(`/quiz/attempts/${enc(attempt)}/result`),
};
