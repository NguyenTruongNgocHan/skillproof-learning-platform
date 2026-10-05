import { PathMetadataPanel } from "@/features/learning/studio/PathMetadataPanel"
import { Link } from "react-router-dom"
import { OrganizerSurface } from "@/features/organizer/OrganizerSurface"
import Button from "@/components/ui/Button"
import { learningApi } from "@/features/learning/learningApi"
import { usePathStudio } from "@/features/learning/usePathStudio"
import { PathStudioProvider } from "@/features/learning/PathStudioContext"
import JourneySection from "@/features/learning/studio/JourneySection"
import AssessmentSection from "@/features/learning/studio/AssessmentSection"
import PublishingSection from "@/features/learning/studio/PublishingSection"

export default function PathStudioPage() {
  const studio = usePathStudio()
  const {
    id,
    path,
    version,
    versions,
    modules,
    assessments,
    error,
    success,
    loading,
    busy,
    load,
    act,
  } = studio
  const draft = version?.status === "DRAFT"
  return (
    <PathStudioProvider value={studio}>
      <OrganizerSurface title="Path studio" authority="MANAGE_CONTENT">
        <div className="v7-wrap">
          <Link className="profile-back" to="/organizer/paths">
            ← All paths
          </Link>
          {loading && !path ? (
            <p role="status">Loading path studio…</p>
          ) : error && !version ? (
            <p role="alert" className="org-error">
              {error}{" "}
              <Button variant="outline" onClick={() => void load()}>
                Retry
              </Button>
            </p>
          ) : (
            <>
              <header className="v7-hero">
                <span className="org-eyebrow">
                  LEARNING PATH STUDIO ·{" "}
                  {draft ? "EDITING DRAFT" : "PUBLISHED VERSION"}
                </span>
                <h1>{path?.title ?? "Shape the next version"}</h1>
                <p>
                  Build modules, add learning resources, define assessments and
                  completion rules, then publish a version learners can trust.
                </p>
                <label>
                  Version
                  <select
                    aria-label="Select learning path version"
                    value={version?.id ?? ""}
                    disabled={busy || loading}
                    onChange={(e) => {
                      if (
                        (studio.moduleTitle ||
                          studio.resourceTitle ||
                          studio.assessmentTitle) &&
                        !window.confirm(
                          "Discard unsaved authoring changes and switch version?",
                        )
                      )
                        return
                      studio.setModuleTitle("")
                      studio.setResourceTitle("")
                      studio.setResourceBody("")
                      studio.setAssessmentTitle("")
                      void load(e.target.value)
                    }}
                  >
                    {versions.map((v) => (
                      <option key={v.id} value={v.id}>
                        Version {v.version_no} · {v.status}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="v7-inline">
                  <span>
                    {version &&
                      `Version ${version.version_no} · ${version.status}`}
                  </span>
                  {!draft && id && (
                    <Button
                      disabled={busy}
                      variant="outline"
                      onClick={() =>
                        void act(
                          () => learningApi.clone(id),
                          "New draft created from the published version.",
                        )
                      }
                    >
                      Create new draft version
                    </Button>
                  )}
                </div>
              </header>
              {path && (
                <PathMetadataPanel
                  key={path.title + path.summary}
                  path={path}
                  onSaved={() => load()}
                />
              )}
              {error && (
                <p role="alert" className="org-error">
                  {error}
                </p>
              )}
              {success && (
                <p role="status" className="org-success">
                  {success}
                </p>
              )}
              {!version ? (
                <p>No version found.</p>
              ) : (
                <>
                  <JourneySection />
                  <AssessmentSection />
                  <PublishingSection />
                </>
              )}
            </>
          )}
        </div>
      </OrganizerSurface>
    </PathStudioProvider>
  )
}
