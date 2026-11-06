import { useEffect, useState } from "react"
import { createPortal } from "react-dom"
import { Download, Eye, RefreshCcw, X, FileText } from "lucide-react"
import { mediaApi } from "@/features/media/api/mediaApi"
export default function EvidencePreview({ id, label }: { id: string; label: string }) {
  const [open, setOpen] = useState(false)
  const [refreshKey, setRefreshKey] = useState(0)
  const [url, setUrl] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)
  useEffect(() => {
    if (!open) {
      setUrl("")
      setError("")
      return
    }
    let active = true
    setLoading(true)
    mediaApi
      .playback(id)
      .then((r) => {
        if (active) setUrl(r.url)
      })
      .catch((e) => {
        if (active) setError(e instanceof Error ? e.message : "Preview unavailable")
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [id, open, refreshKey])
  useEffect(() => {
    if (!open) return
    const prior = document.activeElement as HTMLElement | null
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        event.stopImmediatePropagation()
        event.preventDefault()
        setOpen(false)
      }
      if (event.key === "Tab") {
        event.stopImmediatePropagation()
        const elements = Array.from(
          document.querySelectorAll<HTMLElement>(".adm-viewer-dialog button:not(:disabled)"),
        )
        const first = elements[0],
          last = elements[elements.length - 1]
        if (event.shiftKey && document.activeElement === first) {
          event.preventDefault()
          last?.focus()
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault()
          first?.focus()
        }
      }
    }
    document.addEventListener("keydown", handleKey, true)
    document.querySelector<HTMLButtonElement>(".adm-viewer-close")?.focus()
    return () => {
      document.removeEventListener("keydown", handleKey, true)
      document.body.style.overflow = previousOverflow
      prior?.focus()
    }
  }, [open])
  async function download() {
    try {
      await mediaApi.save({
        id,
        name: label,
        mimeType: "application/octet-stream",
        size: 0,
        createdAt: "",
      })
    } catch (e) {
      setError(e instanceof Error ? e.message : "Download failed")
    }
  }
  return (
    <>
      <div className="adm-evidence-row">
        <div className="adm-evidence-name">
          <FileText size={18} />
          <span title={label}>{label}</span>
        </div>
        <div className="adm-evidence-actions">
          <button className="adm-btn" onClick={() => setOpen(true)}>
            <Eye size={15} /> Preview
          </button>
          <button className="adm-btn adm-btn-quiet" onClick={() => void download()}>
            <Download size={15} /> Download
          </button>
        </div>
      </div>
      {open &&
        createPortal(
          <div
            className="adm-viewer-backdrop"
            onMouseDown={(e) => {
              if (e.target === e.currentTarget) setOpen(false)
            }}
          >
            <section
              className="adm-viewer-dialog"
              role="dialog"
              aria-modal="true"
              aria-label={`Preview ${label}`}
            >
              <header className="adm-viewer-header">
                <div>
                  <strong>{label}</strong>
                  <p>Submission evidence · Secure preview</p>
                </div>
                <button
                  className="adm-icon-button adm-viewer-close"
                  aria-label="Close evidence preview"
                  onClick={() => setOpen(false)}
                >
                  <X size={20} />
                </button>
              </header>
              <div className="adm-viewer-content">
                {loading ? (
                  <div className="adm-viewer-message">Loading document preview…</div>
                ) : error ? (
                  <div className="adm-viewer-message" role="alert">
                    {error}
                  </div>
                ) : url ? (
                  <iframe title={label} src={url} className="adm-viewer-frame" />
                ) : (
                  <div className="adm-viewer-message">
                    No preview available. Download the original file instead.
                  </div>
                )}
              </div>
              <footer className="adm-viewer-footer">
                <span>Preview links expire automatically. The original file is unchanged.</span>
                <div>
                  <button
                    className="adm-btn"
                    onClick={() => {
                      setUrl("")
                      setRefreshKey((v) => v + 1)
                    }}
                  >
                    <RefreshCcw size={15} /> Refresh
                  </button>
                  <button className="adm-btn adm-btn-primary" onClick={() => void download()}>
                    <Download size={15} /> Download original
                  </button>
                </div>
              </footer>
            </section>
          </div>,
          document.body,
        )}
    </>
  )
}
