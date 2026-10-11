import { apiClient } from "@/shared/api/apiClient";
export interface Branding {
  logoMediaId?: string | null;
  coverMediaId?: string | null;
  updatedAt?: string;
}
export interface PublicOrganization {
  id: string;
  displayName: string;
  legalName: string;
  industry: string;
  country: string;
  website: string | null;
  status: "APPROVED";
  branding: Branding;
}
export const publicImage = (path: string) =>
  `${import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1"}${path}`;
export const brandingApi = {
  get: (org: string) =>
    apiClient.get<Branding>(
      `/organizations/${encodeURIComponent(org)}/branding`,
    ),
  save: (org: string, input: Branding) =>
    apiClient.put(`/organizations/${encodeURIComponent(org)}/branding`, {
      logoMediaId: input.logoMediaId || null,
      coverMediaId: input.coverMediaId || null,
    }),
  publicProfile: (org: string) =>
    apiClient.get<PublicOrganization>(
      `/public/organizations/${encodeURIComponent(org)}`,
    ),
};
