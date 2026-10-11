import { apiClient } from "@/shared/api/apiClient";
import type { Review, Report, Page } from "../types/feedback.types";
const e = encodeURIComponent;
export const feedbackApi = {
  history: (id: string) =>
    apiClient.get<
      {
        id: string;
        actorId: string;
        reportId: string;
        decision: string;
        reason: string;
        createdAt: string;
      }[]
    >(`/feedback/${e(id)}/moderation-history`),
  adminReview: (id: string) =>
    apiClient.get<Review>(`/admin/feedback/reviews/${e(id)}`),
  publicList: (type: string, id: string, page = 0) =>
    apiClient.get<{
      average: number | null;
      count: number;
      reviews: Page<Review>;
    }>(`/public/feedback/${e(type)}/${e(id)}?page=${page}`),
  organization: (org: string, page = 0) =>
    apiClient.get<Page<Review>>(
      `/organizations/${e(org)}/feedback?page=${page}`,
    ),
  reply: (id: string, body: string) =>
    apiClient.post(`/feedback/${e(id)}/reply`, { body }),
  write: (type: string, id: string, rating: number, body: string) =>
    apiClient.put(`/feedback/${e(type)}/${e(id)}`, { rating, body }),
  report: (id: string, body: string) =>
    apiClient.post(`/feedback/${e(id)}/reports`, { body }),
  reports: (page = 0) =>
    apiClient.get<Page<Report>>(`/admin/feedback/reports?page=${page}`),
  resolve: (id: string, hide: boolean, reason: string) =>
    apiClient.post(`/admin/feedback/reports/${e(id)}/resolve`, {
      hide,
      reason,
    }),
};
