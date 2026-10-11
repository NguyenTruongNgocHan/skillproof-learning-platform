import { apiClient } from "@/shared/api/apiClient";
import type { LibraryInput, LibraryResource } from "../types/library.types";
const e = encodeURIComponent;
export const libraryApi = {
  reviewQueue: (page: number) =>
    apiClient.get<LibraryResource[]>(`/admin/library?page=${page}&size=20`),
  reviewDetail: (id: string) =>
    apiClient.get<LibraryResource>(`/admin/library/${e(id)}`),
  review: (id: string, approved: boolean, reason: string) =>
    apiClient.post(`/admin/library/${e(id)}/review`, { approved, reason }),
  list: (org: string) =>
    apiClient.get<LibraryResource[]>(`/library/organizations/${e(org)}`),
  create: (org: string, input: LibraryInput) =>
    apiClient.post<LibraryResource>(`/library/organizations/${e(org)}`, input),
  edit: (id: string, input: LibraryInput) =>
    apiClient.put<LibraryResource>(`/library/${e(id)}`, {
      title: input.title,
      summary: input.summary,
      body: input.body,
      priceVnd: input.priceVnd,
    }),
  submit: (id: string) => apiClient.post(`/library/${e(id)}/submit-review`),
};
