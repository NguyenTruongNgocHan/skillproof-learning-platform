import { mediaApi } from "@/features/media/api/mediaApi";
import type { MediaItem, MediaScope } from "@/features/media/types/media.types";
import Button from "@/shared/ui/Button";
import { cancelMultipart } from "../api/multipartUpload";
import { useEffect, useState, useRef } from "react";
function Preview({ item, auto = false }: { item: MediaItem; auto?: boolean }) {
  const [url, setUrl] = useState<string | null>(null);
  const [error, setError] = useState("");
  useEffect(() => {
    if (!auto) return;
    let live = true;
    mediaApi
      .playback(item.id)
      .then((link) => {
        if (live) setUrl(link.url);
      })
      .catch((e) => {
        if (live) setError(e instanceof Error ? e.message : "Preview failed");
      });
    return () => {
      live = false;
    };
  }, [auto, item]);
  useEffect(
    () => () => {
      if (url) URL.revokeObjectURL(url);
    },
    [url],
  );
  if (
    !item.mimeType.startsWith("image/") &&
    !item.mimeType.startsWith("video/") &&
    !item.mimeType.startsWith("audio/") &&
    item.mimeType !== "application/pdf"
  )
    return null;
  return (
    <div className="v7-media-preview">
      <Button
        variant="ghost"
        onClick={() =>
          void mediaApi
            .playback(item.id)
            .then((link) => {
              setUrl(link.url);
              setError("");
            })
            .catch((e) =>
              setError(e instanceof Error ? e.message : "Preview failed"),
            )
        }
      >
        Preview
      </Button>
      {error && <span role="alert">{error}</span>}
      {url && item.mimeType.startsWith("image/") && (
        <img
          src={url}
          alt={item.name}
          loading="lazy"
          style={{ maxWidth: 240, maxHeight: 180, objectFit: "contain" }}
        />
      )}
      {url && item.mimeType.startsWith("audio/") && (
        <audio controls src={url} aria-label={item.name} />
      )}
      {url && item.mimeType === "application/pdf" && (
        <iframe
          title={item.name}
          src={url}
          style={{ width: "100%", height: 480, border: 0 }}
        />
      )}
      {url && item.mimeType.startsWith("video/") && (
        <video
          controls
          src={url}
          aria-label={item.name}
          style={{ maxWidth: "100%" }}
        />
      )}
    </div>
  );
}
export default function MediaPanel({
  scope,
  target,
  writable = false,
  removable = false,
  onUploaded,
  onChanged,
  preferredId,
  onBusyChange,
}: {
  scope: MediaScope;
  target?: string;
  writable?: boolean;
  removable?: boolean;
  onUploaded?: (item: MediaItem) => void;
  onChanged?: () => void;
  onBusyChange?: (busy: boolean) => void;
  preferredId?: string;
}) {
  const lastFile = useRef<File | null>(null);
  const [uploadError, setUploadError] = useState(false);
  const [transfer, setTransfer] = useState(0);
  const [items, setItems] = useState<MediaItem[]>([]),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    onBusyChange?.(busy);
    return () => onBusyChange?.(false);
  }, [busy, onBusyChange]);
  useEffect(() => {
    let live = true;
    setItems([]);
    mediaApi
      .list(scope, target)
      .then((rows) => {
        if (live) {
          setItems(rows);
          setError("");
        }
      })
      .catch((e) => {
        if (live)
          setError(e instanceof Error ? e.message : "Could not load files");
      });
    return () => {
      live = false;
    };
  }, [scope, target]);
  async function upload(file: File) {
    if (busy) return;
    lastFile.current = file;
    setUploadError(false);
    setTransfer(0);
    setBusy(true);
    setError("");
    try {
      const item =
        scope === "AVATAR"
          ? await mediaApi.upload(scope, target, file)
          : await mediaApi.direct(scope, target, file, setTransfer);
      setItems((prev) => [item, ...prev]);
      onUploaded?.(item);
      onChanged?.();
      lastFile.current = null;
    } catch (e) {
      setUploadError(true);
      setError(e instanceof Error ? e.message : "Upload failed");
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="v7-media">
      {busy && (
        <p role="status">
          Uploading {transfer}% · validating file before attachment…
        </p>
      )}
      <h3>
        {scope === "AVATAR" || scope === "BRAND" || scope === "COURSE_COVER"
          ? "Profile image"
          : scope === "ORGANIZATION" || scope === "ISSUER"
            ? "Organization documents"
            : "Lesson attachments"}
      </h3>
      {writable && (
        <label>
          Choose a file (up to{" "}
          {scope === "BRAND" || scope === "COURSE_COVER" ? "10" : "500"} MB)
          <input
            type="file"
            disabled={busy}
            accept={
              scope === "AVATAR" ||
              scope === "BRAND" ||
              scope === "COURSE_COVER"
                ? "image/png,image/jpeg,image/webp"
                : scope === "ORGANIZATION" || scope === "ISSUER"
                  ? "application/pdf,image/png,image/jpeg,image/webp"
                  : "image/png,image/jpeg,image/webp,application/pdf,video/mp4,video/webm,audio/mpeg,audio/wav"
            }
            onChange={(e) => {
              const f = e.target.files?.[0];
              if (f) void upload(f);
              e.target.value = "";
            }}
          />
        </label>
      )}
      {busy && lastFile.current && lastFile.current.size > 33554432 && (
        <Button
          variant="outline"
          onClick={() =>
            void cancelMultipart(scope, target, lastFile.current!).catch((e) =>
              setError(e.message),
            )
          }
        >
          Cancel multipart upload
        </Button>
      )}
      {uploadError && lastFile.current && !busy && (
        <div className="sporg-actions">
          <Button
            variant="outline"
            onClick={() => void upload(lastFile.current!)}
          >
            Retry upload (retain completed chunks)
          </Button>
          <Button
            variant="ghost"
            onClick={() => {
              void cancelMultipart(scope, target, lastFile.current!).catch(
                () => {},
              );
              lastFile.current = null;
              setUploadError(false);
              setError("");
            }}
          >
            Discard upload
          </Button>
        </div>
      )}
      {busy && <p role="status">Uploading file…</p>}
      {error && (
        <p role="alert" className="org-error">
          {error}{" "}
          <Button
            variant="outline"
            onClick={() =>
              void mediaApi
                .list(scope, target)
                .then(setItems)
                .catch(() => setError("Could not load files"))
            }
          >
            Retry
          </Button>
        </p>
      )}
      {items.length === 0 ? (
        <p>No files uploaded yet.</p>
      ) : (
        <ul>
          {items.map((item) => (
            <li key={item.id}>
              <Preview
                item={item}
                auto={
                  scope === "AVATAR" &&
                  item.id === (preferredId ?? items[0]?.id)
                }
              />
              <Button
                variant="outline"
                onClick={() =>
                  void mediaApi
                    .save(item)
                    .catch((e) =>
                      setError(
                        e instanceof Error ? e.message : "Download failed",
                      ),
                    )
                }
              >
                {item.name} · {(item.size / 1024 / 1024).toFixed(1)} MB ↓
              </Button>
              {writable && removable && (
                <Button
                  variant="destructive"
                  disabled={busy}
                  onClick={() => {
                    if (
                      !window.confirm(
                        `Remove ${item.name}? Previously submitted evidence will remain in submission history.`,
                      )
                    )
                      return;
                    setBusy(true);
                    void mediaApi
                      .remove(item.id)
                      .then(() =>
                        setItems((rows) =>
                          rows.filter((row) => row.id !== item.id),
                        ),
                      )
                      .catch((e) =>
                        setError(
                          e instanceof Error ? e.message : "Remove failed",
                        ),
                      )
                      .then(() => onChanged?.())
                      .finally(() => setBusy(false));
                  }}
                >
                  Remove
                </Button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
