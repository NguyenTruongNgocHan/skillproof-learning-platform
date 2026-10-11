export interface Review {
  id: string;
  actorId: string;
  organizationId: string;
  type: string;
  targetId: string;
  rating: number;
  body: string;
  reply: string | null;
  status: string;
  createdAt: string;
}
export interface Report {
  id: string;
  reviewId: string;
  reason: string;
  status: string;
  createdAt: string;
}
export interface Page<T> {
  content: T[];
  number: number;
  totalPages: number;
  totalElements: number;
}
