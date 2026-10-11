import { apiClient } from "@/shared/api/apiClient";
import type {
  RubricCriterion,
  CriterionGrade,
  Assignment,
  AssignmentInput,
  Submission,
} from "../types/assignment.types";
const e = encodeURIComponent;
export const assignmentApi = {
  rubric: (id: string) =>
    apiClient.get<RubricCriterion[]>(`/assignments/${e(id)}/rubric`),
  saveRubric: (id: string, criteria: { title: string; weight: number }[]) =>
    apiClient.put<RubricCriterion[]>(`/assignments/${e(id)}/rubric`, {
      criteria,
    }),
  rubricGrade: (id: string, criteria: CriterionGrade[], feedback: string) =>
    apiClient.post<Submission>(
      `/assignments/submissions/${e(id)}/rubric-grade`,
      { criteria, feedback },
    ),
  criterionGrades: (id: string) =>
    apiClient.get<CriterionGrade[]>(
      `/assignments/submissions/${e(id)}/rubric-grade`,
    ),
  list: (version: string) =>
    apiClient.get<Assignment[]>(`/assignments/versions/${e(version)}`),
  create: (version: string, input: AssignmentInput) =>
    apiClient.post<Assignment>(`/assignments/versions/${e(version)}`, input),
  edit: (id: string, input: AssignmentInput) =>
    apiClient.put<Assignment>(`/assignments/${e(id)}`, input),
  remove: (id: string) => apiClient.delete(`/assignments/${e(id)}`),
  submissions: (id: string) =>
    apiClient.get<Submission[]>(`/assignments/${e(id)}/submissions`),
  grade: (id: string, input: { scorePercent: number; feedback: string }) =>
    apiClient.post<Submission>(
      `/assignments/submissions/${e(id)}/grade`,
      input,
    ),
};
