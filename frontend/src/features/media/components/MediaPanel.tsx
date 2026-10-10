import { mediaApi } from "@/features/media/api/mediaApi"
import type { MediaItem, MediaScope } from "@/features/media/types/media.types"
import Button from "@/shared/ui/Button"
import { useEffect, useState } from "react"
function Preview({ item, auto = false }: { item: MediaItem; auto?: boolean }) {
  const [url, setUrl] = useState<string | null>(null)
  const [error, setError] = useState("")
  useEffect(() => {
    if (!auto) return
    let live = true
    mediaApi
      .download(item)
      .then((blob) => {
        if (live) setUrl(URL.createObjectURL(blob))
      })
      .catch((e) => {
        if (live) setError(e instanceof Error ? e.message : "Preview failed")
      })
    return () => {
      live = false
    }
  }, [auto, item])
  useEffect(
    () => () => {
      if (url) URL.revokeObjectURL(url)
    },
    [url],
  )
  if (
    !item.mimeType.startsWith("image/") &&
    !item.mimeType.startsWith("video/") &&
    !item.mimeType.startsWith("audio/")
  )
    return null
  return (
    <div className="v7-media-preview">
      <Button
        variant="ghost"
        onClick={() =>
          void mediaApi
            .download(item)
            .then((blob) => {
              setUrl(URL.createObjectURL(blob))
              setError("")
            })
            .catch((e) => setError(e instanceof Error ? e.message : "Preview failed"))
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
      {url && item.mimeType.startsWith("video/") && (
        <video controls src={url} aria-label={item.name} style={{ maxWidth: "100%" }} />
      )}
    </div>
  )
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
  scope: MediaScope
  target?: string
  writable?: boolean
  removable?: boolean
  onUploaded?: (item: MediaItem) => void
  onChanged?: () => void
  onBusyChange?: (busy: boolean) => void
  preferredId?: string
}) {
  const [items, setItems] = useState<MediaItem[]>([]),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false)
  useEffect(() => {
    onBusyChange?.(busy)
    return () => onBusyChange?.(false)
  }, [busy, onBusyChange])
  useEffect(() => {
    let live = true
    setItems([])
    mediaApi
      .list(scope, target)
      .then((rows) => {
        if (live) {
          setItems(rows)
          setError("")
        }
      })
      .catch((e) => {
        if (live) setError(e instanceof Error ? e.message : "Could not load files")
      })
    return () => {
      live = false
    }
  }, [scope, target])
  async function upload(file: File) {
    setBusy(true)
    setError("")
    try {
      const item = await mediaApi.upload(scope, target, file)
      setItems((prev) => [item, ...prev])
      onUploaded?.(item)
      onChanged?.()
    } catch (e) {
      setError(e instanceof Error ? e.message : "Upload failed")
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="v7-media">
      <h3>
        {scope === "AVATAR"
          ? "Profile image"
          : scope === "ORGANIZATION"
            ? "Organization documents"
            : "Lesson attachments"}
      </h3>
      {writable && (
        <label>
          Choose a file (up to 100 MB)
          <input
            type="file"
            disabled={busy}
            accept={
              scope === "AVATAR"
                ? "image/png,image/jpeg,image/webp"
                : scope === "ORGANIZATION"
                  ? "application/pdf,image/png,image/jpeg,image/webp"
                  : "image/png,image/jpeg,image/webp,application/pdf,video/mp4,video/webm,audio/mpeg,audio/wav"
            }
            onChange={(e) => {
              const f = e.target.files?.[0]
              if (f) void upload(f)
              e.target.value = ""
            }}
          />
        </label>
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
                auto={scope === "AVATAR" && item.id === (preferredId ?? items[0]?.id)}
              />
              <Button
                variant="outline"
                onClick={() =>
                  void mediaApi
                    .save(item)
                    .catch((e) => setError(e instanceof Error ? e.message : "Download failed"))
                }
              >
                {item.name} · {(item.size / 1024 / 1024).toFixed(1)} MB â†“
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
                      return
                    setBusy(true)
                    void mediaApi
                      .remove(item.id)
                      .then(() => setItems((rows) => rows.filter((row) => row.id !== item.id)))
                      .catch((e) => setError(e instanceof Error ? e.message : "Remove failed"))
                      .then(() => onChanged?.())
                      .finally(() => setBusy(false))
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
  )
}
