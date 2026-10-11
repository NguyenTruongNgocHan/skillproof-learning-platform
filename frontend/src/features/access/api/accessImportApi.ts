import { apiClient } from "@/shared/api/apiClient";
import type {
  AccessImportInput,
  ImportRow,
  ImportResult,
} from "../types/accessImport.types";
export const accessImportApi = {
  preview: (org: string, input: AccessImportInput) =>
    apiClient.post<ImportRow[]>(
      `/access/organizations/${encodeURIComponent(org)}/imports/preview`,
      input,
    ),
  apply: (org: string, input: AccessImportInput) =>
    apiClient.post<ImportResult[]>(
      `/access/organizations/${encodeURIComponent(org)}/imports/apply`,
      input,
      undefined,
      300000,
    ),
};
