export type OrganizationStatus =
  | "DRAFT"
  | "PENDING"
  | "APPROVED"
  | "NEEDS_INFORMATION"
  | "REJECTED"
  | "SUSPENDED";

export type Authority =
  | "MANAGE_PROFILE"
  | "MANAGE_MEMBERS"
  | "MANAGE_CONTENT"
  | "ISSUE_CERTIFICATES"
  | "VIEW_ANALYTICS"
  | "MANAGE_PRICING"
  | "MANAGE_FEEDBACK"
  | "GRADE_ASSIGNMENTS";

export interface Organization {
  id: string;
  ownerUserId: string;
  legalName: string;
  displayName: string;
  website: string | null;
  industry: string;
  country: string;
  registrationNumber: string | null;
  contactName: string;
  contactEmail: string;
  contactPhone: string | null;
  status: OrganizationStatus;
  reviewReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface OrganizationApplication {
  legalName: string;
  displayName: string;
  website?: string;
  industry: string;
  country: string;
  registrationNumber?: string;
  contactName: string;
  contactEmail: string;
  contactPhone?: string;
}

export interface ApplicationRevision {
  id: string;
  organizationId: string;
  revisionNo: number;
  legalName: string;
  displayName: string;
  website: string | null;
  industry: string;
  country: string;
  registrationNumber: string | null;
  contactName: string;
  contactEmail: string;
  contactPhone: string | null;
  status: "DRAFT" | "PENDING" | "APPROVED" | "NEEDS_INFORMATION" | "REJECTED";
  reviewerUserId: string | null;
  reviewReason: string | null;
  submittedAt: string;
  documentMediaIds: string[];
  reviewedAt: string | null;
}

export interface Member {
  user_id: string;
  email: string;
  active: boolean;
  grants: Authority[];
}

export interface Invitation {
  id: string;
  organizationId: string;
  email: string;
  status: "PENDING" | "ACCEPTED" | "REVOKED" | "EXPIRED";
  expiresAt: string;
  createdAt: string;
}
