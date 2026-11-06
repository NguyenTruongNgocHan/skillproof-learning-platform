import { apiClient } from "@/shared/api/apiClient"
import { searchParams, type PageResult } from "@/shared/api/page"
import type { Bank, Question, Choice } from "../types/quiz.types"
export interface QuestionRevision {
  id: string
  questionId: string
  versionNo: number
  stem: string
  createdAt: string
}
export interface QuestionPreview {
  id: string
  versionNo: number
  stem: string
  options: Pick<Choice, "id" | "body" | "correct">[]
}
const e = encodeURIComponent
export const questionBankApi = {
  banks: (org: string, query: string, includeArchived: boolean, page: number) =>
    apiClient.get<PageResult<Bank>>(
      `/quiz/organizations/${e(org)}/banks/search?${searchParams({ query, includeArchived, page, size: 20 })}`,
    ),
  questions: (bank: string, query: string, includeArchived: boolean, page: number) =>
    apiClient.get<PageResult<Question>>(
      `/quiz/banks/${e(bank)}/questions/search?${searchParams({ query, includeArchived, page, size: 20 })}`,
    ),
  rename: (id: string, title: string) => apiClient.patch<Bank>(`/quiz/banks/${e(id)}`, { title }),
  archiveBank: (id: string) => apiClient.post(`/quiz/banks/${e(id)}/archive`),
  archiveQuestion: (id: string) => apiClient.post(`/quiz/questions/${e(id)}/archive`),
  revisions: (id: string) => apiClient.get<QuestionRevision[]>(`/quiz/questions/${e(id)}/versions`),
  preview: (id: string) =>
    apiClient.get<QuestionPreview>(`/quiz/question-versions/${e(id)}/author-preview`),
}
