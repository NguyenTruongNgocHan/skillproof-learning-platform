import { apiClient } from "@/shared/api/apiClient"
import { catalogApi } from "@/features/course/api/catalogApi"
import type { Course, Enrollment, Path, Progress } from "../types/learning.types"
const e = encodeURIComponent
/** Learner compatibility adapter over the frozen Course API. No /learning API aliases exist. */
export const learningApi = {
  discover: async (): Promise<Path[]> =>
    (await catalogApi.list()).map((c) => ({
      id: c.id,
      organization_id: c.organizationId ?? "",
      slug: c.slug,
      title: c.title,
      summary: c.summary,
      version_id: c.versionId,
      version_no: c.versionNo,
    })),
  detail: async (id: string): Promise<Path> => {
    const c = await catalogApi.detail(id)
    return {
      id: c.id,
      organization_id: c.organizationId ?? "",
      slug: c.slug,
      title: c.title,
      summary: c.summary,
      version_id: c.versionId,
      version_no: c.versionNo,
    }
  },
  enroll: (id: string) => catalogApi.enroll(id),
  mine: () => apiClient.get<Enrollment[]>("/courses/me/enrollments"),
  course: (id: string) => apiClient.get<Course>(`/courses/me/enrollments/${e(id)}`),
  complete: (enrollment: string, resource: string) =>
    apiClient.post<Progress>(
      `/courses/me/enrollments/${e(enrollment)}/resources/${e(resource)}/complete`,
    ),
  completeLesson: (enrollment: string, lesson: string) =>
    apiClient.post(`/courses/me/enrollments/${e(enrollment)}/lessons/${e(lesson)}/complete`),
  history: (enrollment: string) =>
    apiClient.get<unknown>(`/courses/me/enrollments/${e(enrollment)}/history`),
}
