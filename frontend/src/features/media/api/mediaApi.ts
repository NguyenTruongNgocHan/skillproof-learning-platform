import type { MediaItem, MediaScope } from "@/features/media/types/media.types"
import { ApiError, apiClient, getAccessToken, refreshAccessToken } from "@/shared/api/apiClient"
const base = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1"
async function mediaRequest(path: string, init: RequestInit, retry = true): Promise<Response> {
  const headers = new Headers(init.headers)
  if (getAccessToken()) headers.set("Authorization", `Bearer ${getAccessToken()}`)
  const response = await fetch(`${base}${path}`, {
    ...init,
    headers,
    credentials: "include",
  })
  if (response.status === 401 && retry) {
    await refreshAccessToken()
    return mediaRequest(path, init, false)
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => ({
      code: "UPLOAD_FAILED",
      message: "Could not transfer file.",
    }))
    throw new ApiError(response.status, problem)
  }
  return response
}
export const mediaApi = {
  list: (scope: MediaScope, target?: string) =>
    apiClient.get<MediaItem[]>(
      `/media?scope=${scope}${target ? `&target=${encodeURIComponent(target)}` : ""}`,
    ),
  upload: async (scope: MediaScope, target: string | undefined, file: File): Promise<MediaItem> => {
    const form = new FormData()
    form.append("scope", scope)
    if (target) form.append("target", target)
    form.append("file", file)
    return (
      await mediaRequest("/media", { method: "POST", body: form })
    ).json() as Promise<MediaItem>
  },
  remove: (id: string) => apiClient.delete<void>(`/media/${encodeURIComponent(id)}`),
  download: async (item: MediaItem): Promise<Blob> =>
    (await mediaRequest(`/media/${encodeURIComponent(item.id)}`, {})).blob(),
  save: async (item: MediaItem): Promise<void> => {
    const blob = await mediaApi.download(item)
    const url = URL.createObjectURL(blob)
    const a = document.createElement("a")
    a.href = url
    a.download = item.name
    document.body.append(a)
    a.click()
    a.remove()
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
  },
}
