export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Program {
  id: string;
  organizationId: string;
  courseVersionId: string;
  completionPolicyId: string;
  name: string;
  status: "DRAFT" | "ACTIVE" | "RETIRED";
  createdAt: string;
}

export interface Enrollment {
  id: string;
  learnerId: string;
  learnerEmail: string;
  pathVersionId: string;
  versionNo: number;
  pathTitle: string;
  status: string;
  enrolledAt: string;
}

export interface Eligibility {
  id: string;
  certificationProgramId: string;
  learnerUserId: string;
  enrollmentId: string;
  status: "ELIGIBLE" | "NOT_ELIGIBLE";
  evidenceSnapshotJson: string;
  evaluatedAt: string;
}

export interface Certificate {
  id: string;
  certificationProgramId: string;
  learnerUserId: string;
  serialNumber: string;
  status: "ISSUED" | "REVOKED";
  issuedAt: string;
  revokedAt: string | null;
  revocationReason: string | null;
  organizationId: string | null;
  programName: string | null;
  courseVersionId: string | null;
  issuerName: string | null;
  learnerEmail: string | null;
}

export interface PublicCertificate {
  serialNumber: string;
  status: "ISSUED" | "REVOKED";
  issuedAt: string;
  revokedAt: string | null;
  issuerName: string | null;
  programName: string | null;
  courseVersionId: string | null;
  proofStatus: "NOT_ANCHORED";
}
