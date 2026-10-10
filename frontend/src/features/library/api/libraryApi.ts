import { apiClient } from "@/shared/api/apiClient";
import type { LibraryInput, LibraryResource } from "../types/library.types";
const e = encodeURIComponent;
export const libraryApi = {
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
