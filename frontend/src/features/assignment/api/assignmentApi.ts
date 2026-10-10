import { apiClient } from "@/shared/api/apiClient";
import type {
  Assignment,
  AssignmentInput,
  Submission,
} from "../types/assignment.types";
const e = encodeURIComponent;
export const assignmentApi = {
  list: (version: string) =>
    apiClient.get<Assignment[]>(`/assignments/versions/${e(version)}`),
  create: (version: string, input: AssignmentInput) =>
    apiClient.post<Assignment>(`/assignments/versions/${e(version)}`, input),
  remove: (id: string) => apiClient.delete(`/assignments/${e(id)}`),
  submissions: (id: string) =>
    apiClient.get<Submission[]>(`/assignments/${e(id)}/submissions`),
  grade: (id: string, input: { scorePercent: number; feedback: string }) =>
    apiClient.post<Submission>(
      `/assignments/submissions/${e(id)}/grade`,
      input,
    ),
};
