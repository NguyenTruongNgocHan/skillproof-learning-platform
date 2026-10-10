import { apiClient } from "@/shared/api/apiClient";
import type {
  CourseRecord,
  CourseVersion,
  CourseOutline,
  Lesson,
  ResourceInput,
} from "../types/course.types";
const e = encodeURIComponent;
export const courseApi = {
  list: (org: string) =>
    apiClient.get<CourseRecord[]>(`/courses/organizations/${e(org)}`),
  create: (
    org: string,
    input: { slug: string; title: string; summary: string },
  ) => apiClient.post<CourseRecord>(`/courses/organizations/${e(org)}`, input),
  edit: (id: string, input: { title: string; summary: string }) =>
    apiClient.patch(`/courses/${e(id)}`, input),
  versions: (id: string) =>
    apiClient.get<CourseVersion[]>(`/courses/${e(id)}/versions`),
  clone: (id: string) =>
    apiClient.post<CourseVersion>(`/courses/${e(id)}/versions`),
  outline: (id: string) =>
    apiClient.get<CourseOutline>(`/courses/versions/${e(id)}/outline`),
  module: (id: string, input: { position: number; title: string }) =>
    apiClient.post(`/courses/versions/${e(id)}/modules`, input),
  editModule: (id: string, input: { position: number; title: string }) =>
    apiClient.patch(`/courses/modules/${e(id)}`, input),
  deleteModule: (id: string) => apiClient.delete(`/courses/modules/${e(id)}`),
  lesson: (
    id: string,
    input: { position: number; title: string; body: string },
  ) => apiClient.post<Lesson>(`/courses/modules/${e(id)}/lessons`, input),
  editLesson: (
    id: string,
    input: { position: number; title: string; body: string },
  ) => apiClient.patch<Lesson>(`/courses/lessons/${e(id)}`, input),
  deleteLesson: (id: string) => apiClient.delete(`/courses/lessons/${e(id)}`),
  resource: (id: string, input: ResourceInput) =>
    apiClient.post(`/courses/lessons/${e(id)}/resources`, input),
  editResource: (id: string, input: ResourceInput) =>
    apiClient.patch(`/courses/resources/${e(id)}`, {
      position: input.position,
      kind: input.kind,
      title: input.title,
      body: input.body,
      url: input.url,
    }),
  deleteResource: (id: string) =>
    apiClient.delete(`/courses/resources/${e(id)}`),
  orderModules: (id: string, ids: string[]) =>
    apiClient.put(`/courses/versions/${e(id)}/module-order`, { ids }),
  orderLessons: (id: string, ids: string[]) =>
    apiClient.put(`/courses/modules/${e(id)}/lesson-order`, { ids }),
  orderResources: (id: string, ids: string[]) =>
    apiClient.put(`/courses/lessons/${e(id)}/resource-order`, { ids }),
  policy: (
    id: string,
    input: {
      requireAllResources: boolean;
      requireOfficialAssessments: boolean;
    },
  ) => apiClient.put(`/courses/versions/${e(id)}/policy`, input),
  offer: (
    id: string,
    input: {
      accessMode: string;
      priceVnd: number;
      certificationPriceVnd: number;
    },
  ) => apiClient.put(`/courses/versions/${e(id)}/offer`, input),
  publish: (id: string) => apiClient.post(`/courses/versions/${e(id)}/publish`),
};
