import { organizerError } from "@/features/organizer/errorMessage"
import { useEffect, useState } from "react"
import { learningApi, type Path } from "../learningApi"
import { useOrganizationContext } from "@/app/providers/OrganizationProvider"
import Button from "@/components/ui/Button"
import Input from "@/components/ui/Input"
export function PathMetadataPanel({
  path,
  onSaved,
}: {
  path: Path
  onSaved: () => Promise<unknown>
}) {
  const [title, setTitle] = useState(path.title),
    [summary, setSummary] = useState(path.summary),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("")
  const { setDirty } = useOrganizationContext()
  const dirty = title !== path.title || summary !== path.summary
  useEffect(() => {
    setDirty("path-metadata", dirty)
    return () => setDirty("path-metadata", false)
  }, [dirty, setDirty])
  async function save() {
    setBusy(true)
    setError("")
    try {
      await learningApi.updatePath(path.id, { title, summary })
      setDirty("path-metadata", false)
      await onSaved()
    } catch (e) {
      setError(organizerError(e, "Unable to save path details"))
    } finally {
      setBusy(false)
    }
  }
  return (
    <details className="sporg-card">
      <summary>Learning path details</summary>
      <p>
        These are the public listing details. Published version content remains
        fixed.
      </p>
      {error && <p role="alert">{error}</p>}
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault()
          void save()
        }}
      >
        <Input
          label="Title"
          required
          maxLength={180}
          value={title}
          disabled={busy}
          onChange={(e) => setTitle(e.target.value)}
        />
        <label>
          Summary
          <textarea
            required
            maxLength={2000}
            value={summary}
            disabled={busy}
            onChange={(e) => setSummary(e.target.value)}
          />
        </label>
        <Button type="submit" disabled={!dirty || busy}>
          {busy ? "Saving…" : "Save listing details"}
        </Button>
      </form>
    </details>
  )
}
