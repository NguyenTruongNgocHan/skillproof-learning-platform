import { ApiError } from "@/shared/api/apiClient"
export function organizerError(
  error: unknown,
  fallback = "The action could not be completed.",
): string {
  if (error instanceof ApiError && error.problem.fieldErrors) {
    const fields = Object.entries(error.problem.fieldErrors).map(
      ([field, message]) => `${field.replace(/([A-Z])/g, " $1")}: ${message}`,
    )
    if (fields.length) return fields.join(". ")
  }
  return error instanceof Error ? error.message : fallback
}
