export interface EvidenceDocument {
  id: string;
  organizationId: string;
  mediaId: string;
  kind: string;
  purpose: "VERIFICATION" | "ISSUER";
  title: string;
  expiresAt: string | null;
  status: string;
  reason: string | null;
  createdAt: string;
}
export interface IssuerApplication {
  id: string;
  organizationId: string;
  status: string;
  scope: string;
  scopeMode: string;
  courseVersionIds: string;
  evidenceIds: string;
  reason: string | null;
  validUntil: string | null;
  createdAt: string;
}
