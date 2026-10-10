import type {
  Course,
  Enrollment,
  Module,
  Path,
  Progress,
  Resource,
  Version,
} from "@/features/learning/types/learning.types"
import { apiClient } from "@/shared/api/apiClient"
const enc = (id: string) => encodeURIComponent(id)
export const learningApi = {
  discover: () => apiClient.get<Path[]>("/learning/paths"),
  detail: (id: string) => apiClient.get<Path>(`/learning/paths/${enc(id)}`),
  owned: (org: string) => apiClient.get<Path[]>(`/learning/organizations/${enc(org)}/paths`),
  create: (
    org: string,
    data: {
      slug: string
      title: string
      summary: string
    },
  ) => apiClient.post<Path>(`/learning/organizations/${enc(org)}/paths`, data),
  versions: (path: string) => apiClient.get<Version[]>(`/learning/paths/${enc(path)}/versions`),
  clone: (path: string) => apiClient.post<Version>(`/learning/paths/${enc(path)}/versions`),
  outline: (version: string) =>
    apiClient.get<{
      version: Version
      modules: Module[]
      policy: {
        require_all_resources: boolean
        require_official_assessments: boolean
      }
    }>(`/learning/versions/${enc(version)}/outline`),
  reorderModules: (version: string, ids: string[]) =>
    apiClient.patch<void>(`/learning/versions/${enc(version)}/modules/order`, {
      ids,
    }),
  reorderResources: (module: string, ids: string[]) =>
    apiClient.patch<void>(`/learning/modules/${enc(module)}/resources/order`, {
      ids,
    }),
  updatePath: (
    id: string,
    data: {
      title: string
      summary: string
    },
  ) => apiClient.patch<Pick<Path, "id" | "title" | "summary">>(`/learning/paths/${enc(id)}`, data),
  module: (
    version: string,
    data: {
      position: number
      title: string
    },
  ) =>
    apiClient.post<Omit<Module, "resources">>(`/learning/versions/${enc(version)}/modules`, data),
  editModule: (
    id: string,
    data: {
      position: number
      title: string
    },
  ) =>
    apiClient.patch<Pick<Module, "id" | "position" | "title">>(
      `/learning/modules/${enc(id)}`,
      data,
    ),
  deleteModule: (id: string) => apiClient.delete<void>(`/learning/modules/${enc(id)}`),
  resource: (
    module: string,
    data: {
      position: number
      kind: Resource["kind"]
      title: string
      body?: string | null
      url?: string | null
    },
  ) =>
    apiClient.post<Pick<Resource, "id" | "module_id" | "position" | "kind" | "title">>(
      `/learning/modules/${enc(module)}/resources`,
      data,
    ),
  editResource: (
    id: string,
    data: {
      position: number
      kind: Resource["kind"]
      title: string
      body?: string | null
      url?: string | null
    },
  ) =>
    apiClient.patch<Pick<Resource, "id" | "position" | "kind" | "title">>(
      `/learning/resources/${enc(id)}`,
      data,
    ),
  deleteResource: (id: string) => apiClient.delete<void>(`/learning/resources/${enc(id)}`),
  policy: (
    version: string,
    data: {
      requireAllResources: boolean
      requireOfficialAssessments: boolean
    },
  ) => apiClient.put(`/learning/versions/${enc(version)}/policy`, data),
  publish: (version: string) =>
    apiClient.post<Pick<Version, "id" | "status">>(`/learning/versions/${enc(version)}/publish`),
  enroll: (path: string) => apiClient.post<Enrollment>(`/learning/paths/${enc(path)}/enroll`),
  mine: () => apiClient.get<Enrollment[]>("/learning/me/enrollments"),
  course: (enrollment: string) =>
    apiClient.get<Course>(`/learning/me/enrollments/${enc(enrollment)}`),
  complete: (enrollment: string, resource: string) =>
    apiClient.post<Progress>(
      `/learning/me/enrollments/${enc(enrollment)}/resources/${enc(resource)}/complete`,
    ),
}
