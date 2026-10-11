import { apiClient } from "@/shared/api/apiClient";
import type { MediaItem, MediaScope } from "../types/media.types";
type Ticket = { id: string; partSize: number; expiresAt: string };
type Job = { ticket: Ticket; cancelled: boolean; active?: XMLHttpRequest };
// Retry state and File handles live only in memory. Uploaded chunks live in S3, never on the backend filesystem.
const jobs = new Map<string, Job>();
const key = (scope: MediaScope, target: string | undefined, file: File) =>
  `${scope}:${target}:${file.name}:${file.size}:${file.lastModified}`;
function put(
  url: string,
  body: Blob,
  job: Job,
  progress: (bytes: number) => void,
) {
  return new Promise<void>((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    job.active = xhr;
    xhr.open("PUT", url);
    xhr.timeout = 180000;
    xhr.upload.onprogress = (e) => progress(e.loaded);
    xhr.onload = () => {
      job.active = undefined;
      xhr.status >= 200 && xhr.status < 300
        ? resolve()
        : reject(new Error(`Part upload failed (${xhr.status})`));
    };
    xhr.onerror = xhr.ontimeout = () => {
      job.active = undefined;
      reject(
        new Error("Part upload disconnected; retry keeps completed chunks"),
      );
    };
    xhr.onabort = () => {
      job.active = undefined;
      reject(new Error("Upload cancelled"));
    };
    xhr.send(body);
  });
}
export async function cancelMultipart(
  scope: MediaScope,
  target: string | undefined,
  file: File,
) {
  const k = key(scope, target, file),
    job = jobs.get(k);
  if (!job) return;
  job.cancelled = true;
  job.active?.abort();
  try {
    await apiClient.delete(`/media/multipart/${job.ticket.id}`);
  } finally {
    jobs.delete(k);
  }
}
export async function uploadMultipart(
  scope: MediaScope,
  target: string | undefined,
  file: File,
  onProgress?: (v: number) => void,
): Promise<MediaItem> {
  const k = key(scope, target, file);
  let job = jobs.get(k);
  if (!job || new Date(job.ticket.expiresAt).getTime() <= Date.now()) {
    const ticket = await apiClient.post<Ticket>("/media/multipart", {
      scope,
      target,
      filename: file.name,
      size: file.size,
    });
    job = { ticket, cancelled: false };
    jobs.set(k, job);
  }
  const state = await apiClient.get<{
    assembled: boolean;
    parts: { number: number; size: number }[];
  }>(`/media/multipart/${job.ticket.id}`);
  const partSize = job.ticket.partSize;
  if (!state.assembled) {
    const done = new Set(state.parts.map((p) => p.number));
    let uploaded = state.parts.reduce((n, p) => n + p.size, 0);
    onProgress?.(Math.round((uploaded / file.size) * 100));
    for (let part = 1; part <= Math.ceil(file.size / partSize); part++) {
      if (job.cancelled) throw new Error("Upload cancelled");
      if (done.has(part)) continue;
      const blob = file.slice(
        (part - 1) * partSize,
        Math.min(file.size, part * partSize),
      );
      let success = false;
      let error: unknown;
      for (let retry = 0; retry < 3 && !success; retry++) {
        if (job.cancelled) throw new Error("Upload cancelled");
        try {
          const signed = await apiClient.post<{ url: string }>(
            `/media/multipart/${job.ticket.id}/parts/${part}`,
          );
          await put(signed.url, blob, job, (bytes) =>
            onProgress?.(Math.round(((uploaded + bytes) / file.size) * 100)),
          );
          success = true;
        } catch (e) {
          error = e;
        }
      }
      if (!success) throw error;
      uploaded += blob.size;
      onProgress?.(Math.round((uploaded / file.size) * 100));
    }
    await apiClient.post(
      `/media/multipart/${job.ticket.id}/assemble`,
      undefined,
      undefined,
      300000,
    );
  }
  const media = await apiClient.post<MediaItem>(
    `/media/direct/${job.ticket.id}/complete`,
    undefined,
    undefined,
    600000,
  );
  jobs.delete(k);
  return media;
}
