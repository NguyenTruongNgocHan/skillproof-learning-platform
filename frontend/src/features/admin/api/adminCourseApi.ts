import { apiClient } from "@/shared/api/apiClient"
import { searchParams, type PageResult } from "@/shared/api/page"
export interface ModeratedCourse {
  id: string
  organizationId: string | null
  slug: string
  title: string
  suspended: boolean
  moderationReason: string | null
}
export const adminCourseApi = {
  search: (query: string, suspended: string, page: number) =>
    apiClient.get<PageResult<ModeratedCourse>>(
      `/admin/courses/search?${searchParams({ query, suspended: suspended || undefined, page, size: 20 })}`,
    ),
  detail: (id: string) =>
    apiClient.get<ModeratedCourse>(`/admin/courses/${encodeURIComponent(id)}`),
  moderate: (id: string, suspended: boolean, reason: string) =>
    apiClient.post(`/admin/courses/${encodeURIComponent(id)}/moderation`, { suspended, reason }),
}
