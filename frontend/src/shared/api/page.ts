export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}
export function searchParams(
  values: Record<string, string | number | boolean | undefined>,
): string {
  const result = new URLSearchParams()
  for (const [key, value] of Object.entries(values))
    if (value !== undefined) result.set(key, String(value))
  return result.toString()
}
