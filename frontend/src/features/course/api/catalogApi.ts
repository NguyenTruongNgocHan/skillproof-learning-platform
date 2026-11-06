import { searchParams, type PageResult } from "@/shared/api/page"
import { apiClient } from "@/shared/api/apiClient"
import type { CatalogCourse, SyllabusModule } from "../types/catalog.types"
export const catalogApi = {
  search: (query: string, domain: string, difficulty: string, page: number) =>
    apiClient.get<PageResult<CatalogCourse>>(
      `/courses/search?${searchParams({ query, domain, difficulty, page, size: 20 })}`,
    ),
  list: (page = 0) => apiClient.get<CatalogCourse[]>(`/courses?page=${page}&size=20`),
  detail: (id: string) => apiClient.get<CatalogCourse>(`/courses/${encodeURIComponent(id)}`),
  syllabus: (id: string) =>
    apiClient.get<SyllabusModule[]>(`/courses/${encodeURIComponent(id)}/syllabus`),
  enroll: (id: string) =>
    apiClient.post<{ id: string }>(`/courses/${encodeURIComponent(id)}/enroll`),
}
