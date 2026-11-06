import { useSearchParams } from "react-router-dom"
import Input from "@/shared/ui/Input"
import Button from "@/shared/ui/Button"
import { useCallback, useState } from "react"
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { workspaceCourseApi } from "@/features/course/api/workspaceCourseApi"
import { useRemote } from "@/shared/hooks/useRemote"
import { RemoteState } from "@/shared/ui/RemoteState"
import { CourseCommercialPanel } from "../components/CourseCommercialPanel"
export default function OrganizationPricingPage() {
  const { organization, confirmDiscard } = useOrganizationContext()
  const remote = useRemote(
    useCallback(() => workspaceCourseApi.directory(organization?.id ?? ""), [organization?.id]),
  )
  const [params] = useSearchParams()
  const [selected, setSelected] = useState(params.get("version") ?? "")
  const [lookup, setLookup] = useState(selected)
  const versionId = selected || remote.data?.[0]?.id || ""
  return (
    <OrganizerSurface
      title="Pricing & promotions"
      description="Manage commercial offers independently of course authoring authority."
      authority="MANAGE_PRICING"
    >
      <RemoteState {...remote} retry={remote.reload} />
      {remote.data?.length === 0 && <p>No published course available for pricing.</p>}
      <form
        className="sporg-form"
        onSubmit={(e) => {
          e.preventDefault()
          if (confirmDiscard()) setSelected(lookup.trim())
        }}
      >
        <Input
          label="Course version ID from your content author (including drafts)"
          value={lookup}
          onChange={(e) => setLookup(e.target.value)}
          required
          pattern="[0-9a-fA-F-]{36}"
        />
        <Button type="submit" variant="outline">
          Open course pricing
        </Button>
      </form>
      {versionId && (
        <>
          <label>
            Published course version
            <select
              value={remote.data?.some((v) => v.id === versionId) ? versionId : ""}
              onChange={(e) => {
                if (confirmDiscard("Discard unsaved commercial settings?"))
                  setSelected(e.target.value)
              }}
            >
              <option value="">Explicit version selected</option>
              {remote.data?.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.title} · {v.id}
                </option>
              ))}
            </select>
          </label>
          <CourseCommercialPanel key={versionId} versionId={versionId} />
        </>
      )}
    </OrganizerSurface>
  )
}
