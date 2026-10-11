export interface MediaItem {
  sha256?: string;
  id: string;
  name: string;
  mimeType: string;
  size: number;
  createdAt: string;
}

export type MediaScope =
  | "AVATAR"
  | "ORGANIZATION"
  | "ISSUER"
  | "BRAND"
  | "COURSE_COVER"
  | "RESOURCE"
  | "LIBRARY"
  | "SUBMISSION";
