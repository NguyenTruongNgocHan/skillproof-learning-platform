export interface LibraryInput {
  title: string;
  summary: string;
  body: string;
  priceVnd: number;
  accessMode: "PUBLIC" | "RESTRICTED";
}
export interface LibraryResource extends LibraryInput {
  id: string;
  organizationId: string | null;
  status: "DRAFT" | "SUBMITTED" | "PUBLISHED" | "REJECTED";
  reviewReason: string | null;
  createdAt: string;
}
