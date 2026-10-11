import { apiClient } from "@/shared/api/apiClient";
import type { CatalogCourse, SyllabusModule } from "../types/catalog.types";
export const catalogApi = {
  list: (page = 0) =>
    apiClient.get<CatalogCourse[]>(`/courses?page=${page}&size=20`),
  detail: (id: string) =>
    apiClient.get<CatalogCourse>(`/courses/${encodeURIComponent(id)}`),
  syllabus: (id: string) =>
    apiClient.get<SyllabusModule[]>(
      `/courses/${encodeURIComponent(id)}/syllabus`,
    ),
  enroll: (id: string) =>
    apiClient.post<{ id: string }>(`/courses/${encodeURIComponent(id)}/enroll`),
};
