import { uploadMultipart } from "./multipartUpload";
import type { MediaItem, MediaScope } from "@/features/media/types/media.types";
import {
  ApiError,
  apiClient,
  getAccessToken,
  refreshAccessToken,
} from "@/shared/api/apiClient";
const base = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1";
async function mediaRequest(
  path: string,
  init: RequestInit,
  retry = true,
): Promise<Response> {
  const headers = new Headers(init.headers);
  if (getAccessToken())
    headers.set("Authorization", `Bearer ${getAccessToken()}`);
  const response = await fetch(`${base}${path}`, {
    ...init,
    headers,
    credentials: "include",
  });
  if (response.status === 401 && retry) {
    await refreshAccessToken();
    return mediaRequest(path, init, false);
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => ({
      code: "UPLOAD_FAILED",
      message: "Could not transfer file.",
    }));
    throw new ApiError(response.status, problem);
  }
  return response;
}
export const mediaApi = {
  playback: (id: string) =>
    apiClient.get<{ url: string; expiresAt: string }>(
      `/media/${encodeURIComponent(id)}/playback`,
    ),
  list: (scope: MediaScope, target?: string) =>
    apiClient.get<MediaItem[]>(
      `/media?scope=${scope}${target ? `&target=${encodeURIComponent(target)}` : ""}`,
    ),
  upload: async (
    scope: MediaScope,
    target: string | undefined,
    file: File,
  ): Promise<MediaItem> => {
    const form = new FormData();
    form.append("scope", scope);
    if (target) form.append("target", target);
    form.append("file", file);
    return (
      await mediaRequest("/media", { method: "POST", body: form })
    ).json() as Promise<MediaItem>;
  },
  direct: async (
    scope: MediaScope,
    target: string | undefined,
    file: File,
    onProgress?: (percent: number) => void,
  ): Promise<MediaItem> => {
    if (file.size > 33554432)
      return uploadMultipart(scope, target, file, onProgress);
    const ticket = await apiClient.post<{ id: string; url: string }>(
      "/media/direct",
      { scope, target, filename: file.name, size: file.size },
    );
    await new Promise<void>((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open("PUT", ticket.url);
      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable)
          onProgress?.(Math.round((e.loaded / e.total) * 100));
      };
      xhr.onload = () =>
        xhr.status >= 200 && xhr.status < 300
          ? resolve()
          : reject(
              new Error("Object upload failed. Check storage CORS and retry."),
            );
      xhr.onerror = () => reject(new Error("Object upload connection failed"));
      xhr.send(file);
    });
    return apiClient.post<MediaItem>(
      `/media/direct/${encodeURIComponent(ticket.id)}/complete`,
      undefined,
      undefined,
      600000,
    );
  },
  remove: (id: string) =>
    apiClient.delete<void>(`/media/${encodeURIComponent(id)}`),
  download: async (item: MediaItem): Promise<Blob> =>
    (await mediaRequest(`/media/${encodeURIComponent(item.id)}`, {})).blob(),
  save: async (item: MediaItem): Promise<void> => {
    const blob = await mediaApi.download(item);
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = item.name;
    document.body.append(a);
    a.click();
    a.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
  },
};
