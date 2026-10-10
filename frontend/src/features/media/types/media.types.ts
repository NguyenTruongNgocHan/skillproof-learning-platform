export interface MediaItem {
  id: string;
  name: string;
  mimeType: string;
  size: number;
  createdAt: string;
}

export type MediaScope =
  "AVATAR" | "ORGANIZATION" | "RESOURCE" | "LIBRARY" | "SUBMISSION";
