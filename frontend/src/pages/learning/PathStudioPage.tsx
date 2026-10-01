import { Link } from "react-router-dom"
import AppShell from "@/components/layout/AppShell"
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
      <AppShell>
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
      </AppShell>
    </PathStudioProvider>
  )
}
