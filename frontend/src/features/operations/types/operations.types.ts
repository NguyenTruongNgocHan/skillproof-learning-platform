export interface Notice {
  id: string;
  category: string;
  message: string;
  link: string;
  createdAt: string;
  readAt: string | null;
}
export interface Audit {
  id: string;
  actorId: string;
  action: string;
  targetId: string | null;
  details: string;
  createdAt: string;
}
export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
}
